package models;

public class Game {
    private int gameID;
    private String gameName;
    private String whiteUsername;
    private String blackUsername;

    public Game(int gameID, String gameName, String whiteUsename, String blackUsername) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.whiteUsername = whiteUsename;
        this.blackUsername = blackUsername;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }
}
