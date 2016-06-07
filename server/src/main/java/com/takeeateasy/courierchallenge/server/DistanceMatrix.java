package com.takeeateasy.courierchallenge.server;

import com.takeeateasy.courierchallenge.api.City;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class DistanceMatrix {

    @Getter
    HashMap<String, Integer> distances;

    public DistanceMatrix(City city) {
        distances = new HashMap<String, Integer>();
        for (int x = 0; x < city.getRoads().length; x++) {
            for (int y = 0; y < city.getRoads()[0].length; y++) {
                if (city.getRoads()[x][y]  != City.EMPTY) {
                    computeDistances(x, y, city.getRoads());
                }
            }
        }

    }

    private void computeDistances(int x, int y, Integer[][] roads) {
        Queue<Item> toDo = new LinkedList<Item>();
        //distances.put(getKey(x,y,x,y),0);
        toDo.add(new Item(0, x, y));

        while (!toDo.isEmpty()) {
            Item current = toDo.remove();
            String key = getKey(x, y, current.getX(), current.getY());
            if (distances.containsKey(key))
                continue;
            distances.put(key, current.cost);

            if (roads[current.getX() + 1][current.getY()] != City.EMPTY) {
                toDo.add(new Item(current.getCost() + 1, current.getX() + 1, current.getY()));
            }
            if (roads[current.getX() - 1][current.getY()] != City.EMPTY) {
                toDo.add(new Item(current.getCost() + 1, current.getX() - 1, current.getY()));
            }
            if (roads[current.getX()][current.getY() + 1]  != City.EMPTY) {
                toDo.add(new Item(current.getCost() + 1, current.getX(), current.getY() + 1));
            }
            if (roads[current.getX()][current.getY() - 1] != City.EMPTY) {
                toDo.add(new Item(current.getCost() + 1, current.getX(), current.getY() - 1));
            }
        }
    }

    public String toString() {
        return "DistanceMatrix(size:" + distances.size() + ")";
    }


    public String getKey(int oX, int oY, int dX, int dY) {
        return "" + oX + "-" + oY + ";" + dX + "-" + dY;
    }

    @Data
    @AllArgsConstructor
    private class Item {
        int cost;
        int x;
        int y;
    }
}
