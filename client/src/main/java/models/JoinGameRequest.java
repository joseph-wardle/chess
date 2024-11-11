package models;

public class JoinGameRequest {
    private String playerColor;
    private int gameID;

    public void setPlayerColor(String playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public String getPlayerColor() {
        return playerColor;
    }
    public int getGameID() {
        return gameID;
    }
}
