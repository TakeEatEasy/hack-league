package com.takeeateasy.courierchallenge.api;

import java.util.Map;

public class City {
    public static final int EMPTY = 0;
    public static final int ROAD = 1;
    public static final int RESTAURANT = 2;



    private Integer[][] roads;
    private Map<String, Integer> distances;


    public Integer[][] getRoads() {
        return roads;
    }

    public void setRoads(Integer[][] roads) {
        this.roads = roads;
    }

    public Map<String, Integer> getDistances() {
        return distances;
    }

    public void setDistances(Map<String, Integer> distances) {
        this.distances = distances;
    }
}
