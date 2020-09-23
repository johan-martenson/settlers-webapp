package org.appland.settlers.rest;

import org.appland.settlers.computer.CompositePlayer;
import org.appland.settlers.computer.ComputerPlayer;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.Scout;
import org.appland.settlers.rest.resource.GameResource;
import org.appland.settlers.utils.CumulativeDuration;
import org.appland.settlers.utils.Group;
import org.appland.settlers.utils.Stats;
import org.appland.settlers.utils.Variable;

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
    private static final String FULL_TICK_TIME = "GameTicker.tick.total";
    private static final String MAP_TICK_TIME = "GameTicker.tick.Map.stepTime";
    private static final String COMPUTER_PLAYERS_TICK_TIME = "GameTicker.tick.ComputerPlayer.turn";

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

        stats.setUpperThreshold(FULL_TICK_TIME, 150);
    }

    void deactivate() {
        scheduler.shutdown();
    }

    void activate() {
        handle = scheduler.scheduleAtFixedRate(() -> {
            boolean runComputers = false;

            Group group = null;
            CumulativeDuration duration = null;
            Variable computerPlayerTurns = null;

            try {
                group = stats.createVariableGroupIfAbsent("GameTickGroup");
                duration = stats.measureCumulativeDuration("GameTicker.tick", group);
                computerPlayerTurns = stats.addIncrementingVariableIfAbsent("ComputerPlayerTurns");
            } catch (Throwable e) {
                System.out.println(e);
                e.printStackTrace();
            }

            if (counter == COMPUTER_PLAYER_FREQUENCY) {
                runComputers = true;
            }

            for (GameResource game : games) {

                GameMap map = game.getMap();

                List<ComputerPlayer> computerPlayers = game.getComputerPlayers();

                synchronized (map) {

                    try {
                        map.stepTime();
                    } catch (Throwable e) {
                        System.out.println("Exception during game loop: " + e);
                        e.printStackTrace();
                    }

                    duration.after("Map.stepTime");

                    if (runComputers) {
                        for (ComputerPlayer computerPlayer : computerPlayers) {
                            synchronized (map) {

                                try {
                                    computerPlayer.turn();

                                    Stats computerPlayerStats = ((CompositePlayer)computerPlayer).getStats();

                                    computerPlayerTurns.reportValue(1);

                                    Variable totalTurn = computerPlayerStats.getVariable("CompositePlayer.turn.total");

                                    if (totalTurn.isLatestValueHighest()) {
                                        stats.printVariablesAsTable();
                                        computerPlayerStats.printVariablesAsTable();
                                    }

                                } catch (Throwable e) {
                                    System.out.println("Exception during computer player turn");
                                    e.printStackTrace();
                                }
                            }

                            duration.after("ComputerPlayer.turn");
                        }
                    }
                }

                try {
                    duration.report();

                    group.collectionPeriodDone();

                    boolean printStats = false;

                    Variable mapStepTime = stats.getVariable("GameTicker.tick.Map.stepTime");
                    Variable computerPlayerTurn = stats.getVariable("GameTicker.tick.ComputerPlayer.turn");
                    Variable fullTick = stats.getVariable("GameTicker.tick.total");

                    if (mapStepTime.isLatestValueHighest()) {
                        System.out.println("\nNew highest time for map.stepTime(): " + mapStepTime.getHighestValue() + " (ms)");

                        System.out.println("Upper threshold is: " + mapStepTime.getUpperThreshold());

                        printStats = true;

                    }

                    if (runComputers && computerPlayerTurn != null && computerPlayerTurn.isLatestValueHighest()) {
                        System.out.println("\nNew highest time for computer players: " + computerPlayerTurn.getHighestValue() + " (ms)");

                        System.out.println("Average: " + computerPlayerTurn.getAverage() + " (ms)");



                        printStats = true;
                    }

                    if (fullTick.isLatestValueHighest()) {
                        System.out.println("\nNew highest time for full tick: " + fullTick.getHighestValue() + " (ms)");

                        System.out.println("Average: " + fullTick.getAverage() + " (ms)");

                        printStats = true;
                    }

                    if (fullTick.isUpperThresholdExceeded()) {
                        System.out.println("\nMap step time exceeded threshold");

                        printStats = true;
                    }

                    if (printStats) {
                        stats.printVariablesAsTable();
                        map.getStats().printVariablesAsTable();
                    }
                } catch (Throwable t) {
                    System.out.println(t);
                    t.printStackTrace();
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

        GameMap map = gameResource.getMap();

        for (Building building : map.getBuildings()) {
            if (building instanceof Headquarter) {
                Headquarter headquarter = (Headquarter) building;

                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
                headquarter.depositWorker(new Scout(headquarter.getPlayer(), map));
            }
        }

        Stats stats = map.getStats();
    }
}
