package org.appland.settlers.rest.resource;

import org.appland.settlers.maps.MapFile;
import org.appland.settlers.model.Building;
import org.appland.settlers.model.Crop;
import org.appland.settlers.model.Flag;
import org.appland.settlers.model.GameMap;
import org.appland.settlers.model.Headquarter;
import org.appland.settlers.model.LandDataPoint;
import org.appland.settlers.model.LandStatistics;
import org.appland.settlers.model.Material;
import org.appland.settlers.model.Player;
import org.appland.settlers.model.Point;
import org.appland.settlers.model.ProductionDataPoint;
import org.appland.settlers.model.ProductionDataSeries;
import org.appland.settlers.model.Road;
import org.appland.settlers.model.Sign;
import org.appland.settlers.model.Size;
import org.appland.settlers.model.StatisticsManager;
import org.appland.settlers.model.Stone;
import org.appland.settlers.model.Tree;
import org.appland.settlers.model.WildAnimal;
import org.appland.settlers.model.Worker;
import org.appland.settlers.rest.GameTicker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.appland.settlers.model.Material.GOLD;
import static org.appland.settlers.model.Material.PLANK;
import static org.appland.settlers.model.Material.SHIELD;
import static org.appland.settlers.model.Material.STONE;
import static org.appland.settlers.model.Material.SWORD;
import static org.appland.settlers.model.Material.WOOD;

