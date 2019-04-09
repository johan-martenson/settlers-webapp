package org.appland.settlers.rest;

import org.appland.settlers.model.GameMap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameTicker {
    private final ScheduledExecutorService scheduler;
    private final Set<GameMap> games;

    GameTicker() {
        games = new HashSet<>();

        scheduler = Executors.newScheduledThreadPool(2);
    }

    void deactivate() {
        scheduler.shutdown();
    }

    void activate() {
        scheduler.scheduleAtFixedRate(() -> {
            for (GameMap map : games) {
                try {
                    synchronized (map) {
                        map.stepTime();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        },
                200,200, TimeUnit.MILLISECONDS);
    }

    public void startGame(GameMap map) {
        games.add(map);
    }
}
