package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import models.Game;
import models.AuthToken;

import java.util.List;

public class GameService {
    private DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Game createGame(String gameName, String username) throws DataAccessException {
        Game game = new Game();
        game.setGameName(gameName);
        game.setWhiteUsername(username); // Assuming creator is white by default
        dataAccess.createGame(game);
        return game;
    }

    public void joinGame(int gameId, String username, String playerColor) throws DataAccessException {
        Game game = dataAccess.getGame(gameId);
        if (game == null) {
            throw new DataAccessException("Game not found.");
        }
        if (playerColor.equalsIgnoreCase("white")) {
            if (game.getWhiteUsername() != null) {
                throw new DataAccessException("White player already joined.");
            }
            game.setWhiteUsername(username);
        } else if (playerColor.equalsIgnoreCase("black")) {
            if (game.getBlackUsername() != null) {
                throw new DataAccessException("Black player already joined.");
            }
            game.setBlackUsername(username);
        } else {
            throw new DataAccessException("Invalid player color.");
        }
        dataAccess.updateGame(game);
    }

    public List<Game> listGames() throws DataAccessException {
        return dataAccess.getAllGames();
    }
}