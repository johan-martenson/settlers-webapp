package org.appland.settlers.rest.resource;

import org.appland.settlers.model.Building;
import org.appland.settlers.model.Crop;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Sign;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.Stone;
import org.appland.settlers.model.Tree;
import org.appland.settlers.model.WildAnimal;
import org.appland.settlers.model.Worker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/settlers/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SettlersAPI {

    private final IdManager idManager;
    private final Utils utils;
    @Context
    ServletContext context;

    private final List<GameMap> games;
    private JSONParser parser;

    public SettlersAPI() throws Exception {
        games = new ArrayList<>();

        /* Pre-populate with one game */
        List<Player> players = new ArrayList<>();
        players.add(new Player("johan", Color.BLUE));
        games.add(new GameMap(players, 300, 300));

        /* */
        idManager = new IdManager();
        utils = new Utils(idManager);

        parser = new JSONParser();
    }

    @GET
    @Path("/games")
    public Response getGames() {
        JSONArray jsonGames = utils.gamesToJson(games);

        return Response.status(200).entity(jsonGames.toJSONString()).build();
    }

    @GET
    @Path("/games/{id}")
    public Response getGame(@PathParam("id") int id) {
        JSONObject jsonGame = utils.gameToJson((GameMap)idManager.getObject(id));

        return Response.status(200).entity(jsonGame.toJSONString()).build();
    }

    @POST
    @Path("/games")
    public Response createGame(String body) throws Exception {
        JSONObject jsonGame = (JSONObject)parser.parse(body);

        GameMap map = utils.jsonToGame(jsonGame);

        games.add(map);

        return Response.status(200).entity(body).build();
    }

    @GET
    @Path("/games/{id}/players")
    public Response getPlayersForGame(@PathParam("id") int id) {
        GameMap map = (GameMap)idManager.getObject(id);

        JSONArray jsonPlayers = utils.playersToJson(map.getPlayers());

        return Response.status(200).entity(jsonPlayers.toJSONString()).build();
    }

    @GET
    @Path("/healthz")
    public Response getHealth() {
        JSONObject response = new JSONObject();

        response.put("health", "healthy");

        return Response.status(200).entity(response.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/map/terrain")
    public Response getTerrainForMap(@PathParam("gameId") int id) {
        GameMap map = (GameMap)idManager.getObject(id);
        JSONObject terrain = utils.terrainToJson(map);

        return Response.status(200).entity(terrain.toJSONString()).build();
    }

    @PUT
    @Path("/games/{gameId}/map/points")
    public Response putPoint(@PathParam("gameId") int gameId, @QueryParam("x") int x, @QueryParam("y") int y, String body) throws Exception {
        Point point = new Point(x, y);
        GameMap map = (GameMap)idManager.getObject(gameId);

        JSONObject jsonBody = (JSONObject) parser.parse(body);
        JSONObject response = new JSONObject();

        if (jsonBody.containsKey("geologistNeeded") &&
                (Boolean)jsonBody.get("geologistNeeded")) {
            map.getFlagAtPoint(point).callGeologist();

            System.out.println("Called geologist");
            response.put("message", "Called geologist to " + point);
        } else if (jsonBody.containsKey("scoutNeeded") &&
                (Boolean)jsonBody.get("scoutNeeded")) {
            map.getFlagAtPoint(point).callScout();

            System.out.println("Called scout");
            response.put("message", "Called scout to " + point);
        }

        return Response.status(200).entity(response.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/map/points")
    public Response getPoint(@PathParam("gameId") int gameId, @QueryParam("playerId") int playerId, @QueryParam("x") int x, @QueryParam("y") int y) {
        System.out.println("Getting point info");
        Point point = new Point(x, y);
        GameMap map = (GameMap)idManager.getObject(gameId);
        Player player = (Player)idManager.getObject(playerId);

        JSONObject jsonPoint = utils.pointToDetailedJson(point, player, map);

        return Response.status(200).entity(jsonPoint.toJSONString()).build();
    }

    @DELETE
    @Path("/games/{gameId}/players/{playerId}/flags/{flagId}")
    public Response removeFlag(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("flagId") int flagId) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);
        Flag flag = (Flag) idManager.getObject(flagId);

        JSONObject jsonResponse = new JSONObject();

        if (player.equals(flag.getPlayer())) {
            map.removeFlag(flag);

            jsonResponse.put("message", "Flag removed");
        } else {
            jsonResponse.put("message", "Cannot remove flag for other player");
        }

        return Response.status(200).entity(jsonResponse).build();
    }

    @GET
    @Path("/games/{gameId}/players/{playerId}/houses/{houseId}")
    public Response getHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("houseId") int houseId) {
        Building building = (Building) idManager.getObject(houseId);
        JSONObject jsonHouse = utils.houseToJson(building);

        return Response.status(200).entity(jsonHouse).build();
    }

    @DELETE
    @Path("/games/{gameId}/players/{playerId}/houses/{houseId}")
    public Response removeHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("houseId") int houseId) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);
        Building building = (Building) idManager.getObject(houseId);

        JSONObject jsonResponse = new JSONObject();

        if (building.getPlayer().equals(player)) {
            building.tearDown();

            jsonResponse.put("message", "Tore down building");
        } else {
            jsonResponse.put("message", "Cannot tear down building for other player");
        }

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/houses")
    public Response createHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String body) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);
        JSONObject jsonHouse = (JSONObject) parser.parse(body);

        Point point = utils.jsonToPoint(jsonHouse);

        Building building = utils.buildingFactory(jsonHouse, player);

        map.placeBuilding(building, point);

        JSONObject jsonResponse = new JSONObject();
        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @PUT
    @Path("/games/{gameId}/players/{playerId}/houses/{houseId}")
    public Response updateHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("houseId") int houseId) throws Exception {
        Building building = (Building) idManager.getObject(houseId);
        Player player = (Player) idManager.getObject(playerId);

        JSONObject jsonResponse = new JSONObject();

        if (building.getPlayer().equals(player)) {
            player.attack(building, 1);

            jsonResponse.put("message", "Attacking building");
        } else {
            jsonResponse.put("message", "Cannot attack own building");
        }

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/roads")
    public Response createRoad(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String bodyRoad) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);

        JSONObject jsonRoad = (JSONObject) parser.parse(bodyRoad);

        JSONArray jsonPoints = (JSONArray) jsonRoad.get("points");

        JSONObject jsonResponse = new JSONObject();

        List<Point> points = utils.jsonToPoints(jsonPoints);

        if (points.size() == 2) {
            map.placeAutoSelectedRoad(player, points.get(0), points.get(1));
            jsonResponse.put("message", "Auto-placed road");
        } else {
            map.placeRoad(player, points);
            jsonResponse.put("message", "Placed road");
        }

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/roads")
    public Response createFlag(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String bodyRoad) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);
        JSONObject jsonPoint = (JSONObject) parser.parse(bodyRoad);

        Point point = utils.jsonToPoint(jsonPoint);

        map.placeFlag(player, point);

        JSONObject jsonResponse = new JSONObject();

        jsonResponse.put("message", "Raised flag");

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/players/{playerId}/view")
    public Response getViewForPlayer(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId) {

        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);

        /* Create instances outside the synchronized block when possible */
        JSONObject view = new JSONObject();

        JSONArray  jsonHouses                = new JSONArray();
        JSONArray  trees                     = new JSONArray();
        JSONArray  jsonStones                = new JSONArray();
        JSONArray  jsonWorkers               = new JSONArray();
        JSONArray  jsonFlags                 = new JSONArray();
        JSONArray  jsonRoads                 = new JSONArray();
        JSONArray  jsonDiscoveredPoints      = new JSONArray();
        JSONArray  jsonBorders               = new JSONArray();
        JSONArray  jsonSigns                 = new JSONArray();
        JSONArray  jsonAnimals               = new JSONArray();
        JSONArray  jsonCrops                 = new JSONArray();
        JSONObject jsonAvailableConstruction = new JSONObject();

        view.put("trees", trees);
        view.put("houses", jsonHouses);
        view.put("stones", jsonStones);
        view.put("workers", jsonWorkers);
        view.put("flags", jsonFlags);
        view.put("roads", jsonRoads);
        view.put("discoveredPoints", jsonDiscoveredPoints);
        view.put("borders", jsonBorders);
        view.put("signs", jsonSigns);
        view.put("animals", jsonAnimals);
        view.put("crops", jsonCrops);
        view.put("availableConstruction", jsonAvailableConstruction);

        /* Protect access to the map to avoid interference */
        synchronized (map) {
            Set<Point> discoveredLand = player.getDiscoveredLand();

            /* Fill in houses */
            for (Building building : map.getBuildings()) {

                if (!discoveredLand.contains(building.getPosition())) {
                    continue;
                }

                jsonHouses.add(utils.houseToJson(building));
            }

            /* Fill in trees */
            for (Tree tree : map.getTrees()) {
                if (!discoveredLand.contains(tree.getPosition())) {
                    continue;
                }

                trees.add(utils.treeToJson(tree));
            }

            /* Fill in stones */
            for (Stone stone : map.getStones()) {

                if (!discoveredLand.contains(stone.getPosition())) {
                    continue;
                }

                jsonStones.add(utils.stoneToJson(stone));
            }

            /* Fill in workers */
            for (Worker worker : map.getWorkers()) {

                if (!discoveredLand.contains(worker.getPosition())) {
                    continue;
                }

                if (worker.isInsideBuilding()) {
                    continue;
                }

                jsonWorkers.add(utils.workerToJson(worker));
            }

            /* Fill in flags */
            for (Flag flag : map.getFlags()) {

                if (!discoveredLand.contains(flag.getPosition())) {
                    continue;
                }

                jsonFlags.add(utils.flagToJson(flag, idManager.getId(flag), playerId));
            }

            /* Fill in roads */
            for (Road road : map.getRoads()) {

                boolean inside = false;

                /* Filter roads the player cannot see */
                for (Point p : road.getWayPoints()) {
                    if (discoveredLand.contains(p)) {
                        inside = true;

                        break;
                    }
                }

                if (!inside) {
                    continue;
                }

                jsonRoads.add(utils.roadToJson(road));
            }

            /* Fill in the points the player has discovered */
            for (Point point : discoveredLand) {
                jsonDiscoveredPoints.add(utils.pointToJson(point));
            }

            jsonBorders.add(utils.borderToJson(player, playerId));

            /* Fill in the signs */
            for (Sign sign : map.getSigns()) {

                if (!discoveredLand.contains(sign.getPosition())) {
                    continue;
                }

                jsonSigns.add(utils.signToJson(sign));
            }

            /* Fill in wild animals */
            for (WildAnimal animal : map.getWildAnimals()) {

                if (!discoveredLand.contains(animal.getPosition())) {
                    continue;
                }

                /* Animal is an extension of worker so the same method is used */

                jsonAnimals.add(utils.workerToJson(animal));
            }

            /* Fill in crops */
            for (Crop crop : map.getCrops()) {

                if (!discoveredLand.contains(crop.getPosition())) {
                    continue;
                }

                jsonCrops.add(utils.cropToJson(crop));
            }
        }

        /* Fill in available construction */
        try {
            for (Point point : player.getAvailableFlagPoints()) {

                /* Filter points not discovered yet */
                if (!player.getDiscoveredLand().contains(point)) {
                    continue;
                }

                String key = "" + point.x + "," + point.y;

                jsonAvailableConstruction.putIfAbsent(key, new JSONArray());

                ((JSONArray)jsonAvailableConstruction.get(key)).add("flag");
            }

            for (Map.Entry<Point, Size> site : player.getAvailableHousePoints().entrySet()) {

                /* Filter points not discovered yet */
                if (!player.getDiscoveredLand().contains(site.getKey())) {
                    continue;
                }

                String key = "" + site.getKey().x + "," + site.getKey().y;

                jsonAvailableConstruction.putIfAbsent(key, new JSONArray());

                ((JSONArray)jsonAvailableConstruction.get(key)).add("" + site.getValue().toString().toLowerCase());
            }

            for (Point point : player.getAvailableMiningPoints()) {

                /* Filter points not discovered yet */
                if (!player.getDiscoveredLand().contains(point)) {
                    continue;
                }

                String key = "" + point.x + "," + point.y;

                jsonAvailableConstruction.putIfAbsent(key, new JSONArray());

                ((JSONArray)jsonAvailableConstruction.get(key)).add("mine");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        for (Point point : player.getAvailableMiningPoints()) {

            /* Filter points not discovered yet */
            if (!player.getDiscoveredLand().contains(point)) {
                continue;
            }

            String key = "" + point.x + "," + point.y;

            jsonAvailableConstruction.putIfAbsent(key, new JSONArray());

            ((JSONArray)jsonAvailableConstruction.get(key)).add("mine");
        }

        return Response.status(200).entity(view.toJSONString()).build();
    }
}
