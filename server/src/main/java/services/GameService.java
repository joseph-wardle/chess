package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.TeamColorAlreadyTakenException;
import models.Game;
import models.AuthToken;

import java.util.List;

public class GameService {
    private DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Game createGame(String gameName, String username) throws DataAccessException {
        int gameId = dataAccess.getAllGames().size() + 1;
        Game game = new Game(gameId, gameName, null, null);
        game.setWhiteUsername(username); // Assuming creator is white by default
        dataAccess.createGame(game);
        return game;
    }

    /**
     * Allows a user to join a game with a specified team color.
     *
     * @param gameId      The ID of the game to join.
     * @param username    The username of the player joining the game.
     * @param playerColor The desired team color ("white" or "black").
     * @throws DataAccessException if the game is not found, the color is invalid, or the color is already taken.
     */
    public void joinGame(int gameId, String username, String playerColor) throws DataAccessException {
        Game game = dataAccess.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Game not found.");
        }

        if (playerColor == null || playerColor.isEmpty()) {
            throw new DataAccessException("playerColor is required.");
        }

        playerColor = playerColor.toLowerCase();

        switch (playerColor) {
            case "white":
                if (game.getWhiteUsername() != null && !game.getWhiteUsername().equals(username)) {
                    throw new TeamColorAlreadyTakenException("White player is already taken.");
                }
                game.setWhiteUsername(username);
                break;
            case "black":
                if (game.getBlackUsername() != null && !game.getBlackUsername().equals(username)) {
                    throw new TeamColorAlreadyTakenException("Black player is already taken.");
                }
                game.setBlackUsername(username);
                break;
            default:
                throw new DataAccessException("Invalid player color.");
        }

        dataAccess.updateGame(game);
    }

    public List<Game> listGames() throws DataAccessException {
        return dataAccess.getAllGames();
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameId The ID of the game.
     * @return The Game object.
     * @throws DataAccessException if the game is not found.
     */
    public Game getGame(int gameId) throws DataAccessException {
        Game game = dataAccess.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Game not found.");
        }
        return game;
    }
}