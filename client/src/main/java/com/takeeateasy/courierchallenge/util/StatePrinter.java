package com.takeeateasy.courierchallenge.util;

import com.takeeateasy.courierchallenge.api.*;

public class StatePrinter {

    public String printState(State state, City city) {
        StringBuilder result = new StringBuilder();
        result.append("\n");
        for (int y = 0; y < city.getRoads()[0].length; y++) {
            for (int x = 0; x < city.getRoads().length; x++) {
                int val = city.getRoads()[x][y];
                if (val == City.EMPTY) {
                    result.append(" \t");
                } else {
                    boolean printed = false;
                    if(val == City.RESTAURANT){
                        result.append("R");
                        printed = true;
                    }

                    for (Courier courier : state.getCouriers()) {
                        if (courier.getPosition().getY() == y && courier.getPosition().getX() == x) {
                            result.append("C");
                            printed = true;
                        }
                    }
                    for (Order order : state.getOrders()) {
                        if (order.getStatus() == Order.STATUS_TODO) {
                            if (order.getFrom().getY() == y && order.getFrom().getX() == x) {
                                result.append("O");
                                printed = true;
                            }
                        }
                        if (order.getStatus() == Order.STATUS_ONGOING) {
                            if (order.getTo().getY() == y && order.getTo().getX() == x) {
                                result.append("D");
                                printed = true;
                            }
                        }

                    }
                    if (!printed)
                        result.append(". ");
                    result.append("\t");
                }
            }
            result.append("\n");
        }
        return result.toString();
    }
}
