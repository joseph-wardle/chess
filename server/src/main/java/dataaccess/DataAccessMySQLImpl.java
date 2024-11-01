package dataaccess;

import models.AuthToken;
import models.Game;
import models.User;

import java.util.List;

public class DataAccessMySQLImpl implements DataAccess {
    @Override
    public User getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(User user) throws DataAccessException {

    }

    @Override
    public AuthToken getAuth(String token) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthToken auth) throws DataAccessException {

    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {

    }

    @Override
    public void deleteAllAuthTokens() throws DataAccessException {

    }

    @Override
    public Game getGame(int gameId) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(Game game) throws DataAccessException {

    }

    @Override
    public void updateGame(Game game) throws DataAccessException {

    }

    @Override
    public List<Game> getAllGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void deleteAllGames() throws DataAccessException {

    }

    @Override
    public void deleteAllUsers() throws DataAccessException {

    }
}
