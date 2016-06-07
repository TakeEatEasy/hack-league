package com.takeeateasy.courierchallenge.server;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.takeeateasy.courierchallenge.api.Courier;
import com.takeeateasy.courierchallenge.api.GameInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class ServerRunner {

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(5000);
        final SocketIOServer server = new SocketIOServer(config);
        final Game game = new Game(server);
        game.initGame();

        //Event Listener for IA Clients
        server.addEventListener("register", Courier.class, new DataListener<Courier>() {
            public void onData(SocketIOClient client, Courier courier, AckRequest ackRequest) throws IOException {
                try {
                    //log.debug(courierString);
                    //Courier courier = new Courier();
                    log.info("New player joined: " + courier.getName());
                    PlayerClient playerClient = new PlayerClient(courier, client);
                    game.addPlayer(playerClient);
                    GameInfo gameInfo = new GameInfo();
                    gameInfo.setCity(game.getCity());
                    gameInfo.setMaxTurns(game.MAX_TURN);
                    ackRequest.sendAckData(gameInfo);
                } catch (Exception e) {
                    log.error("Error adding player", e);
                }
            }
        });

        //Event Listener for Controller/Displayer
       server.addEventListener("controller",null,new DataListener<Object>() {
            public void onData(SocketIOClient client, Object data, AckRequest ackRequest) {
                try {
                    log.info("New controller");
                    GameInfo gameInfo = new GameInfo();
                    gameInfo.setCity(game.getCity());
                    gameInfo.setMaxTurns(game.MAX_TURN);
                    ackRequest.sendAckData(gameInfo);
                    game.addController(client);
                } catch(Exception e) {
                    log.error("Error adding controller",e);
                }
            }
        });

        server.start();
        game.start();
    }


}
