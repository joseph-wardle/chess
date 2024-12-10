package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.TeamColorAlreadyTakenException;
import models.Game;

import java.util.List;

/**
 * Service for handling game-related operations.
 */
public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Creates a new game with the specified name.
     *
     * @param gameName The name of the game.
     * @return The created Game object.
     * @throws DataAccessException If an error occurs during game creation.
     */
    public Game createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Game name is required.");
        }
        Game game = new Game(-1, gameName, null, null);
        dataAccess.createGame(game);
        return game;
    }

    /**
     * Allows a user to join a game with a specified team color.
     *
     * @param gameId      The ID of the game to join.
     * @param username    The username of the player joining the game.
     * @param playerColor The desired team color ("white" or "black").
     * @throws DataAccessException If the game is not found, the color is invalid, or the color is already taken.
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

    /**
     * Lists all available games.
     *
     * @return A list of all Game objects.
     * @throws DataAccessException If an error occurs while fetching games.
     */
    public List<Game> listGames() throws DataAccessException {
        return dataAccess.getAllGames();
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param gameId The ID of the game.
     * @return The Game object.
     * @throws DataAccessException If the game is not found.
     */
    public Game getGame(int gameId) throws DataAccessException {
        Game game = dataAccess.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Game not found.");
        }
        return game;
    }

    public void removePlayerFromGame(int gameId, String username) throws DataAccessException {
        Game game = getGame(gameId);
        boolean changed = false;

        if (game.getWhiteUsername() != null && game.getWhiteUsername().equals(username)) {
            game.setWhiteUsername(null);
            changed = true;
        }
        if (game.getBlackUsername() != null && game.getBlackUsername().equals(username)) {
            game.setBlackUsername(null);
            changed = true;
        }

        if (changed) {
            dataAccess.updateGame(game);
        }
    }
}