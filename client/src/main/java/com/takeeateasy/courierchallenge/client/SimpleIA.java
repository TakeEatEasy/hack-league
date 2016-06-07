package com.takeeateasy.courierchallenge.client;

import com.takeeateasy.courierchallenge.api.*;
import com.takeeateasy.courierchallenge.util.DistanceUtil;

import java.util.List;

public class SimpleIA {

    private GameInfo gameInfo;

    /**
     * This method is called at each new game
     * @param gameInfo
     */
    public SimpleIA(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }


    /**
     * This method is called at each "turn"
     * @param turn contains turn's information (positions, order etc.)
     * @return the action you want to play this turn (must be from the  turn.possibleActions  list).
     */
    public Action playTurn(PlayTurn turn) {

        for (Courier courier : turn.getState().getCouriers()) {
            if (courier.getId() == turn.getIdCourier()) {
                Order currentOrder = null;
                for (Order order : turn.getState().getOrders()) {
                    if (courier.getId().equals(order.getIdCourier())) {
                        currentOrder = order;
                        break;
                    }
                }

                //You should implement the game logic here!

                //Sample client response:  go to the 2,1 position on the map
                return getBestDirection(turn.getPossibleActions(), courier.getPosition(), new Position(2,1));
            }
        }
        return null;
    }


    /**
     * This method returns the best action (MOVE_UP, MOVE_DOWN, MOVE_LEFT or MOVE_RIGHT) to go from the "FROM" position to the "TO" position using the available possible actions
     * @param possibleActions
     * @param from
     * @param to
     * @return the best
     */
    private Action getBestDirection(List<Action> possibleActions, Position from, Position to) {
        Action bestAction = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Action action : possibleActions) {
            if (action.getAction().startsWith("MOVE")) {
                Position newPosition = applyMoveAction(from, action);
                int distance = DistanceUtil.getDistance(newPosition, to, gameInfo.getCity());
                if (distance < bestDistance) {
                    bestAction = action;
                    bestDistance = distance;
                }
            }
        }
        return bestAction;
    }

    /**
     * THis method returns a new position with the "Action" applied to the "from" position
     * @param from
     * @param action
     * @return a new Position object
     */
    private Position applyMoveAction(Position from, Action action) {
        switch (action.getAction()) {
            case Action.MOVE_DOWN:
                return new Position(from.getX(), from.getY() + 1);
            case Action.MOVE_UP:
                return new Position(from.getX(), from.getY() - 1);
            case Action.MOVE_RIGHT:
                return new Position(from.getX() + 1, from.getY());
            case Action.MOVE_LEFT:
                return new Position(from.getX() - 1, from.getY());
            default:
                throw new IllegalArgumentException("Invalid action to apply for move " + action.getAction());
        }
    }
}
