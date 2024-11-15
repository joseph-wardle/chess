package models;

import java.util.Objects;


/**
 * Represents a game with associated players, but for the client! Yay!
 */
public class Game {
    private int gameID;
    private String gameName;
    private String whiteUsername;
    private String blackUsername;

    public Game(int gameID, String gameName, String whiteUsername, String blackUsername) {
        this.gameID = gameID; // This is the game ID
        this.gameName = gameName; // This is the game name
        this.whiteUsername = whiteUsername; // this is the username of the white player
        this.blackUsername = blackUsername; // this is the username of the black player
    }

    public int getGameID() {
        // return the game ID
        return gameID;
    }

    public String getGameName() {
        // return the game name
        return gameName;
    }

    public String getWhiteUsername() {
        // return the white player's username
        return whiteUsername;
    }

    public String getBlackUsername() {
        // return the black player's username
        return blackUsername;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return gameID == game.gameID && Objects.equals(gameName, game.gameName)
                && Objects.equals(whiteUsername, game.whiteUsername)
                && Objects.equals(blackUsername, game.blackUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, gameName, whiteUsername, blackUsername);
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameID=" + gameID +
                ", gameName='" + gameName + '\'' +
                ", whiteUsername='" + whiteUsername + '\'' +
                ", blackUsername='" + blackUsername + '\'' +
                '}';
    }
}