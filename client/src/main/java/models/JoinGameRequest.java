package models;

/**
 * Represents a request to join a game.
 */
public class JoinGameRequest {
    private String playerColor;
    private int gameID;

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
}