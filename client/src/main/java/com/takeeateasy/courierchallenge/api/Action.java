package com.takeeateasy.courierchallenge.api;

import org.json.JSONObject;

public class  Action {
    //NORMAL ACTIONS
    public static final String MOVE_UP = "MOVE_UP"; // y+1
    public static final String MOVE_DOWN = "MOVE_DOWN"; // y-1
    public static final String MOVE_LEFT = "MOVE_LEFT"; //x-1
    public static final String MOVE_RIGHT = "MOVE_RIGHT"; //x+1

    public static final String DROP_ORDER = "DROP_ORDER";
    public static final String PICKUP_ORDER = "PICKUP_ORDER";

    //ADVANCED ACTIONS
    public static final String KICK_PLAYER = "KICK_PLAYER";
    public static final String UPGRADE_FIGHTER = "UPGRADE_FIGHTER";

    public static final String UPGRADE_ELECTRIC = "UPGRADE_ELECTRIC";
    public static final String MOVE_UP_DOUBLE = "MOVE_UP_DOUBLE"; // y+2
    public static final String MOVE_DOWN_DOUBLE = "MOVE_DOWN_DOUBLE"; // y-2
    public static final String MOVE_LEFT_DOUBLE = "MOVE_LEFT_DOUBLE"; //x-2
    public static final String MOVE_RIGHT_DOUBLE = "MOVE_RIGHT_DOUBLE"; //x+2


    private String action; //Required
    private Integer idCourier; //Required
    private Integer idOrder; //Optional
    private Integer idOtherCourier; //Optional

    private int turn;

    public Action(){
    }

    public Action(String action, Integer idCourier,Integer idOrder,Integer idOtherCourier,int turn) {
        this.action = action;
        this.idCourier = idCourier;
        this.turn = turn;
        this.idOrder = idOrder;
        this.idOtherCourier = idOtherCourier;
    }


    public Action(String action, Integer idCourier, int turn) {
        this.action = action;
        this.idCourier = idCourier;
        this.turn = turn;
    }

    public Action(String action, Integer idCourier, Integer idOrder, int turn) {
        this.action = action;
        this.idCourier = idCourier;
        this.idOrder = idOrder;
        this.turn = turn;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Integer idOrder) {
        this.idOrder = idOrder;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    @Override
    public String toString() {
        return new JSONObject(this).toString();
    }

    public Integer getIdCourier() {
        return idCourier;
    }

    public void setIdCourier(Integer idCourier) {
        this.idCourier = idCourier;
    }

    public Integer getIdOtherCourier() {
        return idOtherCourier;
    }

    public void setIdOtherCourier(Integer idOtherCourier) {
        this.idOtherCourier = idOtherCourier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;

        Action action1 = (Action) o;

        if (turn != action1.turn) return false;
        if (action != null ? !action.equals(action1.action) : action1.action != null) return false;
        if (idCourier != null ? !idCourier.equals(action1.idCourier) : action1.idCourier != null) return false;
        if (idOrder != null ? !idOrder.equals(action1.idOrder) : action1.idOrder != null) return false;
        return !(idOtherCourier != null ? !idOtherCourier.equals(action1.idOtherCourier) : action1.idOtherCourier != null);

    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (idCourier != null ? idCourier.hashCode() : 0);
        result = 31 * result + (idOrder != null ? idOrder.hashCode() : 0);
        result = 31 * result + (idOtherCourier != null ? idOtherCourier.hashCode() : 0);
        result = 31 * result + turn;
        return result;
    }
}
