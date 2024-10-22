package dataaccess;

import models.User;
import models.Game;
import models.AuthToken;
import java.util.List;

/**
 * Interface for data access operations.
 */
public interface DataAccess {
    User getUser(String username) throws DataAccessException;
    void createUser(User user) throws DataAccessException;

    AuthToken getAuth(String token) throws DataAccessException;
    void createAuth(AuthToken auth) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;
    void deleteAllAuthTokens() throws DataAccessException;

    Game getGame(int gameId) throws DataAccessException;
    void createGame(Game game) throws DataAccessException;
    void updateGame(Game game) throws DataAccessException;
    List<Game> getAllGames() throws DataAccessException;
    void deleteAllGames() throws DataAccessException;

    void deleteAllUsers() throws DataAccessException;
}