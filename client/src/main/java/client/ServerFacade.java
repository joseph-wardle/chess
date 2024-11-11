package client;

import com.google.gson.Gson;
import models.AuthToken;
import models.Game;

import java.io.IOException;
import java.util.List;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();
    private String authToken;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public AuthToken register(String username, String password, String email) throws IOException {
        return null;
    }

    public AuthToken login(String username, String password) throws IOException {
        return null;
    }

    public void logout() throws IOException {
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
