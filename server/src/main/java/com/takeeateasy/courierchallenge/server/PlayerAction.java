package com.takeeateasy.courierchallenge.server;

import com.takeeateasy.courierchallenge.api.Action;
import com.takeeateasy.courierchallenge.api.Courier;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerAction {
    private Action action;
    private Courier player;
}
