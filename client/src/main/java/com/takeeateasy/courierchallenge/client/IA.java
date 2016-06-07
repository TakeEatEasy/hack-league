package com.takeeateasy.courierchallenge.client;

import com.takeeateasy.courierchallenge.api.Action;
import com.takeeateasy.courierchallenge.api.PlayTurn;

public interface IA {
    Action playTurn(PlayTurn turn);
}
