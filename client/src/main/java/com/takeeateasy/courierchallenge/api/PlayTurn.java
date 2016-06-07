package com.takeeateasy.courierchallenge.api;

import java.util.List;

public class PlayTurn {
    private State state;
    private List<Action> possibleActions;
    private int idCourier;
    private int score;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<Action> getPossibleActions() {
        return possibleActions;
    }

    public void setPossibleActions(List<Action> possibleActions) {
        this.possibleActions = possibleActions;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getIdCourier() {
        return idCourier;
    }

    public void setIdCourier(int idCourier) {
        this.idCourier = idCourier;
    }
}
