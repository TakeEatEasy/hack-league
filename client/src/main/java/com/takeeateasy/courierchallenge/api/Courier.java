package com.takeeateasy.courierchallenge.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Courier {
    private Integer id;
    private Position position;
    private String name;
    private Integer score;

    private boolean fighter = false;
    private boolean electric = false;

    public Courier(){}
    public Courier(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        this.setName(jsonObject.getString("name"));
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isFighter() {
        return fighter;
    }

    public void setFighter(boolean canKick) {
        this.fighter = canKick;
    }

    public boolean isElectric() {
        return electric;
    }

    public void setElectric(boolean electric) {
        this.electric = electric;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
