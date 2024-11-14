package client;

import com.google.gson.Gson;
import models.AuthToken;
import models.Game;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Handles HTTP communication with the server.
 */
public class ServerFacade {
    private final String baseUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    public AuthToken register(String username, String password, String email) throws Exception {
        var client = HttpClient.newHttpClient();
        var user = Map.of("username", username, "password", password, "email", email);
        var requestBody = gson.toJson(user);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            var responseData = gson.fromJson(responseBody, Map.class);
            String authToken = (String) responseData.get("authToken");
            String returnedUsername = (String) responseData.get("username");
            return new AuthToken(returnedUsername, authToken);
        } else {
            throw new Exception("Registration failed: " + response.body());
        }
    }

    public AuthToken login(String username, String password) throws Exception {
        var client = HttpClient.newHttpClient();
        var user = Map.of("username", username, "password", password);
        var requestBody = gson.toJson(user);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/session"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            var responseData = gson.fromJson(responseBody, Map.class);
            String authToken = (String) responseData.get("authToken");
            String returnedUsername = (String) responseData.get("username");
            return new AuthToken(returnedUsername, authToken);
        } else {
            throw new Exception("Login failed: " + response.body());
        }
    }

    public void logout(String authToken) throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/session"))
                .DELETE()
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Logout failed: " + response.body());
        }
    }

    public List<Game> listGames(String authToken) throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/game"))
                .GET()
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            var responseData = gson.fromJson(responseBody, Map.class);
            var gamesList = (List<Map<String, Object>>) responseData.get("games");
            return gamesList.stream().map(Game::fromMap).toList();
        } else {
            throw new Exception("Failed to list games: " + response.body());
        }
    }

    public Game createGame(String authToken, String gameName) throws Exception {
        var client = HttpClient.newHttpClient();
        var game = Map.of("gameName", gameName);
        var requestBody = gson.toJson(game);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/game"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            var responseData = gson.fromJson(responseBody, Map.class);
            int gameID = ((Double) responseData.get("gameID")).intValue();
            String returnedGameName = (String) responseData.get("gameName");
            return new Game(gameID, returnedGameName, null, null);
        } else {
            throw new Exception("Failed to create game: " + response.body());
        }
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var client = HttpClient.newHttpClient();
        var joinRequest = Map.of("gameID", gameID, "playerColor", playerColor);
        var requestBody = gson.toJson(joinRequest);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/game"))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Authorization", authToken)
                .header("Content-Type", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Failed to join game: " + response.body());
        }
    }
}
