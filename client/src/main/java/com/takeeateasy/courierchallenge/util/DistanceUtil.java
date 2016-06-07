package com.takeeateasy.courierchallenge.util;

import com.takeeateasy.courierchallenge.api.City;
import com.takeeateasy.courierchallenge.api.Position;

public class DistanceUtil {

    public static Integer getDistance(int x, int y, int x2, int y2, City city) {
        return city.getDistances().get(getKey(x, y, x2, y2));
    }

    public static String getKey(int oX, int oY, int dX, int dY) {
        return "" + oX + "-" + oY + ";" + dX + "-" + dY;
    }

    public static int getDistance(Position p1, Position p2, City city) {
        return getDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY(), city);
    }
}
