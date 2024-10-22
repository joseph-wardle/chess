package handlers;

import services.GameService;
import services.AuthService;
import models.Game;
import models.AuthToken;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.List;

public class GameHandler {
    private GameService gameService;
    private AuthService authService;
    private Gson gson = new Gson();

    public GameHandler(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    public Object listGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            authService.authenticate(authToken);
            List<Game> games = gameService.listGames();
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("games", games));
        } catch (Exception e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    public Object createGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            AuthToken auth = authService.authenticate(authToken);
            Map<String, String> requestBody = gson.fromJson(req.body(), Map.class);
            String gameName = requestBody.get("gameName");
            if (gameName == null || gameName.isEmpty()) {
                throw new Exception("Game name is required.");
            }
            Game game = gameService.createGame(gameName, auth.getUsername());
            res.status(201);
            res.type("application/json");
            return gson.toJson(Map.of("gameId", game.getGameId(), "gameName", game.getGameName()));
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    public Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            AuthToken auth = authService.authenticate(authToken);
            Map<String, Object> requestBody = gson.fromJson(req.body(), Map.class);
            String playerColor = (String) requestBody.get("playerColor");
            Double gameIdDouble = (Double) requestBody.get("gameID");
            if (playerColor == null || gameIdDouble == null) {
                throw new Exception("playerColor and gameID are required.");
            }
            int gameId = gameIdDouble.intValue();
            gameService.joinGame(gameId, auth.getUsername(), playerColor);
            res.status(200);
            res.type("application/json");
            Game game = gameService.getGame(gameId); // Assume getGame method exists
            return gson.toJson(Map.of(
                    "gameId", game.getGameId(),
                    "status", "joined",
                    "players", Map.of(
                            "white", game.getWhiteUsername(),
                            "black", game.getBlackUsername()
                    )
            ));
        } catch (Exception e) {
            res.status(400);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    // Additional handlers as needed
}