package com.takeeateasy.courierchallenge.api;

import java.util.ArrayList;
import java.util.List;

public class State {

    private List<Courier> couriers;
    private List<Order> orders;
    private int turn;

    public State() {
        couriers = new ArrayList<>();
        orders = new ArrayList<>();
        turn = 0;
    }

    public List<Courier> getCouriers() {
        return couriers;
    }

    public void setCouriers(List<Courier> couriers) {
        this.couriers = couriers;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

}
