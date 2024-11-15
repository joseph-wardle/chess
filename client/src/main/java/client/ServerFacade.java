package client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import models.AuthToken;
import models.Game;

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
        return getAuthToken(response);
    }

    private AuthToken getAuthToken(HttpResponse<String> response) throws Exception {
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            var responseData = gson.fromJson(responseBody, Map.class);
            String authToken = (String) responseData.get("authToken");
            String returnedUsername = (String) responseData.get("username");
            return new AuthToken(authToken, returnedUsername);
        } else {
            throw new Exception(parseErrorMessage(response.body()));
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
        return getAuthToken(response);
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
            throw new Exception(parseErrorMessage(response.body()));
        }
    }

    public List<Game> listGames(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Invalid auth token");
        }

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/game"))
                .GET()
                .header("Authorization", authToken)
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            var responseBody = response.body();
            // Parse the response body into a JsonObject
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            // Get the 'games' array from the JsonObject
            JsonArray gamesArray = jsonObject.getAsJsonArray("games");
            // Define the type for List<Game>
            Type gameListType = new TypeToken<List<Game>>() {}.getType();
            // Parse the gamesArray into List<Game>
            List<Game> games = gson.fromJson(gamesArray, gameListType);
            return games;
        } else {
            throw new Exception(parseErrorMessage(response.body()));
        }
    }

    public Game createGame(String authToken, String gameName) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Invalid auth token");
        }

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
            throw new Exception(parseErrorMessage(response.body()));
        }
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("Invalid auth token");
        }

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
            throw new Exception(parseErrorMessage(response.body()));
        }
    }

    // Method to clear the database between tests
    public void clearDatabase() throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/db"))
                .DELETE()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Failed to clear database: " + response.body());
        }
    }

    // Helper method to parse error messages from server responses
    private String parseErrorMessage(String responseBody) {
        try {
            var responseData = gson.fromJson(responseBody, Map.class);
            return (String) responseData.get("message");
        } catch (Exception e) {
            return "An error occurred: " + responseBody;
        }
    }

}