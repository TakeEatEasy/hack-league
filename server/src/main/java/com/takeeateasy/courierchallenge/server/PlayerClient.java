package com.takeeateasy.courierchallenge.server;

import com.corundumstudio.socketio.SocketIOClient;
import com.takeeateasy.courierchallenge.api.Courier;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerClient {
    private Courier courier;
    private SocketIOClient client;

    private int lastTurnPlayed = 0;
    private boolean busy = false;

    public PlayerClient(Courier player, SocketIOClient client) {
        this.courier = player;
        this.client = client;
    }
}