@Path("/settlers/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SettlersAPI {

    public final static String MAP_FILE_LIST = "mapFileList";
    public static final String GAME_TICKER = "gameTicker";
    public final List<Material> PRODUCTION_STATISTICS_MATERIALS = Arrays.asList(
            WOOD,
            STONE,
            PLANK,
            GOLD,
            SWORD,
            SHIELD
    );

    private final IdManager idManager;
    private final Utils utils;
    @Context
    ServletContext context;

    private final List<GameMap> games;
    private final JSONParser parser;
    private final List<GamePlaceholder> gamePlaceholders;

    public SettlersAPI() {
        games = new ArrayList<>();

        idManager = new IdManager();
        utils = new Utils(idManager);

        parser = new JSONParser();
        gamePlaceholders = new ArrayList<>();
    }

    @GET
    @Path("/maps")
    public Response getMaps() {

        /* Get the list of map files from the servlet context */
        List<MapFile> mapFiles = (List<MapFile>) context.getAttribute(MAP_FILE_LIST);

        /* Return the list of map files as JSON documents */
        return Response.status(200).entity(utils.mapFilesToJson(mapFiles).toJSONString()).build();
    }

    @GET
    @Path("/maps/{mapId}")
    public Response getMap(@PathParam("mapId") String mapId) {
        MapFile mapFile = (MapFile) idManager.getObject(Integer.parseInt(mapId));

        return Response.status(200).entity(utils.mapFileToJson(mapFile).toJSONString()).build();
    }

    @GET
    @Path("/maps/{mapId}/terrain")
    public Response getTerrainForMap(@PathParam("mapId") String id) throws Exception {
        MapFile mapFile = (MapFile)idManager.getObject(Integer.parseInt(id));

        return Response.status(200).entity(utils.mapFileTerrainToJson(mapFile).toJSONString()).build();
    }

    @GET
    @Path("/games")
    public Response getGames() {
        JSONArray jsonGames = utils.gamesToJson(games);

        jsonGames.addAll(utils.gamePlaceholdersToJson(gamePlaceholders));

        return Response.status(200).entity(jsonGames.toJSONString()).build();
    }

    @DELETE
    @Path("/maps/{mapId}")
    public Response deleteMap() {

        return Response.status(405).build();
    }

    @GET
    @Path("/games/{id}")
    public Response getGame(@PathParam("id") int id) {

        Object gameObject = idManager.getObject(id);

        /* Return 404 if the game doesn't exist */
        if (gameObject == null) {
            return Response.status(404).build();
        }

        /* Return the game as a JSON document */
        if (gameObject instanceof GameMap) {
            JSONObject jsonGame = utils.gameToJson((GameMap)gameObject);

            return Response.status(200).entity(jsonGame.toJSONString()).build();
        } else {
            JSONObject jsonGame = utils.gamePlaceholderToJson((GamePlaceholder)gameObject);

            return Response.status(200).entity(jsonGame.toJSONString()).build();
        }
    }

    @POST
    @Path("/games")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGame(String body) throws Exception {

        /* Return 400 (bad request) if the body is empty */
        if (body.equals("")) {
            return Response.status(400).build();
        }

        JSONObject jsonGame = (JSONObject) parser.parse(body);

        /* Create a real game instance if the game is immediately started */
        if (jsonGame.containsKey("status") && jsonGame.get("status").equals("STARTED")) {

            return null;
        } else {

            /* Create a placeholder if there are missing attributes */
            GamePlaceholder gamePlaceholder = new GamePlaceholder();

            if (jsonGame.containsKey("name")) {
                gamePlaceholder.setName((String) jsonGame.get("name"));
            }

            if (jsonGame.containsKey("players")) {
                gamePlaceholder.setPlayers(utils.jsonToPlayers((JSONArray) jsonGame.get("players")));
            }

            if (jsonGame.containsKey("mapId")) {
                String mapId = (String) jsonGame.get("mapId");
                gamePlaceholder.setMap((MapFile) idManager.getObject(Integer.parseInt(mapId)));
            }

            gamePlaceholders.add(gamePlaceholder);

            return Response.status(201).entity(utils.gamePlaceholderToJson(gamePlaceholder).toJSONString()).build();
        }

        //return Response.status(201).entity("").build();
    }

    @PATCH
    @Path("/games/{gameId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyGame(@PathParam("gameId") String gameId, String body) throws Exception {
        Object gameObject = idManager.getObject(Integer.parseInt(gameId));

        JSONObject jsonUpdates = (JSONObject) parser.parse(body);

        if (jsonUpdates.containsKey("mapId") && gameObject instanceof GamePlaceholder) {
            String updatedMapFileId = (String) jsonUpdates.get("mapId");

            MapFile updatedMapFile = (MapFile) idManager.getObject(Integer.parseInt(updatedMapFileId));

            if (gameObject instanceof GamePlaceholder) {
                GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

                gamePlaceholder.setMap(updatedMapFile);

                return Response.status(200).entity(utils.gamePlaceholderToJson(gamePlaceholder).toJSONString()).build();
            } else {
                return Response.status(500).build();
            }
        }

        if (jsonUpdates.containsKey("status") && gameObject instanceof GamePlaceholder) {
            String updatedStatus = (String) jsonUpdates.get("status");

            GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

            if (updatedStatus.equals("STARTED")) {

                /* Convert the game placeholder to a game map */
                GameMap map = utils.gamePlaceholderToGame(gamePlaceholder);
                games.add(map);
                gamePlaceholders.remove(gamePlaceholder);

                idManager.updateObject(gameObject, map);

                /* Place a headquarter for each player */
                List<Player> players = map.getPlayers();
                List<Point> startingPoints = map.getStartingPoints();

                for (int i = 0; i < startingPoints.size(); i++) {

                    if (i == players.size()) {
                        break;
                    }

                    map.placeBuilding(new Headquarter(players.get(i)), startingPoints.get(i));
                }

                /* Adjust the initial set of resources */
                utils.adjustResources(map, gamePlaceholder.getResources());

                /* Start the time for the game by adding it to the game ticker */
                GameTicker gameTicker = (GameTicker) context.getAttribute(GAME_TICKER);

                gameTicker.startGame(map);

                return Response.status(200).entity(utils.gameToJson(map).toJSONString()).build();
            }

            return Response.status(405).build();
        }

        if (jsonUpdates.containsKey("resources") && gameObject instanceof GamePlaceholder) {
            ResourceLevel level = ResourceLevel.valueOf((String) jsonUpdates.get("resources"));

            GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

            gamePlaceholder.setResource(level);

            return Response.status(200).entity(utils.gamePlaceholderToJson(gamePlaceholder).toJSONString()).build();
        }

        /* Return bad request (400) if there is no mapFileId included */
        return Response.status(400).build();
    }

    @DELETE
    @Path("/games/{gameId}")
    public Response deleteGame(@PathParam("gameId") String gameId) {
        Object gameObject = idManager.getObject(Integer.parseInt(gameId));

        /* Return 404 if the game doesn't exist */
        if (gameObject == null) {
            return Response.status(404).build();
        }

        if (gameObject instanceof GamePlaceholder) {
            gamePlaceholders.remove(gameObject);
        } else {
            games.remove(gameObject);
        }

        /* Free up the id */
        idManager.remove(gameObject);

        return Response.status(200).build();
    }

    @GET
    @Path("/games/{id}/players")
    public Response getPlayersForGame(@PathParam("id") int id) {
        Object gameObject = idManager.getObject(id);

        if (gameObject instanceof GamePlaceholder) {
            GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

            return Response.status(200).entity(utils.playersToJson(gamePlaceholder.getPlayers()).toJSONString()).build();
        } else {
            GameMap map = (GameMap) gameObject;

            JSONArray jsonPlayers = utils.playersToJson(map.getPlayers());

            return Response.status(200).entity(jsonPlayers.toJSONString()).build();
        }
    }

    @POST
    @Path("/games/{gameId}/players")
    public Response addPlayerToGame(@PathParam("gameId") String gameId, String playerBody) throws ParseException {
        JSONObject jsonPlayer = (JSONObject) parser.parse(playerBody);

        Player player = utils.jsonToPlayer(jsonPlayer);

        Object gameObject = idManager.getObject(Integer.parseInt(gameId));

        if (gameObject instanceof GamePlaceholder) {
            GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

            gamePlaceholder.addPlayer(player);
        }


        return Response.status(201).entity(utils.playerToJson(player).toJSONString()).build();
    }

    @PATCH
    @Path("/games/{gameId}/players/{playerId}")
    public Response updatePlayerInGame(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId, String body) throws ParseException {
        GamePlaceholder gamePlaceholder = (GamePlaceholder) idManager.getObject(Integer.parseInt(gameId));
        Player player = (Player) idManager.getObject(Integer.parseInt(playerId));
        JSONObject jsonUpdates = (JSONObject) parser.parse(body);

        // TODO: verify that the player is in the right game
        // TODO: verify that the player exists

        if (jsonUpdates.containsKey("name")) {
            player.setName((String)jsonUpdates.get("name"));
        }

        if (jsonUpdates.containsKey("color")) {
            player.setColor(Color.decode((String)jsonUpdates.get("color")));
        }

        return Response.status(200).entity(utils.playerToJson(player).toJSONString()).build();
    }

    @DELETE
    @Path("/games/{gameId}/players/{playerId}")
    public Response removePlayerFromGame(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId) {
        GamePlaceholder gamePlaceholder = (GamePlaceholder) idManager.getObject(Integer.parseInt(gameId));
        Player player = (Player) idManager.getObject(Integer.parseInt(playerId));

        // TODO: verify that the player is in the right game
        // TODO: verify that the player exists

        gamePlaceholder.removePlayer(player);

        return Response.status(200).entity(utils.playerToJson(player).toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/players/{playerId}")
    public Response getPlayerForGame(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId) {
        Player player = (Player) idManager.getObject(Integer.parseInt(playerId));
        Object gameObject = idManager.getObject(Integer.parseInt(gameId));

        /* Return 404 if the game doesn't exist */
        if (gameObject == null) {
            return Response.status(404).build();
        }

        /* Check that the player belongs to the given game */
        if (gameObject instanceof GamePlaceholder) {
            GamePlaceholder gamePlaceholder = (GamePlaceholder) gameObject;

            /* Return 404 if the player does not belong to the given game */
            if (!gamePlaceholder.getPlayers().contains(player)) {
                return Response.status(404).build();
            }
        }

        /* Return the player */
        return Response.status(200).entity(utils.playerToJson(player).toJSONString()).build();
    }

    @GET
    @Path("/healthz")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        JSONObject response = new JSONObject();

        response.put("isHealthy", true);

        return Response.status(200).entity(response.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/map/terrain")
    public Response getTerrainForMapInGame(@PathParam("gameId") int id) {
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

            response.put("message", "Called geologist to " + point);
        } else if (jsonBody.containsKey("scoutNeeded") &&
                (Boolean)jsonBody.get("scoutNeeded")) {
            map.getFlagAtPoint(point).callScout();

            response.put("message", "Called scout to " + point);
        }

        return Response.status(200).entity(response.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/map/points")
    public Response getPoint(@PathParam("gameId") int gameId, @QueryParam("playerId") int playerId, @QueryParam("x") int x, @QueryParam("y") int y) throws Exception {
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

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/players/{playerId}/houses/{houseId}")
    public Response getHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("houseId") int houseId) {
        Building building = (Building) idManager.getObject(houseId);
        JSONObject jsonHouse = utils.houseToJson(building);

        return Response.status(200).entity(jsonHouse.toJSONString()).build();
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

    @GET
    @Path("/games/{gameId}/players/{playerId}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getHouses(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId) {
        Player player = (Player) idManager.getObject(playerId);

        //FIXME: Verify that the player is connected to the game

        return Response.status(200).entity(utils.housesToJson(player.getBuildings()).toJSONString()).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/houses")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String body) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);

        JSONObject jsonHouse = (JSONObject) parser.parse(body);

        Point point = utils.jsonToPoint(jsonHouse);

        Building building = utils.buildingFactory(jsonHouse, player);

        synchronized (map) {
            map.placeBuilding(building, point);
        }

        return Response.status(200).entity(utils.houseToJson(building).toJSONString()).build();
    }

    @PUT
    @Path("/games/{gameId}/players/{playerId}/houses/{houseId}")
    public Response updateHouse(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("houseId") int houseId, String body) throws Exception {
        Building building = (Building) idManager.getObject(houseId);
        Player player = (Player) idManager.getObject(playerId);

        JSONObject jsonHouseModification = (JSONObject) parser.parse(body);

        JSONObject jsonResponse = new JSONObject();

        System.out.println(jsonHouseModification.keySet());

        if (jsonHouseModification.containsKey("evacuate")) {

            if (!building.isMilitaryBuilding()) {
                jsonResponse.put("message", "Cannot evacuate non-military building");
            } else if (!(building.occupied() || building.ready())) {
                jsonResponse.put("message", "Cannot evacuate a building in this state");
            } else {
                building.evacuate();

                jsonResponse = utils.houseToJson(building);
            }
        } else if(jsonHouseModification.containsKey("attack")) {

            if (building.getPlayer().equals(player)) {
                player.attack(building, 1);

                jsonResponse.put("message", "Attacking building");
            } else {
                jsonResponse.put("message", "Cannot attack own building");
            }
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

        List<Point> points = utils.jsonToPoints(jsonPoints);

        Road road;

        if (points.size() == 2) {
            road = map.placeAutoSelectedRoad(player, points.get(0), points.get(1));
        } else {
            road = map.placeRoad(player, points);
        }

        return Response.status(200).entity(utils.roadToJson(road).toJSONString()).build();
    }

    @POST
    @Path("/games/{gameId}/players/{playerId}/flags")
    public Response createFlag(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String bodyFlag) throws Exception {
        GameMap map = (GameMap) idManager.getObject(gameId);
        Player player = (Player) idManager.getObject(playerId);
        JSONObject jsonPoint = (JSONObject) parser.parse(bodyFlag);

        Point point = utils.jsonToPoint(jsonPoint);

        Flag flag = map.placeFlag(player, point);

        return Response.status(200).entity(utils.flagToJson(flag).toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/players/{playerId}/flags/{flagId}")
    public Response getFlag(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, @PathParam("flagId") int flagId) {
        // TODO: check that the flag belongs to the player and that the player belongs to the game
        Flag flag = (Flag) idManager.getObject(flagId);

        return Response.status(200).entity(utils.flagToJson(flag).toJSONString()).build();
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

                jsonFlags.add(utils.flagToJson(flag));
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

    @POST
    @Path("/rpc/games/{gameId}/players/{playerId}/find-new-road")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findNewRoad(@PathParam("gameId") int gameId, @PathParam("playerId") int playerId, String bodyFindNewRoad) throws ParseException {
        GameMap map = (GameMap)idManager.getObject(gameId);
        Player player = (Player)idManager.getObject(playerId);

        JSONObject jsonNewRoadParameters = (JSONObject) parser.parse(bodyFindNewRoad);

        Point start = utils.jsonToPoint((JSONObject) jsonNewRoadParameters.get("from"));
        Point goal = utils.jsonToPoint((JSONObject) jsonNewRoadParameters.get("to"));
        List<Point> avoid = null;

        if (jsonNewRoadParameters.containsKey("avoid")) {
            avoid = utils.jsonToPoints((JSONArray) jsonNewRoadParameters.get("avoid"));
        }

        List<Point> possibleRoad = map.findAutoSelectedRoad(player, start, goal, avoid);

        JSONObject findNewRoadResponse = new JSONObject();

        findNewRoadResponse.put("roadIsPossible", true);
        findNewRoadResponse.put("possibleRoad", utils.pointsToJson(possibleRoad));

        if (map.isFlagAtPoint(goal) || (map.isRoadAtPoint(goal) && map.isAvailableFlagPoint(player, goal))) {
            findNewRoadResponse.put("closesRoad", true);
        } else {
            findNewRoadResponse.put("closesRoad", false);
        }

        return Response.status(200).entity(findNewRoadResponse.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/statistics/land")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLandStatistics(@PathParam("gameId") int gameId) {
        GameMap map = (GameMap)idManager.getObject(gameId);

        JSONObject jsonResponse = new JSONObject();

        /*

        {'players': [...]    -- jsonPlayers
         'currentTime': 12345,
         'landStatistics': [ -- jsonLandStatisticsDataSeries
            {'time': 23,     -- jsonLandMeasurement
             'values': [2, 3, 4, 5]
            },
         ...
         ]
        }
         */

        /* Add the players */
        JSONArray jsonPlayers = utils.playersToShortJson(map.getPlayers());

        /* Add the land statistics array to the response */
        JSONArray jsonLandStatisticsDataSeries = new JSONArray();

        LandStatistics landStatistics = map.getStatisticsManager().getLandStatistics();

        for (LandDataPoint landDataPoint : landStatistics.getDataPoints()){
            JSONObject jsonLandMeasurement = new JSONObject();

            jsonLandMeasurement.put("time", landDataPoint.getTime());

            JSONArray jsonLandValues = new JSONArray();
            for (int landSize : landDataPoint.getValues()) {
                jsonLandValues.add(landSize);
            }

            jsonLandMeasurement.put("values", jsonLandValues);

            jsonLandStatisticsDataSeries.add(jsonLandMeasurement);
        }

        jsonResponse.put("players", jsonPlayers);
        jsonResponse.put("landStatistics", jsonLandStatisticsDataSeries);
        jsonResponse.put("currentTime", map.getCurrentTime());

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }

    @GET
    @Path("/games/{gameId}/statistics/production")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMaterialStatistics(@PathParam("gameId") int gameId) {
        GameMap map = (GameMap)idManager.getObject(gameId);

        JSONObject jsonResponse = new JSONObject();

        jsonResponse.put("players", utils.playersToShortJson(map.getPlayers()));

        JSONArray jsonProductionStatisticsForAllMaterials = new JSONArray();
        StatisticsManager statisticsManager = map.getStatisticsManager();

        for (Material material : PRODUCTION_STATISTICS_MATERIALS) {

            ProductionDataSeries materialProductionDataSeries = statisticsManager.getProductionStatisticsForMaterial(material);

            /* Set the meta data for the report for this material */
            JSONObject jsonMaterialStatisticsDataAndMeta = new JSONObject();
            JSONArray jsonMaterialStatisticsDataSeries = new JSONArray();

            jsonMaterialStatisticsDataAndMeta.put("material", material.name().toLowerCase());
            jsonMaterialStatisticsDataAndMeta.put("materialStatistics", jsonMaterialStatisticsDataSeries);

            /* Add the statistics for this material to the array */
            jsonProductionStatisticsForAllMaterials.add(jsonMaterialStatisticsDataAndMeta);

            for (ProductionDataPoint dataPoint : materialProductionDataSeries.getProductionDataPoints()) {

                JSONObject jsonMaterialMeasurementPoint = new JSONObject();

                /* Set measurement 0 */
                jsonMaterialMeasurementPoint.put("time", dataPoint.getTime());

                JSONArray jsonMaterialMeasurementPointValues = new JSONArray();

                int[] values = dataPoint.getValues();

                for (int value : values) {
                    jsonMaterialMeasurementPointValues.add(value);
                }

                jsonMaterialMeasurementPoint.put("values", jsonMaterialMeasurementPointValues);

                /* Add the data point to the data series */
                jsonMaterialStatisticsDataSeries.add(jsonMaterialMeasurementPoint);
            }
        }

        /* Add the production statistics array to the response */
        jsonResponse.put("materialStatistics", jsonProductionStatisticsForAllMaterials);

        return Response.status(200).entity(jsonResponse.toJSONString()).build();
    }
}
