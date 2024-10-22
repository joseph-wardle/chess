package handlers;

import dataaccess.InvalidAuthTokenException;
import dataaccess.TeamColorAlreadyTakenException;
import models.JoinGameRequest;
import services.GameService;
import services.AuthService;
import models.Game;
import models.AuthToken;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

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
            Game game = gameService.createGame(gameName);
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("gameID", game.getGameId(), "gameName", game.getGameName()));
        } catch (InvalidAuthTokenException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
        catch (Exception e) {
            res.status(400); // Bad Request
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    public Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            AuthToken auth = authService.authenticate(authToken);
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            String playerColor = joinRequest.getPlayerColor();
            int gameId = joinRequest.getGameID();
            if (playerColor == null || playerColor.isEmpty()) {
                throw new Exception("playerColor and gameID are required.");
            }
            gameService.joinGame(gameId, auth.getUsername(), playerColor);
            res.status(200);
            res.type("application/json");
            Game game = gameService.getGame(gameId);
            return gson.toJson(Map.of(
                    "gameId", game.getGameId(),
                    "status", "joined",
                    "players", Map.of(
                            "white", game.getWhiteUsername(),
                            "black", game.getBlackUsername()
                    )
            ));
        } catch (TeamColorAlreadyTakenException e) {
            res.status(403); // Forbidden
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        } catch (InvalidAuthTokenException e) {
            res.status(401);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
        catch (Exception e) {
            res.status(400); // Bad Request
            return gson.toJson(Map.of("message", "Error: " + e.getMessage(), "success", false));
        }
    }

    // Additional handlers as needed
}