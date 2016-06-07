package com.takeeateasy.courierchallenge.server;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.takeeateasy.courierchallenge.api.*;
import com.takeeateasy.courierchallenge.util.DistanceUtil;
import com.takeeateasy.courierchallenge.util.StatePrinter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Game {

    public static final boolean KICK_ENABLED = true;
    public static final boolean ELECTRIC_ENABLED = true;
    public static final int PRICE_UPGRADE_KICK = 150;
    public static final int PRICE_UPGRADE_ELECTRIC = 100;

    public static final int MAX_TURN = 400;
    private static final long SLEEP_TIME = 800;


    SocketIOServer server;
    Map<String, PlayerClient> players = new ConcurrentHashMap<String, PlayerClient>();

    List<Position> restaurantsPosition = new ArrayList<>();

    Map<Integer,Integer> playerStunned = new HashMap<>();
    private State state = null;
    @Getter
    private City city;
    private DistanceMatrix distanceMatrix;
    private Queue<PlayerAction> actionQueue = new ConcurrentLinkedQueue();
    private static final AtomicInteger playerIdGenerator = new AtomicInteger(0);
    private static final AtomicInteger orderIdGenerator = new AtomicInteger(0);


    public Game(SocketIOServer server) {
        this.server = server;
    }

    public void initGame() throws FileNotFoundException {
        state = new State();
        city = new City();
        players = new ConcurrentHashMap<String, PlayerClient>();

        readMapFromFile2("/map.txt");
        //createSampleMap(mapSize);
        //createRestaurants(state, nbRestaurants);

        log.info("{}", new StatePrinter().printState(state, city));

        distanceMatrix = new DistanceMatrix(city);
        city.setDistances(distanceMatrix.getDistances());

        addOrders(state);

        log.info(distanceMatrix.toString());

        log.info("{}", new StatePrinter().printState(state, city));
    }

    private void createRandomRestaurants(State state, int nbRestaurants) {
        retry:
        for (int i = 0; i < nbRestaurants; i++) {
            Position position = getRandomRoadPosition();
            city.getRoads()[position.getX()][position.getY()]= City.RESTAURANT;
            restaurantsPosition.add(position);
        }
    }

    private void addOrders(State state) {
        int nbCouriers = state.getCouriers().size();
        int nbOrders = state.getOrders().size();
        int missingOrders = nbCouriers + (int) Math.floor(Math.random() * nbCouriers * 3) - nbOrders+1;
        if (missingOrders > 0) {
            for (int i = 0; i < missingOrders; i++) {
                addOrder(state);
            }
        }
    }

    private void addOrder(State state) {
        Order order = new Order();
        order.setTo(getRandomRoadPosition());
        order.setId(orderIdGenerator.getAndIncrement());
        state.getOrders().add(order);
        Position restaurant = restaurantsPosition.get((int) (restaurantsPosition.size() * Math.random()));
        order.setFrom(restaurant);
        order.setValue(DistanceUtil.getDistance(order.getFrom(),order.getTo(),city)*2+10);
    }



    public void newGame(){
        List<Courier> previousCouriers = state.getCouriers();
        state = new State();
        for(Courier courier : previousCouriers){
            state.getCouriers().add(courier);
        }
        for (final PlayerClient client : players.values()) {
            client.getClient().sendEvent("new_game");
        }
    }

    public void nextTurn(){
        Queue<PlayerAction> finalQueue = new ConcurrentLinkedQueue<PlayerAction>(actionQueue);
        actionQueue = new ConcurrentLinkedQueue<PlayerAction>();
        executeActionQueue(finalQueue);

        notifyControllers(state);

        state.setTurn(state.getTurn() + 1);
        if (state.getTurn() % 5 == 0)
            addOrders(state);
        cleanOldPlayers();
        decreaseOrderValues();

        List<PlayerClient> players = new ArrayList(this.players.values());
        Collections.shuffle(players);

        for (final PlayerClient client : players) {
            try {
                if (client.isBusy())
                    continue;
                int idCourier = client.getCourier().getId();
                if(playerStunned.containsKey(idCourier)){
                    int value = playerStunned.get(idCourier);
                    if(value <= 0){
                        playerStunned.remove(idCourier);
                    }else{
                        playerStunned.put(idCourier,value-1);
                        continue;
                    }
                }

                final List<Action> possibleActions = computePossibleActions(state, client.getCourier());
                log.info("Possible actions: {}", possibleActions);
                PlayTurn playTurn = new PlayTurn();
                playTurn.setIdCourier(client.getCourier().getId());
                playTurn.setPossibleActions(possibleActions);
                playTurn.setScore(client.getCourier().getScore());
                playTurn.setState(state);
                client.setBusy(true);
                client.getClient().sendEvent("turn", new AckCallback<Action>(Action.class) {
                    @Override
                    public void onSuccess(Action action) {
                        log.info("Returned action : {}", action);
                        try {
                            if (possibleActions.contains(action)) {
                                actionQueue.add(new PlayerAction(action, client.getCourier()));
                                client.setLastTurnPlayed(state.getTurn());
                            }
                        } catch (Exception e) {
                            log.error("Error",e);
                        }
                        client.setBusy(false);
                    }
                }, playTurn);
            } catch (Exception e) {
                log.error("Error playing player "+client.getCourier().getName(),e);
            }
        }
        log.info("{}", new StatePrinter().printState(state, city));
    }


    private void executeActionQueue(Queue<PlayerAction> actionQueue) {
        while (!actionQueue.isEmpty()) {
            PlayerAction playerAction = actionQueue.remove();
            if (playerAction.getAction().getTurn() == state.getTurn()) {
                executeAction(playerAction.getAction(), playerAction.getPlayer());
            } else {
                log.warn("Ignoring action {} of turn {} at {}", playerAction, playerAction.getAction().getTurn(), state.getTurn());
            }
        }
    }

    private void executeAction(Action action, Courier player) {
        for (Courier courier : state.getCouriers()) {
            if (courier.getId().equals(action.getIdCourier())) {
                if (Action.MOVE_DOWN.equals(action.getAction())) {
                    courier.getPosition().setY(courier.getPosition().getY() + 1);
                } else if (Action.MOVE_UP.equals(action.getAction())) {
                    courier.getPosition().setY(courier.getPosition().getY() - 1);
                } else if (Action.MOVE_LEFT.equals(action.getAction())) {
                    courier.getPosition().setX(courier.getPosition().getX() - 1);
                } else if (Action.MOVE_RIGHT.equals(action.getAction())) {
                    courier.getPosition().setX(courier.getPosition().getX() + 1);
                } else if (Action.MOVE_DOWN_DOUBLE.equals(action.getAction())) {
                    courier.getPosition().setY(courier.getPosition().getY() + 2);
                } else if (Action.MOVE_UP_DOUBLE.equals(action.getAction())) {
                    courier.getPosition().setY(courier.getPosition().getY() - 2);
                } else if (Action.MOVE_LEFT_DOUBLE.equals(action.getAction())) {
                    courier.getPosition().setX(courier.getPosition().getX() - 2);
                } else if (Action.MOVE_RIGHT_DOUBLE.equals(action.getAction())) {
                    courier.getPosition().setX(courier.getPosition().getX() + 2);
                }else if (Action.DROP_ORDER.equals(action.getAction())) {
                    for (Order order : new ArrayList<Order>(state.getOrders())) {
                        if (order.getId() == action.getIdOrder() && order.getStatus() == Order.STATUS_ONGOING) {
                            if (courier.getPosition().equals(order.getTo())) {
                                player.setScore(player.getScore() + order.getValue());
                                state.getOrders().remove(order);
                            } else {
                                //Should not happen anymore
                                order.setStatus(Order.STATUS_TODO);
                                order.setFrom(new Position(courier.getPosition()));
                                order.setIdCourier(0);
                            }
                            break;
                        }
                    }
                } else if (Action.PICKUP_ORDER.equals(action.getAction())) {
                    for (Order order : new ArrayList<Order>(state.getOrders())) {
                        if (order.getId() == action.getIdOrder()) {
                            if (courier.getPosition().equals(order.getFrom())) {
                                order.setStatus(Order.STATUS_ONGOING);
                                order.setIdCourier(courier.getId());
                                break;
                            } else {
                                throw new RuntimeException("Incorrect order pickup");
                            }
                        }
                    }
                } else if (Action.KICK_PLAYER.equals(action.getAction())){
                    for(Order order : state.getOrders()){
                        if(order.getId() == action.getIdOtherCourier()){
                            order.setStatus(Order.STATUS_TODO);
                            //Assume same position as current courier
                            order.setFrom(courier.getPosition());
                            order.setId(0);
                            playerStunned.put(action.getIdOtherCourier(),2);
                        }
                    }
                } else if(Action.UPGRADE_FIGHTER.equals(action.getAction())){
                    player.setScore(player.getScore()-PRICE_UPGRADE_KICK);
                    courier.setFighter(true);
                } else if(Action.UPGRADE_ELECTRIC.equals(action.getAction())){
                    player.setScore(player.getScore()-PRICE_UPGRADE_ELECTRIC);
                    courier.setElectric(true);
                }
                return;
            }
        }
    }

    private List<Action> computePossibleActions(State state, Courier courier) {
        List<Action> results = new ArrayList<Action>();

                int curX = courier.getPosition().getX();
                int curY = courier.getPosition().getY();
                Integer[][] roads = city.getRoads();
                if (roads[curX + 1][curY] != City.EMPTY) {
                    results.add(new Action(Action.MOVE_RIGHT, courier.getId(), state.getTurn()));
                    if(courier.isElectric()) {
                        if (roads[curX + 2][curY] != City.EMPTY) {
                            results.add(new Action(Action.MOVE_RIGHT_DOUBLE, courier.getId(), state.getTurn()));
                        }
                    }
                }
                if (roads[curX - 1][curY] != City.EMPTY) {
                    results.add(new Action(Action.MOVE_LEFT, courier.getId(), state.getTurn()));
                    if(courier.isElectric()) {
                        if (roads[curX - 2][curY] != City.EMPTY) {
                            results.add(new Action(Action.MOVE_LEFT_DOUBLE, courier.getId(), state.getTurn()));
                        }
                    }
                }
                if (roads[curX][curY + 1] != City.EMPTY) {
                    results.add(new Action(Action.MOVE_DOWN, courier.getId(), state.getTurn()));
                    if(courier.isElectric()) {
                        if (roads[curX][curY + 2] != City.EMPTY) {
                            results.add(new Action(Action.MOVE_DOWN_DOUBLE, courier.getId(), state.getTurn()));
                        }
                    }
                }
                if (roads[curX][curY - 1] != City.EMPTY) {
                    results.add(new Action(Action.MOVE_UP, courier.getId(), state.getTurn()));
                    if(courier.isElectric()) {
                        if (roads[curX][curY - 2] != City.EMPTY) {
                            results.add(new Action(Action.MOVE_UP_DOUBLE, courier.getId(), state.getTurn()));
                        }
                    }
                }
                Order currentOrder = getOrderFromCourier(courier, state.getOrders());
                if (currentOrder != null) {
                    if(currentOrder.getTo().equals(courier.getPosition())){
                        results.add(new Action(Action.DROP_ORDER, courier.getId(), currentOrder.getId(), state.getTurn()));
                    }
                } else {
                    for (Order order : state.getOrders()) {
                        if (order.getStatus() == Order.STATUS_TODO) {
                            if (order.getFrom().getX() == curX && order.getFrom().getY() == curY) {
                                //if no current action
                                if (getOrderFromCourier(courier, state.getOrders()) == null) {
                                    results.add(new Action(Action.PICKUP_ORDER, courier.getId(), order.getId(), state.getTurn()));
                                }
                            }
                        }
                    }
                }
                //Detect kick
                if(KICK_ENABLED ) {
                    if(courier.isFighter() && currentOrder == null){
                        //Detect other player
                        for (Courier otherCourier : state.getCouriers()) {
                            //skip himself
                            if (otherCourier.getId().equals(courier.getId()))
                                continue;
                            if (otherCourier.getPosition().equals(courier.getPosition())) {
                                for (Order order : state.getOrders()) {
                                    if (order.getId() == otherCourier.getId()) {
                                        results.add(new Action(Action.KICK_PLAYER, courier.getId(), null, otherCourier.getId(), state.getTurn()));
                                    }
                                }
                            }
                        }
                    }
                    //Detect upgrade
                    if(courier.isFighter()==false) {
                        if (courier.getScore() >= PRICE_UPGRADE_KICK) {
                            results.add(new Action(Action.UPGRADE_FIGHTER,courier.getId(),state.getTurn()));
                        }
                    }
                }

                if(ELECTRIC_ENABLED){
                    if(courier.isElectric()==false) {
                        if (courier.getScore() >= PRICE_UPGRADE_ELECTRIC) {
                            results.add(new Action(Action.UPGRADE_ELECTRIC,courier.getId(),state.getTurn()));
                        }
                    }
                }
        return results;
    }

    private Order getOrderFromCourier(Courier courier, List<Order> orders) {
        for (Order order : orders) {
            if (order.getIdCourier() == courier.getId())
                return order;
        }
        return null;
    }

    private void cleanOldPlayers() {
        List<PlayerClient> playerClientList = new ArrayList<PlayerClient>(players.values());
        for (PlayerClient playerClient : playerClientList) {
            if (state.getTurn() - playerClient.getLastTurnPlayed() > 50) {
                playerClient.getClient().disconnect();
                players.remove(playerClient.getCourier().getName());

                Courier playerCourier = null;
                Order playerOrder = null;

                for (Courier courier : state.getCouriers()) {
                    if (courier.getId().equals(playerClient.getCourier().getId())) {
                        for (Order order : state.getOrders()) {
                            if (order.getIdCourier() == courier.getId()) {
                                playerOrder=order;
                                break;
                            }
                        }
                        playerCourier = courier;
                        break;
                    }
                }
                if(playerCourier != null)
                    state.getCouriers().remove(playerCourier);
                if(playerOrder !=null)
                    state.getOrders().remove(playerOrder);
            }
        }
    }

    private void decreaseOrderValues(){
        for(Order order : new ArrayList<>(state.getOrders())){
            order.setValue(order.getValue()-1);
            if(order.getValue()<=0){
                state.getOrders().remove(order);
            }
        }
    }

    public void addPlayer(PlayerClient playerClient){
        playerClient.setLastTurnPlayed(state.getTurn());
        PlayerClient old = players.put(playerClient.getCourier().getName(), playerClient);

        if (old == null) {
            Courier courier = new Courier();
            courier.setName(playerClient.getCourier().getName());
            courier.setScore(0);
            int idPlayer = playerIdGenerator.incrementAndGet();
            courier.setId(idPlayer);
            playerClient.getCourier().setId(idPlayer);
            courier.setPosition(getRandomRoadPosition());
            state.getCouriers().add(courier);
            playerClient.setCourier(courier);
        } else {
            playerClient.setCourier(old.getCourier());
            old.getClient().disconnect();
        }
    }

    private void readMapFromFile(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(this.getClass().getResourceAsStream(filename));
        //scanner.useDelimiter("\\D");
        int width =scanner.nextInt();
        int height = scanner.nextInt();
        Integer[][] map = new Integer[width][height];
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                switch(scanner.nextInt()){
                    case -1:
                        map[x][y] = City.EMPTY;
                        break;
                    case 1:
                        map[x][y] = City.ROAD;
                        break;
                    case 0:
                        map[x][y] = City.RESTAURANT;
                        break;
                }
            }
        }
        city.setRoads(map);
    }

    private void readMapFromFile2(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(this.getClass().getResourceAsStream(filename));
        int width =scanner.nextInt();
        int height = scanner.nextInt();
        Integer[][] map = new Integer[width][height];
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                map[x][y] = scanner.nextInt();
                if(map[x][y] == City.RESTAURANT){
                    restaurantsPosition.add(new Position(x,y));
                }
            }
        }
        city.setRoads(map);
    }


    private void createSampleMap(int mapSize) {
        Integer[][] map = new Integer[mapSize][mapSize];
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (x == 0)
                    map[x][y] = 0;
                else if (y == 0)
                    map[x][y] = 0;
                else if (x == map.length - 1)
                    map[x][y] = 0;
                else if (y == map[0].length - 1)
                    map[x][y] = 0;
                else if (x % 2 == 1)
                    map[x][y] = 1;
                else if (y % 3 == 1)
                    map[x][y] = 1;
                else
                    map[x][y] = 0;
            }
        }
        city.setRoads(map);
    }

    public Position getRandomRoadPosition() {
        while (true) {
            int rx = (int) Math.floor(city.getRoads().length * Math.random());
            int ry = (int) Math.floor(city.getRoads()[0].length * Math.random());
            if (city.getRoads()[rx][ry] == City.ROAD) {
                return new Position(rx, ry);
            }
        }
    }

    public void start() throws InterruptedException {
        while(true) {
            newGame();
            Thread.sleep(15000);
            List<PlayerClient> players = new ArrayList(this.players.values());
            for (final PlayerClient client : players) {
                client.getCourier().setScore(0);
                client.getCourier().setFighter(false);
                client.getCourier().setElectric(false);
            }

            while (state.getTurn() < MAX_TURN) {
                try {
                    nextTurn();
                } catch (Exception e) {
                    log.error("Error playing turn", e);
                }
                Thread.sleep(SLEEP_TIME);
            }
        }
    }

    private List<SocketIOClient> controllers = new ArrayList<>();
    public void addController(SocketIOClient client) {
        controllers.add(client);
    }

    private void notifyControllers(State state) {
        for (final SocketIOClient controller : controllers) {
            controller.sendEvent("turn", state);
        }
    }

}
