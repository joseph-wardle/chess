package client;

import com.google.gson.Gson;
import models.AuthToken;
import models.Game;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverURL;
    private final Gson gson = new Gson();
    private String authToken;

    public ServerFacade(String serverURL) {
        this.serverURL = serverURL;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public AuthToken register(String username, String password, String email) throws IOException {
        URL url = new URL(serverURL + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);

        String jsonRequest = gson.toJson(requestBody);

        OutputStream os = connection.getOutputStream();
        os.write(jsonRequest.getBytes());
        os.flush();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            Reader reader = new InputStreamReader(is);
            Map<String, String> responseMap = gson.fromJson(reader, Map.class);
            String token = responseMap.get("authToken");
            String user = responseMap.get("username");
            AuthToken auth = new AuthToken(token, user);
            setAuthToken(token);
            return auth;
        } else {
            InputStream is = connection.getErrorStream();
            Reader reader = new InputStreamReader(is);
            Map<String, String> responseMap = gson.fromJson(reader, Map.class);
            String message = responseMap.get("message");
            throw new IOException(message);
        }
    }

    public AuthToken login(String username, String password) throws IOException {
        URL url = new URL(serverURL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        String jsonRequest = gson.toJson(requestBody);

        OutputStream os = connection.getOutputStream();
        os.write(jsonRequest.getBytes());
        os.flush();

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = connection.getInputStream();
            Reader reader = new InputStreamReader(is);
            Map<String, String> responseMap = gson.fromJson(reader, Map.class);
            String token = responseMap.get("authToken");
            String user = responseMap.get("username");
            AuthToken auth = new AuthToken(token, user);
            setAuthToken(token);
            return auth;
        } else {
            InputStream is = connection.getErrorStream();
            Reader reader = new InputStreamReader(is);
            Map<String, String> responseMap = gson.fromJson(reader, Map.class);
            String message = responseMap.get("message");
            throw new IOException(message);
        }
    }

    public void logout() throws IOException {
        URL url = new URL(serverURL + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Logout successful
            authToken = null;
        } else {
            InputStream is = connection.getErrorStream();
            Reader reader = new InputStreamReader(is);
            Map<String, String> responseMap = gson.fromJson(reader, Map.class);
            String message = responseMap.get("message");
            throw new IOException(message);
        }
    }

    public Game createGame(String gameName) throws IOException {
        return null;
    }

    public List<Game> listGames() throws IOException {
        return null;
    }

    public void joinGame(int gameId, String playerColor) throws IOException {
    }

    public void clearData() throws IOException {
    }
}
