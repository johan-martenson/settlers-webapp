package org.appland.settlers.rest;

import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.rest.resource.GameResource;
import org.appland.settlers.utils.Stats;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameTicker {

    private static final int COMPUTER_PLAYER_FREQUENCY = 100;
    private static final String FULL_TICK_TIME = "Time for full tick";
    private static final String MAP_TICK_TIME = "Time for map.stepTime()";
    private static final String COMPUTER_PLAYERS_TICK_TIME = "Time for running all computer players";

    private final ScheduledExecutorService scheduler;
    private final Set<GameResource> games;
    private final Stats stats;
    ScheduledFuture<?> handle;
    private int counter;
    private long highestTimeOfMapStepTime;
    private long highestTimeOfTick;

    GameTicker() {
        games = new HashSet<>();

        scheduler = Executors.newScheduledThreadPool(2);

        counter = 0;

        highestTimeOfMapStepTime = 0;
        highestTimeOfTick = 0;

        stats = new Stats();

        stats.addVariable(FULL_TICK_TIME);
        stats.addVariable(MAP_TICK_TIME);
        stats.addVariable(COMPUTER_PLAYERS_TICK_TIME);
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
                    long timestampStartingTick;
                    long timestampAfterMapStepTime;
                    long timestampAtEndOfTick;

                    synchronized (map) {
                        timestampStartingTick = getTimestamp();

                        map.stepTime();

                        timestampAfterMapStepTime = getTimestamp();

                        if (runComputers) {
                            for (ComputerPlayer computerPlayer : computerPlayers) {
                                synchronized (map) {
                                    computerPlayer.turn();
                                }
                            }
                        }

                        timestampAtEndOfTick = getTimestamp();
                    }

                    long timeOfMapStepTime = timestampAfterMapStepTime - timestampStartingTick;
                    long timeOfTick = timestampAtEndOfTick - timestampStartingTick;
                    long timeOfComputerPlayers = timestampAtEndOfTick - timestampAfterMapStepTime;

                    stats.reportVariableValue(FULL_TICK_TIME, timeOfTick);
                    stats.reportVariableValue(MAP_TICK_TIME, timeOfMapStepTime);
                    stats.reportVariableValue(COMPUTER_PLAYERS_TICK_TIME, timeOfComputerPlayers);

                    if (stats.isVariableLatestValueHighest(MAP_TICK_TIME)) {
                        System.out.println("\nNew highest time for map.stepTime(): " +
                                stats.getHighestValueForVariable(MAP_TICK_TIME) +
                                " (ms)");

                        System.out.println("Average: " + stats.getAverageForVariable(MAP_TICK_TIME) + " (ms)");

                        Stats mapStats = map.getStats();

                        for (String name : mapStats.getVariables()) {
                            System.out.println();
                            System.out.println("  " + name + ":");
                            System.out.println("   -- Latest: " + mapStats.getLatestValueForVariable(name));
                            System.out.println("   -- Average: " + mapStats.getAverageForVariable(name));
                            System.out.println("   -- Highest: " + mapStats.getHighestValueForVariable(name));
                            System.out.println("   -- Lowest: " + mapStats.getLatestValueForVariable(name));
                        }
                    }

                    if (stats.isVariableLatestValueHighest(FULL_TICK_TIME)) {
                        System.out.println("\nNew highest time for full tick: " +
                                stats.getHighestValueForVariable(FULL_TICK_TIME) +
                                " (ms)");

                        System.out.println("Average: " + stats.getAverageForVariable(FULL_TICK_TIME) + " (ms)");
                    }

                    if (stats.isVariableLatestValueHighest(COMPUTER_PLAYERS_TICK_TIME)) {
                        System.out.println("\nNew highest time for computer players: " +
                                stats.getHighestValueForVariable(COMPUTER_PLAYERS_TICK_TIME) +
                                " (ms)");

                        System.out.println("Average: " + stats.getAverageForVariable(COMPUTER_PLAYERS_TICK_TIME) + " (ms)");
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

    private long getTimestamp() {
        return (new Date()).getTime();
    }

    public void startGame(GameResource gameResource) {
        games.add(gameResource);
    }
}
