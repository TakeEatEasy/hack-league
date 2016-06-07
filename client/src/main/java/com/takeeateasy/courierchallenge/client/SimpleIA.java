package com.takeeateasy.courierchallenge.client;

import com.takeeateasy.courierchallenge.api.*;
import com.takeeateasy.courierchallenge.util.DistanceUtil;

import java.util.List;

public class SimpleIA implements  IA {

    private GameInfo gameInfo;

    public SimpleIA(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

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

                return getBestDirection(turn.getPossibleActions(), courier.getPosition(), new Position(2,1));
            }
        }
        return null;
    }




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
