package org.appland.settlers.rest.resource;

import org.appland.settlers.model.GameChangesList;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.PlayerGameViewMonitor;
import org.json.simple.JSONObject;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/ws/monitor/games/{gameId}/players/{playerId}")

public class WebsocketMonitor implements PlayerGameViewMonitor {

    private final Map<Player, Session> sessions;
    private final Utils utils;

    private IdManager idManager = IdManager.idManager;

    public WebsocketMonitor() {
        sessions = new HashMap<>();

        System.out.println("CREATED NEW WEBSOCKET MONITOR");
        utils = new Utils(idManager);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("ON MESSAGE: " + message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("ON CLOSE");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("ON ERROR");
    }

    @OnOpen
    public void onOpen(Session session, @javax.websocket.server.PathParam("gameId") String gameId, @javax.websocket.server.PathParam("playerId") String playerId, EndpointConfig config) {

        System.out.println("Websocket opened");

        /* Subscribe to changes */
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);

        System.out.println("Storing session");
        this.sessions.put(player, session);

        System.out.println("Starting to monitor");
        player.monitorGameView(this);
    }

    @Override
    public void onViewChangesForPlayer(Player player, GameChangesList gameChangesList) {
        JSONObject jsonGameMonitoringEvent = utils.gameMonitoringEventsToJson(gameChangesList);

        sessions.get(player).getAsyncRemote().sendText(jsonGameMonitoringEvent.toJSONString());
    }
}