package dataaccess;

import models.User;
import models.Game;
import models.AuthToken;
import java.util.List;

public interface DataAccess {
    // User operations
    User getUser(String username) throws DataAccessException;
    void createUser(User user) throws DataAccessException;

    // AuthToken operations
    AuthToken getAuth(String token) throws DataAccessException;
    void createAuth(AuthToken auth) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;
    void deleteAllAuthTokens() throws DataAccessException;

    // Game operations
    Game getGame(int gameId) throws DataAccessException;
    void createGame(Game game) throws DataAccessException;
    void updateGame(Game game) throws DataAccessException;
    List<Game> getAllGames() throws DataAccessException;
    void deleteAllGames() throws DataAccessException;

    // Clear operations
    void deleteAllUsers() throws DataAccessException;
}