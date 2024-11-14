package models;

import java.util.Map;

/**
 * Represents game data returned by the server.
 */
public class Game {
    private final int gameID;
    private final String gameName;
    private final String whitePlayer;
    private final String blackPlayer;

    public Game(int gameID, String gameName, String whitePlayer, String blackPlayer) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public static Game fromMap(Map<String, Object> map) {
        int gameID = ((Double) map.get("gameID")).intValue();
        String gameName = (String) map.get("gameName");
        String whitePlayer = (String) map.get("whiteUsername");
        String blackPlayer = (String) map.get("blackUsername");
        return new Game(gameID, gameName, whitePlayer, blackPlayer);
    }
}
