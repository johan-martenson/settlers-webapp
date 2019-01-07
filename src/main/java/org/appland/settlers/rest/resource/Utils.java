package org.appland.settlers.rest.resource;

import org.appland.settlers.model.Bakery;
import org.appland.settlers.model.Barracks;
import org.appland.settlers.model.Building;
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
import org.appland.settlers.model.Material;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private final IdManager idManager;

    Utils(IdManager idManager) {
        this.idManager = idManager;
    }

    public JSONArray gamesToJson(Map<Integer, GameMap> gamesMap) {
        JSONArray jsonGames = new JSONArray();

        for (Map.Entry<Integer, GameMap> entry : gamesMap.entrySet()) {
            int id = entry.getKey();
            GameMap map = entry.getValue();

            JSONObject jsonGame = gameToJson(map);

            jsonGames.add(jsonGame);
        }

        return jsonGames;
    }

    JSONObject gameToJson(GameMap map) {
        JSONObject jsonGame = new JSONObject();

        int id = idManager.getId(map);

        jsonGame.put("id", id);

        jsonGame.put("width", map.getWidth());
        jsonGame.put("height", map.getHeight());


        jsonGame.put("players", playersToJson(map.getPlayers()));

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
        jsonPlayer.put("id", i);

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

        GameMap map = new GameMap(players, width, height);

        return map;
    }

    private List<Player> jsonToPlayers(JSONArray jsonPlayers) {
        List<Player> players = new ArrayList<>();

        for (Object jsonPlayer : jsonPlayers) {
            jsonPlayers.add(jsonToPlayer((JSONObject) jsonPlayer));
        }

        return players;
    }

    private Player jsonToPlayer(JSONObject jsonPlayer) {
        String name = (String) jsonPlayer.get("name");
        Color color = jsonToColor((String) jsonPlayer.get("color"));

        Player player = new Player(name, color);

        return player;
    }

    private Color jsonToColor(String color) {
        return Color.BLUE;
    }

    public JSONObject terrainToJson(GameMap map) {
        JSONObject jsonTerrain = new JSONObject();

        JSONArray jsonTrianglesBelow = new JSONArray();
        JSONArray jsonTrianglesBelowRight = new JSONArray();

        jsonTerrain.put("straightBelow", jsonTrianglesBelow);
        jsonTerrain.put("belowToTheRight", jsonTrianglesBelowRight);

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
            default:
                System.out.println("Cannot handle this vegetation " + v);
                System.exit(1);
        }

        return ""; // Should never be reached but the compiler complains
    }

    public JSONObject pointToDetailedJson(Point point, Player player, GameMap map) {

        JSONObject jsonPointInfo = pointToJson(point);

        if (player.getDiscoveredLand().contains(point)) {

            if (map.isBuildingAtPoint(point)) {
                Building building = map.getBuildingAtPoint(point);
                jsonPointInfo.put("building", houseToJson(building));
                jsonPointInfo.put("is", "building");
            }

            if (map.isFlagAtPoint(point)) {
                jsonPointInfo.put("is", "flag");
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
        jsonHouse.put("houseId", idManager.getId(building));

        if (building.canProduce()) {
            JSONArray jsonProduces = new JSONArray();

            jsonHouse.put("productivity", building.getProductivity());
            jsonHouse.put("produces", jsonProduces);

            for (Material material : building.getProducedMaterial()) {
                jsonProduces.add(material.name());
            }

            JSONObject jsonResources = new JSONObject();

            for (Material material : building.getMaterialNeeded()) {
                int amount = building.getTotalAmountNeeded(material);

                if (amount > 0) {
                    JSONObject jsonResource = new JSONObject();

                    jsonResource.put("needs", amount);
                    jsonResource.put("has", building.getAmount(material));

                    jsonResources.put(material.name().toLowerCase(), jsonResource);
                }
            }

            jsonHouse.put("resources", jsonResources);
        }

        if (building.underConstruction()) {
            jsonHouse.put("state", "unfinished");
        } else if (building.ready() && !building.occupied()) {
            jsonHouse.put("state", "unoccupied");
        } else if (building.ready() && building.occupied()) {
            jsonHouse.put("state", "occupied");
        } else if (building.burningDown()) {
            jsonHouse.put("state", "burning");
        } else if (building.destroyed()) {
            jsonHouse.put("state", "destroyed");
        }

        return jsonHouse;
    }


    public List<Point> jsonToPoints(JSONArray jsonPoints) {
        List<Point> points = new ArrayList<>();

        for (Object point : jsonPoints) {
            points.add(jsonToPoint((JSONObject) point));
        }

        return points;
    }

    Point jsonToPoint(JSONObject point) {
        int x = Integer.parseInt((String) point.get("x"));
        int y = Integer.parseInt((String) point.get("y"));

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
                JSONObject jsonNext = new JSONObject();
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

    JSONObject flagToJson(Flag flag, int flagId, int playerId) {
        JSONObject jsonFlag = pointToJson(flag.getPosition());

        jsonFlag.put("flagId", flagId);
        jsonFlag.put("playerId", playerId);

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

        return jsonSign;
    }

    Object cropToJson(Crop crop) {
        JSONObject jsonCrop = pointToJson(crop.getPosition());

        jsonCrop.put("state", "" + crop.getGrowthState());

        return jsonCrop;
    }

}
