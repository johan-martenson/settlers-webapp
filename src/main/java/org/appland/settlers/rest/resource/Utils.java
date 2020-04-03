package org.appland.settlers.rest.resource;

import org.appland.settlers.maps.MapFile;
import org.appland.settlers.maps.MapLoader;
import org.appland.settlers.model.Armory;
import org.appland.settlers.model.Bakery;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Brewery;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.Cargo;
import org.appland.settlers.model.Catapult;
import org.appland.settlers.model.CoalMine;
import org.appland.settlers.model.Crop;
import org.appland.settlers.model.DonkeyFarm;
import org.appland.settlers.model.Farm;
import org.appland.settlers.model.Fishery;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.ForesterHut;
import org.appland.settlers.model.Fortress;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.GoldMine;
import org.appland.settlers.model.GraniteMine;
import org.appland.settlers.model.GuardHouse;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.HunterHut;
import org.appland.settlers.model.IronMine;
import org.appland.settlers.model.IronSmelter;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Military;
import org.appland.settlers.model.Mill;
import org.appland.settlers.model.Mint;
import org.appland.settlers.model.PigFarm;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Quarry;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Sawmill;
import org.appland.settlers.model.Sign;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.SlaughterHouse;
import org.appland.settlers.model.Stone;
import org.appland.settlers.model.Terrain;
import org.appland.settlers.model.Tile;
import org.appland.settlers.model.Tree;
import org.appland.settlers.model.WatchTower;
import org.appland.settlers.model.Well;
import org.appland.settlers.model.Woodcutter;
import org.appland.settlers.model.Worker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Utils {

    private final IdManager idManager;

    Utils(IdManager idManager) {
        this.idManager = idManager;
    }

    JSONArray gamesToJson(List<GameMap> games) {
        JSONArray jsonGames = new JSONArray();

        for (GameMap map : games) {
            JSONObject jsonGame = gameToJson(map);

            jsonGames.add(jsonGame);
        }

        return jsonGames;
    }

    JSONObject gameToJson(GameMap map) {
        JSONObject jsonGame = new JSONObject();

        int id = idManager.getId(map);

        jsonGame.put("id", id);
        jsonGame.put("players", playersToJson(map.getPlayers()));

        /* Set the status to STARTED because this is an instance of GameMap */
        jsonGame.put("status", "STARTED");

        return jsonGame;
    }

    JSONArray playersToJson(Collection<Player> players) {
        JSONArray jsonPlayers = new JSONArray();

        for (Player player : players) {
            JSONObject jsonPlayer = playerToJson(player, idManager.getId(player));

            jsonPlayers.add(jsonPlayer);
        }

        return jsonPlayers;
    }

    private JSONObject playerToJson(Player player, int i) {
        JSONObject jsonPlayer = new JSONObject();

        jsonPlayer.put("name", player.getName());
        jsonPlayer.put("color", colorToHexString(player.getColor()));
        jsonPlayer.put("id", "" + i);

        /* Get the player's "center spot" */
        for (Building building : player.getBuildings()) {
            if (building instanceof Headquarter) {
                jsonPlayer.put("centerPoint", pointToJson(building.getPosition()));

                break;
            }
        }

        /* Fill in the points the player has discovered */
        JSONArray jsonDiscoveredPoints = new JSONArray();
        jsonPlayer.put("discoveredPoints", jsonDiscoveredPoints);

        for (Point point : player.getDiscoveredLand()) {
            jsonDiscoveredPoints.add(pointToJson(point));
        }

        return jsonPlayer;
    }

    JSONArray pointsToJson(List<Point> points) {
        JSONArray jsonPoints = new JSONArray();

        for (Point point : points) {
            jsonPoints.add(pointToJson(point));
        }

        return jsonPoints;
    }

    JSONObject pointToJson(Point point) {
        JSONObject jsonPoint = new JSONObject();

        jsonPoint.put("x", point.x);
        jsonPoint.put("y", point.y);

        return jsonPoint;
    }


    private String colorToHexString(Color c) {

        StringBuilder hex = new StringBuilder(Integer.toHexString(c.getRGB() & 0xffffff));

        while (hex.length() < 6) {
            hex.insert(0, "0");
        }

        hex.insert(0, "#");

        return hex.toString();
    }

    public GameMap jsonToGame(JSONObject jsonGame) throws Exception {
        int width = Integer.parseInt((String) jsonGame.get("width"));
        int height = Integer.parseInt((String) jsonGame.get("height"));
        List<Player> players = jsonToPlayers((JSONArray) jsonGame.get("players"));

        return new GameMap(players, width, height);
    }

    List<Player> jsonToPlayers(JSONArray jsonPlayers) {
        List<Player> players = new ArrayList<>();

        if (jsonPlayers != null) {

            for (Object jsonPlayer : jsonPlayers) {
                players.add(jsonToPlayer((JSONObject) jsonPlayer));
            }
        }

        return players;
    }

    Player jsonToPlayer(JSONObject jsonPlayer) {
        String name = (String) jsonPlayer.get("name");
        Color color = jsonToColor((String) jsonPlayer.get("color"));

        Player player = new Player(name, color);

        return player;
    }

    private Color jsonToColor(String hexColor) {
        return Color.decode(hexColor);
    }

    JSONObject terrainToJson(GameMap map) {
        JSONObject jsonTerrain = new JSONObject();

        JSONArray jsonTrianglesBelow = new JSONArray();
        JSONArray jsonTrianglesBelowRight = new JSONArray();
        JSONArray jsonHeights = new JSONArray();

        jsonTerrain.put("straightBelow", jsonTrianglesBelow);
        jsonTerrain.put("belowToTheRight", jsonTrianglesBelowRight);
        jsonTerrain.put("heights", jsonHeights);

        int start = 1;

        synchronized (map) {
            jsonTerrain.put("width", map.getWidth());
            jsonTerrain.put("height", map.getHeight());

            Terrain terrain = map.getTerrain();

            for (int y = 1; y < map.getHeight(); y++) {
                for (int x = start; x + 1 < map.getWidth(); x += 2) {
                    Point p = new Point(x, y);

                    Tile below = terrain.getTileBelow(p);
                    Tile belowRight = terrain.getTileDownRight(p);

                    jsonTrianglesBelow.add(vegetationToJson(below.getVegetationType()));
                    jsonTrianglesBelowRight.add(vegetationToJson(belowRight.getVegetationType()));
                    jsonHeights.add(map.getHeightAtPoint(p));
                }

                if (start == 1) {
                    start = 2;
                } else {
                    start = 1;
                }
            }
        }

        return jsonTerrain;
    }

    private String vegetationToJson(Tile.Vegetation v) {
        switch (v) {
            case GRASS:
                return "G";
            case WATER:
                return "W";
            case SWAMP:
                return "SW";
            case MOUNTAIN:
                return "M";
            case DEEP_WATER:
                return "DW";
            case SNOW:
                return "SN";
            case LAVA:
                return "L";
            case MOUNTAIN_MEADOW:
                return "MM";
            case STEPPE:
                return "ST";
            case DESERT:
                return "DE";
            case SAVANNAH:
                return "SA";
            default:
                System.out.println("Cannot handle this vegetation " + v);
                System.exit(1);
        }

        return ""; // Should never be reached but the compiler complains
    }

    JSONObject pointToDetailedJson(Point point, Player player, GameMap map) throws Exception {

        JSONObject jsonPointInfo = pointToJson(point);

        if (player.getDiscoveredLand().contains(point)) {

            if (map.isBuildingAtPoint(point)) {
                Building building = map.getBuildingAtPoint(point);
                jsonPointInfo.put("building", houseToJson(building));
                jsonPointInfo.put("is", "building");
                jsonPointInfo.put("buildingId", idManager.getId(building));
            } else if (map.isFlagAtPoint(point)) {
                jsonPointInfo.put("is", "flag");
                jsonPointInfo.put("flagId", idManager.getId(map.getFlagAtPoint(point)));
            } else if (map.isRoadAtPoint(point)) {
                jsonPointInfo.put("is", "road");
                jsonPointInfo.put("roadId", idManager.getId(map.getRoadAtPoint(point)));
            }

            JSONArray canBuild = new JSONArray();
            jsonPointInfo.put("canBuild", canBuild);

            try {
                if (map.isAvailableFlagPoint(player, point)) {
                    canBuild.add("flag");
                }

                if (map.isAvailableMinePoint(player, point)) {
                    canBuild.add("mine");
                }

                Size size = map.isAvailableHousePoint(player, point);

                if (size != null) {
                    if (size == Size.LARGE) {
                        canBuild.add("large");
                        canBuild.add("medium");
                        canBuild.add("small");
                    } else if (size == Size.MEDIUM) {
                        canBuild.add("medium");
                        canBuild.add("small");
                    } else if (size == Size.SMALL) {
                        canBuild.add("small");
                    }
                }

                /* Fill in available connections for a new road */
                JSONArray jsonPossibleConnections = new JSONArray();
                jsonPointInfo.put("possibleRoadConnections", jsonPossibleConnections);
                for (Point possibleConnection : map.getPossibleAdjacentRoadConnectionsIncludingEndpoints(player, point)) {
                    jsonPossibleConnections.add(pointToJson(possibleConnection));
                }
            } catch (Exception ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jsonPointInfo;
    }

    JSONObject houseToJson(Building building) {
        JSONObject jsonHouse = pointToJson(building.getPosition());

        jsonHouse.put("type", building.getClass().getSimpleName());
        jsonHouse.put("playerId", "" + idManager.getId(building.getPlayer()));
        jsonHouse.put("id", idManager.getId(building));

        if (building.canProduce()) {
            JSONArray jsonProduces = new JSONArray();

            jsonHouse.put("productivity", building.getProductivity());
            jsonHouse.put("produces", jsonProduces);

            for (Material material : building.getProducedMaterial()) {
                jsonProduces.add(material.name());
            }
        }

        JSONObject jsonResources = new JSONObject();

        for (Material material : Material.values()) {
            int amountNeeded = building.getTotalAmountNeeded(material);
            int amountAvailable = building.getAmount(material);

            JSONObject jsonResource = new JSONObject();

            if (amountNeeded > 0) {
                jsonResource.put("needs", amountNeeded);
            }

            if (amountAvailable > 0) {
                jsonResource.put("has", amountAvailable);
            }

            if (amountAvailable > 0 || amountNeeded > 0) {
                jsonResources.put(material.name().toLowerCase(), jsonResource);
            }
        }

        jsonHouse.put("resources", jsonResources);

        if (building.underConstruction()) {
            jsonHouse.put("state", "UNFINISHED");
        } else if (building.ready() && !building.occupied()) {
            jsonHouse.put("state", "UNOCCUPIED");
        } else if (building.ready() && building.occupied()) {
            jsonHouse.put("state", "OCCUPIED");
        } else if (building.burningDown()) {
            jsonHouse.put("state", "BURNING");
        } else if (building.destroyed()) {
            jsonHouse.put("state", "DESTROYED");
        }

        if (building.underConstruction()) {
            jsonHouse.put("constructionProgress", building.getConstructionProgress());
        }

        /* Add amount of hosted soldiers for military buildings */
        if (building.isMilitaryBuilding() && building.ready()) {
            JSONArray jsonSoldiers = new JSONArray();

            for (Military military : building.getHostedMilitary()) {
                jsonSoldiers.add(military.getRank().name().toUpperCase());
            }

            jsonHouse.put("soldiers", jsonSoldiers);
            jsonHouse.put("maxSoldiers", building.getMaxHostedMilitary());
            jsonHouse.put("evacuated", building.isEvacuated());
            jsonHouse.put("promotionsEnabled", building.isPromotionEnabled());
        }

        return jsonHouse;
    }


    List<Point> jsonToPoints(JSONArray jsonPoints) {
        List<Point> points = new ArrayList<>();

        for (Object point : jsonPoints) {
            points.add(jsonToPoint((JSONObject) point));
        }

        return points;
    }

    Point jsonToPoint(JSONObject point) {
        int x;
        int y;

        Object xObject = point.get("x");
        Object yObject = point.get("y");

        if (xObject instanceof String) {
            x = Integer.parseInt((String) xObject);
        } else if (xObject instanceof Integer) {
            x = (Integer) xObject;
        } else {
            x = ((Long) xObject).intValue();
        }

        if (yObject instanceof String) {
            y = Integer.parseInt((String) yObject);
        } else if (yObject instanceof Integer) {
            y = (Integer) yObject;
        } else {
            y = ((Long) yObject).intValue();
        }

        return new Point(x, y);
    }

    Building buildingFactory(JSONObject jsonHouse, Player player) {
        Building building = null;
        switch((String)jsonHouse.get("type")) {
            case "ForesterHut":
                building = new ForesterHut(player);
                break;
            case "Woodcutter":
                building = new Woodcutter(player);
                break;
            case "Quarry":
                building = new Quarry(player);
                break;
            case "Headquarter":
                building = new Headquarter(player);
                break;
            case "Sawmill":
                building = new Sawmill(player);
                break;
            case "Farm":
                building = new Farm(player);
                break;
            case "Barracks":
                building = new Barracks(player);
                break;
            case "Well":
                building = new Well(player);
                break;
            case "Mill":
                building = new Mill(player);
                break;
            case "Bakery":
                building = new Bakery(player);
                break;
            case "Fishery":
                building = new Fishery(player);
                break;
            case "GoldMine":
                building = new GoldMine(player);
                break;
            case "IronMine":
                building = new IronMine(player);
                break;
            case "CoalMine":
                building = new CoalMine(player);
                break;
            case "GraniteMine":
                building = new GraniteMine(player);
                break;
            case "PigFarm":
                building = new PigFarm(player);
                break;
            case "Mint":
                building = new Mint(player);
                break;
            case "SlaughterHouse":
                building = new SlaughterHouse(player);
                break;
            case "DonkeyFarm":
                building = new DonkeyFarm(player);
                break;
            case "GuardHouse":
                building = new GuardHouse(player);
                break;
            case "WatchTower":
                building = new WatchTower(player);
                break;
            case "Fortress":
                building = new Fortress(player);
                break;
            case "Catapult":
                building = new Catapult(player);
                break;
            case "HunterHut":
                building = new HunterHut(player);
                break;
            case "IronSmelter":
                building = new IronSmelter(player);
                break;
            case "Armory":
                building = new Armory(player);
                break;
            case "Brewery":
                building = new Brewery(player);
                break;
            default:
                System.out.println("DON'T KNOW HOW TO CREATE BUILDING " + jsonHouse.get("type"));
                System.exit(1);
        }
        return building;
    }

    JSONObject treeToJson(Tree tree) {
        JSONObject jsonTree = pointToJson(tree.getPosition());

        return jsonTree;
    }

    JSONObject stoneToJson(Stone stone) {
        JSONObject jsonStone = pointToJson(stone.getPosition());

        jsonStone.put("amount", stone.getAmount());

        return jsonStone;
    }


    JSONObject workerToJson(Worker worker) {
        JSONObject jsonWorker = pointToJson(worker.getPosition());

        jsonWorker.put("type", worker.getClass().getSimpleName());
        jsonWorker.put("inside", worker.isInsideBuilding());
        jsonWorker.put("betweenPoints", !worker.isExactlyAtPoint());

        if (!worker.isExactlyAtPoint()) {
            jsonWorker.put("previous", pointToJson(worker.getLastPoint()));

            try {
                jsonWorker.put("next", pointToJson(worker.getNextPoint()));

            } catch(Exception e) {
                System.out.println("" + e);
            }

            jsonWorker.put("percentageTraveled", worker.getPercentageOfDistanceTraveled());
            jsonWorker.put("speed", 10); // TODO: dynamically look up speed
        } else {
            jsonWorker.put("percentageTraveled", 0);
        }
        return jsonWorker;
    }

    JSONObject flagToJson(Flag flag) {
        JSONObject jsonFlag = pointToJson(flag.getPosition());

        jsonFlag.put("id", "" + idManager.getId(flag));
        jsonFlag.put("playerId", "" + idManager.getId(flag.getPlayer()));

        return jsonFlag;
    }

    JSONObject roadToJson(Road road) {
        JSONObject jsonRoad = new JSONObject();

        JSONArray jsonPoints = new JSONArray();

        for (Point point : road.getWayPoints()) {
            JSONObject jsonPoint = pointToJson(point);

            jsonPoints.add(jsonPoint);
        }

        jsonRoad.put("points", jsonPoints);

        return jsonRoad;
    }

    JSONObject borderToJson(Player player, int playerId) {

        /* Fill in borders */
        JSONObject jsonBorder = new JSONObject();
        jsonBorder.put("color", colorToHexString(player.getColor()));
        jsonBorder.put("playerId", playerId);

        JSONArray jsonBorderPoints = new JSONArray();
        jsonBorder.put("points", jsonBorderPoints);

        for (Collection<Point> border : player.getBorders()) {
            for (Point point : border) {
                jsonBorderPoints.add(pointToJson(point));
            }
        }

        return jsonBorder;
    }

    JSONObject signToJson(Sign sign) {
        JSONObject jsonSign = new JSONObject();

        if (sign.isEmpty()) {
            jsonSign.put("type", null);
        } else {
            switch (sign.getType()) {
                case GOLD:
                    jsonSign.put("type", "gold");
                    break;
                case IRON:
                    jsonSign.put("type", "iron");
                    break;
                case COAL:
                    jsonSign.put("type", "coal");
                    break;
                case STONE:
                    jsonSign.put("type", "granite");
                    break;
                case WATER:
                    jsonSign.put("type", "water");
                    break;
                default:
                    System.out.println("Cannot have sign of type " + sign.getType());
                    System.exit(1);
            }
        }

        Point point = sign.getPosition();

        jsonSign.put("x", point.x);
        jsonSign.put("y", point.y);

        return jsonSign;
    }

    Object cropToJson(Crop crop) {
        JSONObject jsonCrop = pointToJson(crop.getPosition());

        jsonCrop.put("state", "" + crop.getGrowthState());

        return jsonCrop;
    }

    JSONObject gamePlaceholderToJson(GamePlaceholder gamePlaceholder) {
        JSONObject jsonGamePlaceholder = new JSONObject();

        if (gamePlaceholder.getPlayers() != null) {
            jsonGamePlaceholder.put("players", playersToJson(gamePlaceholder.getPlayers()));
        } else {
            jsonGamePlaceholder.put("players", Collections.EMPTY_LIST);
        }

        MapFile mapFile = gamePlaceholder.getMapFile();

        if (mapFile != null) {
            jsonGamePlaceholder.put("mapId", "" + idManager.getId(mapFile));

            jsonGamePlaceholder.put("map", mapFileToJson(mapFile));
        }

        if (gamePlaceholder.isNameSet()) {
            jsonGamePlaceholder.put("name", gamePlaceholder.getName());
        }

        jsonGamePlaceholder.put("id", "" + idManager.getId(gamePlaceholder));

        /* Return a status of NOT_STARTED because this is a game placeholder */
        jsonGamePlaceholder.put("status", "NOT_STARTED");

        jsonGamePlaceholder.put("resources", gamePlaceholder.getResources().name());

        return jsonGamePlaceholder;
    }

    JSONObject playerToJson(Player player) {
        JSONObject jsonPlayer = new JSONObject();

        jsonPlayer.put("id", "" + idManager.getId(player));
        jsonPlayer.put("name", player.getName());
        jsonPlayer.put("color", colorToHexString(player.getColor()));

        return jsonPlayer;
    }

    JSONArray mapFilesToJson(List<MapFile> mapFiles) {
        JSONArray jsonMapFiles = new JSONArray();

        for (MapFile mapFile : mapFiles) {
            jsonMapFiles.add(mapFileToJson(mapFile));
        }

        return jsonMapFiles;
    }

    JSONObject mapFileToJson(MapFile mapFile) {
        JSONObject jsonMapFile = new JSONObject();

        jsonMapFile.put("title", mapFile.getTitle());
        jsonMapFile.put("author", mapFile.getAuthor());
        jsonMapFile.put("width", mapFile.getWidth());
        jsonMapFile.put("height", mapFile.getHeight());
        jsonMapFile.put("maxPlayers", mapFile.getMaxNumberOfPlayers());
        jsonMapFile.put("id", "" + "" + idManager.getId(mapFile));
        jsonMapFile.put("startingPoints", pointsToJson(mapFile.getStartingPoints()));

        return jsonMapFile;
    }

    Collection gamePlaceholdersToJson(List<GamePlaceholder> gamePlaceholders) {
        JSONArray jsonGamePlaceholders = new JSONArray();

        for (GamePlaceholder gamePlaceholder : gamePlaceholders) {
            jsonGamePlaceholders.add(gamePlaceholderToJson(gamePlaceholder));
        }

        return jsonGamePlaceholders;
    }

    GameMap gamePlaceholderToGame(GamePlaceholder gamePlaceholder) throws Exception {

        /* Create a GameMap instance from the map file */
        MapLoader mapLoader = new MapLoader();
        GameMap map = mapLoader.convertMapFileToGameMap(gamePlaceholder.getMapFile());

        /* Assign the players */
        map.setPlayers(new ArrayList<>(gamePlaceholder.getPlayers()));

        return map;
    }

    void adjustResources(GameMap map, ResourceLevel resources) throws Exception {

        for (Player player : map.getPlayers()) {

            Headquarter headquarter = (Headquarter)player.getBuildings().get(0);

            if (resources == ResourceLevel.LOW) {

                headquarter.retrieve(Material.STONE);
                headquarter.retrieve(Material.STONE);
                headquarter.retrieve(Material.STONE);

                headquarter.retrieve(Material.PLANK);
                headquarter.retrieve(Material.PLANK);
                headquarter.retrieve(Material.PLANK);

                headquarter.retrieve(Material.WOOD);
                headquarter.retrieve(Material.WOOD);
                headquarter.retrieve(Material.WOOD);
            } else if (resources == ResourceLevel.HIGH) {

                deliver(Material.STONE, 3, headquarter);
                deliver(Material.PLANK, 3, headquarter);
                deliver(Material.WOOD, 3, headquarter);
            }
        }
    }

    private void deliver(Material material, int amount, Headquarter headquarter) throws Exception {

        for (int i = 0; i < amount; i++) {
            headquarter.promiseDelivery(material);
            headquarter.putCargo(new Cargo(material, headquarter.getMap()));
        }
    }

    JSONArray housesToJson(List<Building> buildings) {
        JSONArray jsonHouses = new JSONArray();

        for (Building building : buildings) {
            jsonHouses.add(houseToJson(building));
        }

        return jsonHouses;
    }

    JSONObject mapFileTerrainToJson(MapFile mapFile) throws Exception {
        MapLoader mapLoader = new MapLoader();

        GameMap map = mapLoader.convertMapFileToGameMap(mapFile);

        return terrainToJson(map);
    }

    public JSONArray playersToShortJson(List<Player> players) {
        JSONArray jsonPlayers = new JSONArray();

        for (Player player : players) {
            JSONObject jsonPlayer = new JSONObject();

            jsonPlayer.put("name", player.getName());
            jsonPlayer.put("color", colorToHexString(player.getColor()));
            jsonPlayer.put("id", idManager.getId(player));

            jsonPlayers.add(jsonPlayer);
        }

        return jsonPlayers;
    }
}
