package org.appland.settlers.rest.resource;

import org.appland.settlers.maps.MapFile;
import org.appland.settlers.model.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GamePlaceholder {
    private int height;
    private int width;
    private final Collection<Player> players;
    private MapFile mapFile;
    private String name;

    GamePlaceholder() {
        players = new ArrayList<>();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    void setPlayers(List<Player> players) {
        this.players.addAll(players);
    }

    Collection<Player> getPlayers() {
        return players;
    }

    int getHeight() {
        return height;
    }

    int getWidth() {
        return width;
    }

    void addPlayer(Player player) {
        players.add(player);
    }

    void setMap(MapFile updatedMapFile) {
        width = updatedMapFile.getWidth();
        height = updatedMapFile.getHeight();

        mapFile = updatedMapFile;
    }

    MapFile getMapFile() {
        return mapFile;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    boolean isNameSet() {
        return name != null;
    }
}
