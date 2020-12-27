package org.appland.settlers.rest.resource;

import org.appland.settlers.maps.MapFile;
import org.appland.settlers.maps.MapLoader;
import org.appland.settlers.model.Armory;
import org.appland.settlers.model.Bakery;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.BorderChange;
import org.appland.settlers.model.Brewery;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.BuildingCapturedMessage;
import org.appland.settlers.model.BuildingLostMessage;
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
import org.appland.settlers.model.GameChangesList;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.GeologistFindMessage;
import org.appland.settlers.model.GoldMine;
import org.appland.settlers.model.GraniteMine;
import org.appland.settlers.model.GuardHouse;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.HunterHut;
import org.appland.settlers.model.IronMine;
import org.appland.settlers.model.IronSmelter;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Message;
import org.appland.settlers.model.Military;
import org.appland.settlers.model.MilitaryBuildingCausedLostLandMessage;
import org.appland.settlers.model.MilitaryBuildingOccupiedMessage;
import org.appland.settlers.model.MilitaryBuildingReadyMessage;
import org.appland.settlers.model.Mill;
import org.appland.settlers.model.Mint;
import org.appland.settlers.model.NoMoreResourcesMessage;
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
import org.appland.settlers.model.Storehouse;
import org.appland.settlers.model.StoreHouseIsReadyMessage;
import org.appland.settlers.model.TransportCategory;
import org.appland.settlers.model.Vegetation;
import org.appland.settlers.model.Tree;
import org.appland.settlers.model.TreeConservationProgramActivatedMessage;
import org.appland.settlers.model.TreeConservationProgramDeactivatedMessage;
import org.appland.settlers.model.UnderAttackMessage;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.appland.settlers.model.Message.MessageType.BUILDING_CAPTURED;
import static org.appland.settlers.model.Message.MessageType.BUILDING_LOST;
import static org.appland.settlers.model.Message.MessageType.GEOLOGIST_FIND;
import static org.appland.settlers.model.Message.MessageType.MILITARY_BUILDING_CAUSED_LOST_LAND;
import static org.appland.settlers.model.Message.MessageType.MILITARY_BUILDING_OCCUPIED;
import static org.appland.settlers.model.Message.MessageType.MILITARY_BUILDING_READY;
import static org.appland.settlers.model.Message.MessageType.NO_MORE_RESOURCES;
import static org.appland.settlers.model.Message.MessageType.STORE_HOUSE_IS_READY;
import static org.appland.settlers.model.Message.MessageType.TREE_CONSERVATION_PROGRAM_ACTIVATED;
import static org.appland.settlers.model.Message.MessageType.TREE_CONSERVATION_PROGRAM_DEACTIVATED;
import static org.appland.settlers.model.Message.MessageType.UNDER_ATTACK;

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

        String id = idManager.getId(map);

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

    private JSONObject playerToJson(Player player, String playerId) {
        JSONObject jsonPlayer = new JSONObject();

        jsonPlayer.put("name", player.getName());
        jsonPlayer.put("color", colorToHexString(player.getColor()));
        jsonPlayer.put("id", playerId);

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

    JSONArray pointsToJson(Collection<Point> points) {
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

    // TODO: remove this if possible
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

            for (int y = 1; y < map.getHeight(); y++) {
                for (int x = start; x + 1 < map.getWidth(); x += 2) {
                    Point p = new Point(x, y);

                    Vegetation below = map.getTileBelow(p);
                    Vegetation belowRight = map.getTileDownRight(p);

                    jsonTrianglesBelow.add(vegetationToJson(below));
                    jsonTrianglesBelowRight.add(vegetationToJson(belowRight));
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

    private String vegetationToJson(Vegetation v) {
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

    JSONObject pointToDetailedJson(Point point, Player player, GameMap map) {

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
        jsonHouse.put("playerId", idManager.getId(building.getPlayer()));
        jsonHouse.put("id", idManager.getId(building));

        if (building.canProduce()) {
            JSONArray jsonProduces = new JSONArray();

            jsonHouse.put("productivity", building.getProductivity());
            jsonHouse.put("produces", jsonProduces);

            for (Material material : building.getProducedMaterial()) {
                jsonProduces.add(material.name());
            }

            jsonHouse.put("productionEnabled", building.isProductionEnabled());
        }

        JSONObject jsonResources = new JSONObject();

        for (Material material : Material.values()) {
            int amountTotalNeeded = building.getTotalAmountNeeded(material);
            int amountAvailable = building.getAmount(material);

            JSONObject jsonResource = new JSONObject();

            if (amountAvailable > 0 || amountTotalNeeded > 0) {
                jsonResource.put("has", amountAvailable);

                if (amountTotalNeeded > 0) {
                    jsonResource.put("totalNeeded", amountTotalNeeded);
                }

                jsonResources.put(material.name().toLowerCase(), jsonResource);
            }
        }

        jsonHouse.put("resources", jsonResources);

        if (building.isUnderConstruction()) {
            jsonHouse.put("state", "UNFINISHED");
        } else if (building.isReady() && !building.isOccupied()) {
            jsonHouse.put("state", "UNOCCUPIED");
        } else if (building.isReady() && building.isOccupied()) {
            jsonHouse.put("state", "OCCUPIED");
        } else if (building.isBurningDown()) {
            jsonHouse.put("state", "BURNING");
        } else if (building.isDestroyed()) {
            jsonHouse.put("state", "DESTROYED");
        }

        if (building.isUnderConstruction()) {
            jsonHouse.put("constructionProgress", building.getConstructionProgress());
        }

        /* Add amount of hosted soldiers for military buildings */
        if (building.isMilitaryBuilding() && building.isReady()) {
            JSONArray jsonSoldiers = new JSONArray();

            for (Military military : building.getHostedMilitary()) {
                jsonSoldiers.add(military.getRank().name().toUpperCase());
            }

            jsonHouse.put("soldiers", jsonSoldiers);
            jsonHouse.put("maxSoldiers", building.getMaxHostedMilitary());
            jsonHouse.put("evacuated", building.isEvacuated());
            jsonHouse.put("promotionsEnabled", building.isPromotionEnabled());

            if (building.isUpgrading()) {
                jsonHouse.put("upgrading", true);
            }
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

    public Building buildingFactory(JSONObject jsonHouse, Player player) {
        String buildingType = (String)jsonHouse.get("type");

        return buildingFactory(buildingType, player);
    }

    public Building buildingFactory(String buildingType, Player player) {
        Building building = null;
        switch(buildingType) {
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
            case "Storehouse":
                building = new Storehouse(player);
                break;
            default:
                System.out.println("DON'T KNOW HOW TO CREATE BUILDING " + buildingType);
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
        jsonWorker.put("id", idManager.getId(worker));

        if (!worker.isExactlyAtPoint()) {
            jsonWorker.put("previous", pointToJson(worker.getLastPoint()));

            try {
                jsonWorker.put("next", pointToJson(worker.getNextPoint()));

            } catch(Exception e) {
                System.out.println("Exception while serializing worker: " + e);
            }

            jsonWorker.put("percentageTraveled", worker.getPercentageOfDistanceTraveled());
            jsonWorker.put("speed", 10); // TODO: dynamically look up speed
        } else {
            jsonWorker.put("percentageTraveled", 0);
        }

        if (worker.getCargo() != null) {
            jsonWorker.put("cargo", worker.getCargo().getMaterial().getSimpleName());
        }

        return jsonWorker;
    }

    JSONObject flagToJson(Flag flag) {
        JSONObject jsonFlag = pointToJson(flag.getPosition());

        jsonFlag.put("id", idManager.getId(flag));
        jsonFlag.put("playerId", idManager.getId(flag.getPlayer()));

        if (!flag.getStackedCargo().isEmpty()) {
            jsonFlag.put("stackedCargo", cargosToMaterialJson(flag.getStackedCargo()));
        }

        return jsonFlag;
    }

    private JSONArray cargosToMaterialJson(Collection<Cargo> cargos) {
        JSONArray jsonMaterial = new JSONArray() ;

        for (Cargo cargo : cargos) {
            Material material = cargo.getMaterial();

            jsonMaterial.add(material.getSimpleName());
        }

        return jsonMaterial;
    }

    JSONObject roadToJson(Road road) {
        JSONObject jsonRoad = new JSONObject();

        JSONArray jsonPoints = new JSONArray();

        for (Point point : road.getWayPoints()) {
            JSONObject jsonPoint = pointToJson(point);

            jsonPoints.add(jsonPoint);
        }

        jsonRoad.put("points", jsonPoints);
        jsonRoad.put("id", idManager.getId(road));

        return jsonRoad;
    }

    JSONObject borderToJson(Player player, String playerId) {

        /* Fill in borders */
        JSONObject jsonBorder = new JSONObject();
        jsonBorder.put("playerId", playerId);

        JSONArray jsonBorderPoints = new JSONArray();
        jsonBorder.put("points", jsonBorderPoints);

        for (Point point : player.getBorderPoints()) {
            jsonBorderPoints.add(pointToJson(point));
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

        jsonSign.put("id", idManager.getId(sign));

        if (sign.getSize() != null) {
            jsonSign.put("amount", sign.getSize().toString().toLowerCase());
        }

        return jsonSign;
    }

    Object cropToJson(Crop crop) {
        JSONObject jsonCrop = pointToJson(crop.getPosition());

        jsonCrop.put("state", "" + crop.getGrowthState());

        return jsonCrop;
    }

    JSONObject gamePlaceholderToJson(GameResource gamePlaceholder) {
        JSONObject jsonGamePlaceholder = new JSONObject();

        if (gamePlaceholder.getPlayers() != null) {
            jsonGamePlaceholder.put("players", playersToJson(gamePlaceholder.getPlayers()));
        } else {
            jsonGamePlaceholder.put("players", Collections.EMPTY_LIST);
        }

        MapFile mapFile = gamePlaceholder.getMapFile();

        if (mapFile != null) {
            jsonGamePlaceholder.put("mapId", idManager.getId(mapFile));

            jsonGamePlaceholder.put("map", mapFileToJson(mapFile));
        }

        if (gamePlaceholder.isNameSet()) {
            jsonGamePlaceholder.put("name", gamePlaceholder.getName());
        }

        jsonGamePlaceholder.put("id", idManager.getId(gamePlaceholder));

        /* Return a status of NOT_STARTED because this is a game placeholder */
        jsonGamePlaceholder.put("status", "NOT_STARTED");

        jsonGamePlaceholder.put("resources", gamePlaceholder.getResources().name());

        return jsonGamePlaceholder;
    }

    JSONObject playerToJson(Player player) {
        JSONObject jsonPlayer = new JSONObject();

        jsonPlayer.put("id", idManager.getId(player));
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
        jsonMapFile.put("id", idManager.getId(mapFile));
        jsonMapFile.put("startingPoints", pointsToJson(mapFile.getStartingPoints()));

        return jsonMapFile;
    }

    JSONArray gamePlaceholdersToJson(List<GameResource> gamePlaceholders) {
        JSONArray jsonGamePlaceholders = new JSONArray();

        for (GameResource gamePlaceholder : gamePlaceholders) {
            jsonGamePlaceholders.add(gamePlaceholderToJson(gamePlaceholder));
        }

        return jsonGamePlaceholders;
    }

    GameMap gamePlaceholderToGame(GameResource gamePlaceholder) throws Exception {

        /* Create a GameMap instance from the map file */
        MapLoader mapLoader = new MapLoader();
        GameMap map = mapLoader.convertMapFileToGameMap(gamePlaceholder.getMapFile());

        /* Assign the players */
        map.setPlayers(gamePlaceholder.getPlayers());

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

    private void deliver(Material material, int amount, Headquarter headquarter) {

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

    public JSONObject buildingLostMessageToJson(BuildingLostMessage buildingLostMessage) {
        JSONObject jsonBuildingLostMessage = new JSONObject();

        jsonBuildingLostMessage.put("type", "BUILDING_LOST");
        jsonBuildingLostMessage.put("houseId", idManager.getId(buildingLostMessage.getBuilding()));

        return jsonBuildingLostMessage;
    }

    public JSONObject buildingCapturedMessageToJson(BuildingCapturedMessage buildingCapturedMessage) {
        JSONObject jsonBuildingCapturedMessage = new JSONObject();

        jsonBuildingCapturedMessage.put("type", "BUILDING_CAPTURED");
        jsonBuildingCapturedMessage.put("houseId", idManager.getId(buildingCapturedMessage.getBuilding()));

        return jsonBuildingCapturedMessage;
    }

    public JSONObject jsonStoreHouseIsReadyMessageToJson(StoreHouseIsReadyMessage storeHouseIsReadyMessage) {
        JSONObject jsonStoreHouseIsReadyMessage = new JSONObject();

        jsonStoreHouseIsReadyMessage.put("type", "STORE_HOUSE_IS_READY");
        jsonStoreHouseIsReadyMessage.put("houseId", idManager.getId(storeHouseIsReadyMessage.getBuilding()));

        return jsonStoreHouseIsReadyMessage;
    }

    JSONObject militaryBuildingReadyMessageToJson(MilitaryBuildingReadyMessage militaryBuildingReadyMessage) {
        JSONObject jsonMilitaryBuildingOccupiedMessage;
        jsonMilitaryBuildingOccupiedMessage = new JSONObject();

        jsonMilitaryBuildingOccupiedMessage.put("type", MILITARY_BUILDING_READY.toString());
        jsonMilitaryBuildingOccupiedMessage.put("houseId", idManager.getId(militaryBuildingReadyMessage.getBuilding()));

        return jsonMilitaryBuildingOccupiedMessage;
    }

    JSONObject noMoreResourcesMessageToJson(NoMoreResourcesMessage noMoreResourcesMessage) {
        JSONObject jsonNoMoreResourcesMessage = new JSONObject();

        jsonNoMoreResourcesMessage.put("type", NO_MORE_RESOURCES.toString());
        jsonNoMoreResourcesMessage.put("houseId", idManager.getId(noMoreResourcesMessage.getBuilding()));

        return jsonNoMoreResourcesMessage;
    }

    JSONObject militaryBuildingOccupiedMessageToJson(MilitaryBuildingOccupiedMessage militaryBuildingOccupiedMessage) {
        JSONObject jsonMilitaryBuildingOccupiedMessage = new JSONObject();

        jsonMilitaryBuildingOccupiedMessage.put("type", MILITARY_BUILDING_OCCUPIED.toString());
        jsonMilitaryBuildingOccupiedMessage.put("houseId", idManager.getId(militaryBuildingOccupiedMessage.getBuilding()));

        return jsonMilitaryBuildingOccupiedMessage;
    }

    JSONObject underAttackMessageToJson(UnderAttackMessage underAttackMessage) {
        JSONObject jsonUnderAttackMessage;
        jsonUnderAttackMessage = new JSONObject();

        jsonUnderAttackMessage.put("type", UNDER_ATTACK.toString());
        jsonUnderAttackMessage.put("houseId", idManager.getId(underAttackMessage.getBuilding()));

        return jsonUnderAttackMessage;
    }

    JSONObject geologistFindMessageToJson(GeologistFindMessage geologistFindMessage) {
        JSONObject jsonGeologistFindMessage = new JSONObject();

        JSONObject jsonGeologistFindPoint = new JSONObject();

        jsonGeologistFindPoint.put("x", geologistFindMessage.getPoint().x);
        jsonGeologistFindPoint.put("y", geologistFindMessage.getPoint().y);

        jsonGeologistFindMessage.put("type", GEOLOGIST_FIND.toString());
        jsonGeologistFindMessage.put("point", jsonGeologistFindPoint);

        jsonGeologistFindMessage.put("material", geologistFindMessage.getMaterial().toString());

        return jsonGeologistFindMessage;
    }

    public JSONObject gameMonitoringEventsToJson(GameChangesList gameChangesList, Player player) {
        JSONObject jsonMonitoringEvents = new JSONObject();

        jsonMonitoringEvents.put("time", gameChangesList.getTime());

        if (!gameChangesList.getNewStones().isEmpty()) {
            jsonMonitoringEvents.put("newStones", newStonesToJson(gameChangesList.getNewStones()));
        }

        if (!gameChangesList.getWorkersWithNewTargets().isEmpty()) {
            jsonMonitoringEvents.put("workersWithNewTargets", workersWithNewTargetsToJson(gameChangesList.getWorkersWithNewTargets()));
        }

        if (!gameChangesList.getNewBuildings().isEmpty()) {
            jsonMonitoringEvents.put("newBuildings", newBuildingsToJson(gameChangesList.getNewBuildings()));
        }

        if (!gameChangesList.getNewFlags().isEmpty()) {
            jsonMonitoringEvents.put("newFlags", flagsToJson(gameChangesList.getNewFlags()));
        }

        if (!gameChangesList.getNewRoads().isEmpty()) {
            jsonMonitoringEvents.put("newRoads", newRoadsToJson(gameChangesList.getNewRoads()));
        }

        if (!gameChangesList.getNewTrees().isEmpty()) {
            jsonMonitoringEvents.put("newTrees", newTreesToJson(gameChangesList.getNewTrees()));
        }

        if (!gameChangesList.getNewDiscoveredLand().isEmpty()) {
            jsonMonitoringEvents.put("newDiscoveredLand", newDiscoveredLandToJson(gameChangesList.getNewDiscoveredLand()));
        }

        if (!gameChangesList.getNewCrops().isEmpty()) {
            jsonMonitoringEvents.put("newCrops", newCropsToJson(gameChangesList.getNewCrops()));
        }

        if (!gameChangesList.getNewSigns().isEmpty()) {
            jsonMonitoringEvents.put("newSigns", newSignsToJson(gameChangesList.getNewSigns()));
        }

        if (!gameChangesList.getChangedBuildings().isEmpty()) {
            jsonMonitoringEvents.put("changedBuildings", changedBuildingsToJson(gameChangesList.getChangedBuildings()));
        }

        if (!gameChangesList.getChangedFlags().isEmpty()) {
            jsonMonitoringEvents.put("changedFlags", flagsToJson(gameChangesList.getChangedFlags()));
        }

        if (!gameChangesList.getRemovedWorkers().isEmpty()) {
            jsonMonitoringEvents.put("removedWorkers", removedWorkersToJson(gameChangesList.getRemovedWorkers()));
        }

        if (!gameChangesList.getRemovedBuildings().isEmpty()) {
            jsonMonitoringEvents.put("removedBuildings", removedBuildingsToJson(gameChangesList.getRemovedBuildings()));
        }

        if (!gameChangesList.getRemovedFlags().isEmpty()){
            jsonMonitoringEvents.put("removedFlags", removedFlagsToJson(gameChangesList.getRemovedFlags()));
        }

        if (!gameChangesList.getRemovedRoads().isEmpty()) {
            jsonMonitoringEvents.put("removedRoads", removedRoadsToJson(gameChangesList.getRemovedRoads()));
        }

        if (!gameChangesList.getRemovedTrees().isEmpty()) {
            jsonMonitoringEvents.put("removedTrees", removedTreesToJson(gameChangesList.getRemovedTrees()));
        }

        if (!gameChangesList.getChangedBorders().isEmpty()) {
            jsonMonitoringEvents.put("changedBorders", borderChangesToJson(gameChangesList.getChangedBorders()));
        }

        if (!gameChangesList.getChangedAvailableConstruction().isEmpty()) {
            jsonMonitoringEvents.put(
                    "changedAvailableConstruction",
                    availableConstructionChangesToJson(gameChangesList.getChangedAvailableConstruction(), player)
            );
        }

        if (!gameChangesList.getRemovedCrops().isEmpty()) {
            jsonMonitoringEvents.put("removedCrops", removedCropsToJson(gameChangesList.getRemovedCrops()));
        }

        if (!gameChangesList.getRemovedSigns().isEmpty()) {
            jsonMonitoringEvents.put("removedSigns", removedSignsToJson(gameChangesList.getRemovedSigns()));
        }

        if (!gameChangesList.getRemovedStones().isEmpty()) {
            jsonMonitoringEvents.put("removedStones", removedStonesToJson(gameChangesList.getRemovedStones()));
        }

        if (!gameChangesList.getNewGameMessages().isEmpty()) {
            jsonMonitoringEvents.put("newMessages", messagesToJson(gameChangesList.getNewGameMessages()));
        }

        return jsonMonitoringEvents;
    }

    private JSONArray newStonesToJson(List<Stone> newStones) {
        JSONArray jsonNewStones = new JSONArray();

        for (Stone stone : newStones) {
            jsonNewStones.add(pointToJson(stone.getPosition()));
        }

        return jsonNewStones;
    }

    private JSONArray messagesToJson(List<Message> newGameMessages) {
        JSONArray jsonMessages = new JSONArray();

        for (Message message : newGameMessages) {
            if (message.getMessageType() == MILITARY_BUILDING_OCCUPIED) {
                jsonMessages.add(militaryBuildingOccupiedMessageToJson((MilitaryBuildingOccupiedMessage) message));
            } else if (message.getMessageType() == GEOLOGIST_FIND) {
                jsonMessages.add(geologistFindMessageToJson((GeologistFindMessage) message));
            } else if (message.getMessageType() == MILITARY_BUILDING_READY) {
                jsonMessages.add(militaryBuildingReadyMessageToJson((MilitaryBuildingReadyMessage) message));
            } else if (message.getMessageType() == NO_MORE_RESOURCES) {
                jsonMessages.add(noMoreResourcesMessageToJson((NoMoreResourcesMessage) message));
            } else if (message.getMessageType() == UNDER_ATTACK) {
                jsonMessages.add(underAttackMessageToJson((UnderAttackMessage) message));
            } else if (message.getMessageType() == BUILDING_CAPTURED) {
                jsonMessages.add(buildingCapturedMessageToJson((BuildingCapturedMessage) message));
            } else if (message.getMessageType() == BUILDING_LOST) {
                jsonMessages.add(buildingLostMessageToJson((BuildingLostMessage) message));
            } else if (message.getMessageType() == STORE_HOUSE_IS_READY) {
                jsonMessages.add(jsonStoreHouseIsReadyMessageToJson((StoreHouseIsReadyMessage) message));
            } else if (message.getMessageType() == TREE_CONSERVATION_PROGRAM_ACTIVATED) {
                jsonMessages.add(treeConservationProgramActivatedMessageToJson((TreeConservationProgramActivatedMessage) message));
            } else if (message.getMessageType() == TREE_CONSERVATION_PROGRAM_DEACTIVATED) {
                jsonMessages.add(treeConservationProgramDeactivatedMessageToJson((TreeConservationProgramDeactivatedMessage) message));
            } else if (message.getMessageType() == MILITARY_BUILDING_CAUSED_LOST_LAND) {
                jsonMessages.add(militaryBuildingCausedLostLandMessageToJson((MilitaryBuildingCausedLostLandMessage) message));
            }
        }

        return jsonMessages;
    }

    private JSONObject militaryBuildingCausedLostLandMessageToJson(MilitaryBuildingCausedLostLandMessage message) {
        JSONObject jsonMilitaryBuildingCausedLostLandMessage = new JSONObject();

        jsonMilitaryBuildingCausedLostLandMessage.put("type", MILITARY_BUILDING_CAUSED_LOST_LAND.toString());
        jsonMilitaryBuildingCausedLostLandMessage.put("houseId", idManager.getId(message.getBuilding()));

        return jsonMilitaryBuildingCausedLostLandMessage;
    }

    private JSONObject treeConservationProgramDeactivatedMessageToJson(TreeConservationProgramDeactivatedMessage message) {
        JSONObject jsonTreeConservationProgramDeactivated = new JSONObject();

        jsonTreeConservationProgramDeactivated.put("type", TREE_CONSERVATION_PROGRAM_DEACTIVATED.toString());

        return jsonTreeConservationProgramDeactivated;
    }

    private JSONObject treeConservationProgramActivatedMessageToJson(TreeConservationProgramActivatedMessage message) {
        JSONObject jsonTreeConservationProgramActivated = new JSONObject();

        jsonTreeConservationProgramActivated.put("type", TREE_CONSERVATION_PROGRAM_ACTIVATED.toString());

        return jsonTreeConservationProgramActivated;
    }



    private JSONArray availableConstructionChangesToJson(Collection<Point> changedAvailableConstruction, Player player) {
        GameMap map = player.getMap();

        JSONArray jsonChangedAvailableConstruction = new JSONArray();

        synchronized (map) {
            for (Point point : changedAvailableConstruction) {
                JSONObject jsonPointAndAvailableConstruction = new JSONObject();
                JSONArray jsonAvailableConstruction = new JSONArray();

                if (map.isAvailableFlagPoint(player, point)) {
                    jsonAvailableConstruction.add("flag");
                }

                Size size = map.isAvailableHousePoint(player, point);

                if (size != null) {
                    jsonAvailableConstruction.add(size.name().toLowerCase());
                }

                if (map.isAvailableMinePoint(player, point)) {
                    jsonAvailableConstruction.add("mine");
                }

                jsonPointAndAvailableConstruction.put("available", jsonAvailableConstruction);
                jsonPointAndAvailableConstruction.put("x", point.x);
                jsonPointAndAvailableConstruction.put("y", point.y);

                jsonChangedAvailableConstruction.add(jsonPointAndAvailableConstruction);
            }
        }

        return jsonChangedAvailableConstruction;
    }

    private JSONArray borderChangesToJson(List<BorderChange> changedBorders) {
        JSONArray jsonBorderChanges = new JSONArray();

        for (BorderChange borderChange : changedBorders) {
            JSONObject jsonBorderChange = new JSONObject();

            jsonBorderChange.put("playerId", idManager.getId(borderChange.getPlayer()));
            jsonBorderChange.put("newBorder", pointsToJson(borderChange.getNewBorder()));
            jsonBorderChange.put("removedBorder", pointsToJson(borderChange.getRemovedBorder()));

            jsonBorderChanges.add(jsonBorderChange);
        }

        return jsonBorderChanges;
    }

    private JSONArray removedStonesToJson(List<Stone> removedStones) {
        JSONArray jsonRemovedStones = new JSONArray();

        for (Stone stone : removedStones) {
            jsonRemovedStones.add(pointToJson(stone.getPosition()));
        }

        return jsonRemovedStones;
    }

    private JSONArray removedSignsToJson(List<Sign> removedSigns) {
        return objectsToJsonIdArray(removedSigns);
    }

    private JSONArray newSignsToJson(List<Sign> newSigns) {
        JSONArray jsonSigns = new JSONArray();

        for (Sign sign : newSigns) {
            jsonSigns.add(signToJson(sign));
        }

        return jsonSigns;
    }

    private JSONArray removedCropsToJson(List<Crop> removedCrops) {
        JSONArray jsonRemovedCrops = new JSONArray();

        for (Crop crop : removedCrops) {
            jsonRemovedCrops.add(pointToJson(crop.getPosition()));
        }

        return jsonRemovedCrops;
    }

    private JSONArray newCropsToJson(List<Crop> newCrops) {
        return cropsToJson(newCrops);
    }

    private JSONArray cropsToJson(List<Crop> newCrops) {
        JSONArray jsonCrops = new JSONArray();

        for (Crop crop : newCrops) {
            jsonCrops.add(cropToJson(crop));
        }

        return jsonCrops;
    }

    private JSONArray newDiscoveredLandToJson(Collection<Point> newDiscoveredLand) {
        return pointsToJson(newDiscoveredLand);
    }

    private JSONArray removedTreesToJson(List<Tree> removedTrees) {
        JSONArray jsonRemovedTrees = new JSONArray();

        for (Tree tree : removedTrees) {
            jsonRemovedTrees.add(pointToJson(tree.getPosition()));
        }

        return jsonRemovedTrees;
    }

    private JSONArray newTreesToJson(List<Tree> newTrees) {
        return treesToJson(newTrees);
    }

    private JSONArray treesToJson(List<Tree> newTrees) {
        JSONArray jsonTrees = new JSONArray();

        for (Tree tree : newTrees) {
            jsonTrees.add(treeToJson(tree));
        }

        return jsonTrees;
    }

    private JSONArray changedBuildingsToJson(List<Building> changedBuildings) {
        return housesToJson(changedBuildings);
    }

    private JSONArray removedRoadsToJson(List<Road> removedRoads) {
        return objectsToJsonIdArray(removedRoads);
    }

    private JSONArray removedFlagsToJson(List<Flag> removedFlags) {
        return objectsToJsonIdArray(removedFlags);
    }

    private JSONArray removedBuildingsToJson(List<Building> removedBuildings) {
        return objectsToJsonIdArray(removedBuildings);
    }

    private JSONArray removedWorkersToJson(List<Worker> removedWorkers) {
        return objectsToJsonIdArray(removedWorkers);
    }

    private JSONArray objectsToJsonIdArray(List<?> gameObjects) {
        JSONArray jsonIdArray = new JSONArray();

        for (Object gameObject : gameObjects) {
            jsonIdArray.add(idManager.getId(gameObject));
        }

        return jsonIdArray;
    }

    private JSONArray newRoadsToJson(List<Road> newRoads) {
        JSONArray jsonNewRoads = new JSONArray();

        for (Road road : newRoads) {
            jsonNewRoads.add(roadToJson(road));
        }

        return jsonNewRoads;
    }

    private JSONArray flagsToJson(Collection<Flag> flags) {
        JSONArray jsonFlags = new JSONArray();

        for (Flag flag : flags) {
            jsonFlags.add(flagToJson(flag));
        }

        return jsonFlags;
    }

    private JSONArray newBuildingsToJson(List<Building> newBuildings) {
        JSONArray jsonNewBuildings = new JSONArray();

        for (Building building : newBuildings) {
            jsonNewBuildings.add(houseToJson(building));
        }

        return jsonNewBuildings;
    }

    private JSONArray workersWithNewTargetsToJson(List<Worker> workersWithNewTargets) {
        JSONArray jsonWorkersWithNewTarget = new JSONArray();

        for (Worker worker : workersWithNewTargets) {
            JSONObject jsonWorkerWithNewTarget = new JSONObject();

            if (worker.getPlannedPath().isEmpty()) {
                System.out.println("EMPTY PATH");
                System.out.println(worker);
            }

            jsonWorkerWithNewTarget.put("id", idManager.getId(worker));
            jsonWorkerWithNewTarget.put("path", pointsToJson(worker.getPlannedPath()));

            jsonWorkerWithNewTarget.put("x", worker.getPosition().x);
            jsonWorkerWithNewTarget.put("y", worker.getPosition().y);

            jsonWorkerWithNewTarget.put("type", worker.getClass().getSimpleName());

            if (worker.getCargo() != null) {
                jsonWorkerWithNewTarget.put("cargo", worker.getCargo().getMaterial().getSimpleName());
            }

            jsonWorkersWithNewTarget.add(jsonWorkerWithNewTarget);
        }

        return jsonWorkersWithNewTarget;
    }

    public JSONArray transportPriorityToJson(List<TransportCategory> transportPriorityList) {
        JSONArray jsonTransportPriority = new JSONArray();

        for (TransportCategory category : transportPriorityList) {
            jsonTransportPriority.add(category.name().toLowerCase());
        }

        return jsonTransportPriority;
    }

    public Set<Point> jsonToPointsSet(JSONArray avoid) {
        Set<Point> pointsSet = new HashSet<>();

        for (Object jsonPoint : avoid) {
            Point point = jsonToPoint((JSONObject) jsonPoint);

            pointsSet.add(point);
        }

        return pointsSet;
    }

    public void printTimestamp(String message) {

        Date date = new Date();
        long timeMilli = date.getTime();
        System.out.println(message + ": " + timeMilli);
    }
}
