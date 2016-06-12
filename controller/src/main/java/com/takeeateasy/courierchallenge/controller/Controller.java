package com.takeeateasy.courierchallenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeeateasy.courierchallenge.api.*;
import com.takeeateasy.courierchallenge.util.StatePrinter;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Controller extends Application {
    Logger log = LoggerFactory.getLogger(this.getClass());
    public static String serverUrl = "";

    private GameInfo gameInfo;
    VBox scorePane;
    StackPane mapStackPane;
    GridPane mapPane;
    GridPane courierOrderPane;
    Text turnText;

    Stack<Paint> stack = new Stack();
    Map<Integer,Paint> colors = new HashMap<>();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing parameters <url>");
            System.exit(0);
        }
        serverUrl = args[0];
        launch(args);
    }




    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hack League - Take Eat Easy");

        StackPane root = new StackPane();
        BorderPane main = new BorderPane();

        scorePane = getScorePanel();
        mapStackPane = getMapNode();

        main.setRight(scorePane);
        main.setCenter(mapStackPane);

        Scene scene = new Scene(main, 800,600);
        primaryStage.setScene(scene);
        primaryStage.show();


        try {
            final Socket socket = IO.socket(serverUrl);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                public void call(final Object... args) {
                    log.info("EVENT_CONNECT {}", args);
                    try {
                        socket.emit("controller", new Ack() {
                            public void call(Object... objects) {
                                log.info("Received Ack: {}");
                                try {
                                    gameInfo = new ObjectMapper().readValue(objects[0].toString(), GameInfo.class);

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() { displayMap(gameInfo);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
            }).on("turn", new Emitter.Listener() {
                public void call(Object... args) {
                    try {
                        log.info("ON TURN {}", args);
                        State state = new ObjectMapper().readValue(args[0].toString(), State.class);
                        log.info("{}", new StatePrinter().printState(state, gameInfo.getCity()));

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                updateScoring(state);
                                displayCouriers(state);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                public void call(Object... args) {
                    log.info("EVENT_DISCONNECT {}", args);
                    socket.close();
                }

            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void displayMap(GameInfo gameInfo) {
        mapPane.getChildren().clear();
        Integer[][] roads = gameInfo.getCity().getRoads();

        for (int y = 0; y < roads[0].length; y++) {
            for (int x = 0; x < roads.length; x++) {
                int val = roads[x][y];
                if (val == City.EMPTY) {
                    mapPane.add(getEmptyBox(), x, y);
                } else if (val == City.RESTAURANT) {
                    mapPane.add(getRestaurantBox(), x, y);
                } else if (val == City.ROAD) {
                    mapPane.add(getRoadBox(), x, y);
                }
                FlowPane pane = new FlowPane();
                pane.setHgap(1);
                pane.setVgap(1);
                pane.setMinHeight(rHeight);
                pane.setMinWidth(rWidth);
                pane.setMaxHeight(rHeight);
                pane.setMaxWidth(rWidth);
                courierOrderPane.add(pane,x,y);
            }
        }
    }

    private void displayCouriers(State state){
        for(Node node : courierOrderPane.getChildren()){
            if(node instanceof FlowPane){
                ((FlowPane) node).getChildren().clear();
            }
        }
        for(Courier courier : state.getCouriers()){
            Node node = getNodeByRowColumnIndex(courier.getPosition().getX(),courier.getPosition().getY(),courierOrderPane);
            if(node instanceof Pane){
                Paint color = getCourierPaint(courier);
                boolean hasOrder = false;
                for(Order order : state.getOrders()){
                    if(order.getIdCourier()==courier.getId()){
                        hasOrder=true;
                        break;
                    }
                }
                ((Pane) node).getChildren().add(new Rectangle(hasOrder?10:5,5,color));
            }
        }

        for(Order order : state.getOrders()){
            if(order.getIdCourier()==0) {
                Node node = getNodeByRowColumnIndex(order.getFrom().getX(), order.getFrom().getY(), courierOrderPane);
                if (node instanceof Pane) {
                    ((Pane) node).getChildren().add(new Rectangle(5, 5, Paint.valueOf("purple")));
                }
            }
        }
    }

    public Node getNodeByRowColumnIndex(final int x,final int y,GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();
        for(Node node : childrens) {
            if(gridPane.getRowIndex(node) == y && gridPane.getColumnIndex(node) == x) {
                result = node;
                break;
            }
        }
        return result;
    }


    HashMap<Integer,Text> scoreTexts = new HashMap<>();

    private void updateScoring(State state) {
        //scorePane.getChildren().clear();
        turnText.setText(state.getTurn() +" / "+ gameInfo.getMaxTurns());

        for(Courier courier : state.getCouriers()){
            String text = courier.getName()+(courier.isElectric()?" (E)":"")+(courier.isFighter()?" (F)":"")+":"+courier.getScore();
            if(!scoreTexts.containsKey(courier.getId())){
                Text courierText = new Text(text);
                Paint color = getCourierPaint(courier);
                courierText.setFill(color);
                scorePane.getChildren().add(courierText);
                scoreTexts.put(courier.getId(),courierText);
            }else{

                scoreTexts.get(courier.getId()).setText(text);
            }
        }

        //TODO remove old
    }

    private Paint getCourierPaint(Courier courier) {
        Paint color = colors.get(courier.getId());
        if(color == null){
            color = getColor();
            colors.put(courier.getId(),color);
        }
        return color;
    }

    public VBox getScorePanel() {
        VBox scoreBox = new VBox();
        scoreBox.setMinWidth(150);
        scoreBox.setPadding(new Insets(10));
        scoreBox.setAlignment(Pos.TOP_LEFT);
        scoreBox.setSpacing(8);
        Text turn = new Text("Turn");
        turn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        scoreBox.getChildren().add(turn);

        this.turnText = new Text("");
        scoreBox.getChildren().add(turnText);


        Text title = new Text("Players");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        scoreBox.getChildren().add(title);
        return scoreBox;
    }

    public StackPane getMapNode() {
        StackPane stack = new StackPane();
        mapPane = new GridPane();
        courierOrderPane = new GridPane();

        stack.getChildren().add(mapPane);
        stack.getChildren().add(courierOrderPane);

        return stack;
    }

    int rWidth = 20;
    int rHeight = 20;
    public Node getEmptyBox() {
        return new Rectangle(rWidth,rHeight,Paint.valueOf("lightgreen"));
    }
    public Node getRoadBox() {
        return new Rectangle(rWidth,rHeight,Paint.valueOf("lightgray"));
    }
    public Node getRestaurantBox() {
        return new Rectangle(rWidth,rHeight,Paint.valueOf("lightblue"));
    }

    public Paint getColor(){
        if(stack.isEmpty()){
            fillStack();
        }
        return stack.pop();
    }

    public void fillStack(){
        stack.push(Paint.valueOf("red"));
        stack.push(Paint.valueOf("orange"));
        stack.push(Paint.valueOf("aqua"));
        stack.push(Paint.valueOf("pink"));
        stack.push(Paint.valueOf("blue"));
        stack.push(Paint.valueOf("violet"));
        stack.push(Paint.valueOf("salmon"));
        stack.push(Paint.valueOf("black"));
        stack.push(Paint.valueOf("darkgreen"));
    }
}
