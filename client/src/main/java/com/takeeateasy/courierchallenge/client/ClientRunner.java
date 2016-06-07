package com.takeeateasy.courierchallenge.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeeateasy.courierchallenge.api.Action;
import com.takeeateasy.courierchallenge.api.Courier;
import com.takeeateasy.courierchallenge.api.GameInfo;
import com.takeeateasy.courierchallenge.api.PlayTurn;
import com.takeeateasy.courierchallenge.util.StatePrinter;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

public class ClientRunner{

    Logger log = LoggerFactory.getLogger(this.getClass());

    private Courier courier;
    private GameInfo gameInfo;
    private SimpleIA ia;

    public ClientRunner(String name, String serverUrl) {
        try {

            courier = new Courier();
            courier.setName(name);

            final Socket socket = IO.socket(serverUrl);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                public void call(final Object... args) {
                    log.info("EVENT_CONNECT {}", args);
                    try {
                        socket.emit("register", new Object[]{new ObjectMapper().writeValueAsString(courier)}, new Ack() {
                            public void call(Object... objects) {
                                log.info("Received Ack: {}", objects);
                                try {
                                    gameInfo = new ObjectMapper().readValue(objects[0].toString(), GameInfo.class);
                                    ia = new SimpleIA(gameInfo);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                }
            }).on("turn", new Emitter.Listener() {
                public void call(Object... args) {
                    try {
                        log.info("ON TURN {}", args);
                        PlayTurn turn = new ObjectMapper().readValue(args[0].toString(), PlayTurn.class);
                        Ack ack = (Ack) args[args.length - 1];

                        log.info("{}", new StatePrinter().printState(turn.getState(), gameInfo.getCity()));
                        int i = 0;
                        for (Action action : turn.getPossibleActions()) {
                            log.info("Action : {} {}", i++, action);
                        }

                        Action action = ia.playTurn(turn);

                        log.info("Choosed action {}",action);
                        if(action == null)
                            action = turn.getPossibleActions().get(0);
                        ack.call(new JSONObject(action));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                public void call(Object... args) {
                    log.info("EVENT_DISCONNECT {}", args);
                    socket.close();
                }

            });
            socket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 2) {
            System.out.println("Missing parameters <name> <url>");
            System.exit(0);
        }
        new ClientRunner(args[0], args[1]);
    }
}
