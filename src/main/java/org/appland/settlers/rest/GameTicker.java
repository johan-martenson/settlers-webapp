package org.appland.settlers.rest;

import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.rest.resource.GameResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameTicker {

    private static final int COMPUTER_PLAYER_FREQUENCY = 100;

    private final ScheduledExecutorService scheduler;
    private final Set<GameResource> games;
    ScheduledFuture<?> handle;
    private int counter;

    GameTicker() {
        games = new HashSet<>();

        scheduler = Executors.newScheduledThreadPool(2);

        counter = 0;
    }

    void deactivate() {
        scheduler.shutdown();
    }

    void activate() {
        handle = scheduler.scheduleAtFixedRate(() -> {
            boolean runComputers = false;

            if (counter == COMPUTER_PLAYER_FREQUENCY) {
                runComputers = true;
            }

            for (GameResource game : games) {

                GameMap map = game.getMap();

                List<ComputerPlayer> computerPlayers = game.getComputerPlayers();

                try {
                    synchronized (map) {
                        map.stepTime();


                        if (runComputers) {
                            for (ComputerPlayer computerPlayer : computerPlayers) {
                                synchronized (map) {
                                    computerPlayer.turn();
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }

            if (runComputers) {
                counter = 0;
            } else {
                counter = counter + 1;
            }
        },
        200,200, TimeUnit.MILLISECONDS);
    }

    public void startGame(GameResource gameResource) {
        games.add(gameResource);
    }
}
