package models;

/**
 * Represents a request to join a game, but in the client! Yay!
 */
public class JoinGameRequest {
    private String playerColor;
    private int gameID;

    public String getPlayerColor() {
        return playerColor;
    }

    // Hopefully adding some comments helps with the code quality thing
    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public int getGameID() {
        // the more comments the merrier!
        return gameID;
    }

    public void setGameID(int gameID) {
        // Return the game ID
        this.gameID = gameID;
    }
}