package dataaccess;

import models.User;
import models.Game;
import models.AuthToken;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataAccessImpl implements DataAccess {
    private Map<String, User> users = new ConcurrentHashMap<>();
    private Map<String, AuthToken> authTokens = new ConcurrentHashMap<>();
    private Map<Integer, Game> games = new ConcurrentHashMap<>();
    private int gameIdCounter = 1;

    @Override
    public User getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void createUser(User user) throws DataAccessException {
        if (users.containsKey(user.getUsername())) {
            throw new DataAccessException("User already exists.");
        }
        users.put(user.getUsername(), user);
    }

    @Override
    public AuthToken getAuth(String token) throws DataAccessException {
        return authTokens.get(token);
    }

    @Override
    public void createAuth(AuthToken auth) throws DataAccessException {
        authTokens.put(auth.getToken(), auth);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        authTokens.remove(token);
    }

    @Override
    public void deleteAllAuthTokens() throws DataAccessException {
        authTokens.clear();
    }

    @Override
    public Game getGame(int gameId) throws DataAccessException {
        return games.get(gameId);
    }

    @Override
    public void createGame(Game game) throws DataAccessException {
        game.setGameId(gameIdCounter++);
        games.put(game.getGameId(), game);
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        if (!games.containsKey(game.getGameId())) {
            throw new DataAccessException("Game not found.");
        }
        games.put(game.getGameId(), game);
    }

    @Override
    public List<Game> getAllGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void deleteAllGames() throws DataAccessException {
        games.clear();
    }

    @Override
    public void deleteAllUsers() throws DataAccessException {
        users.clear();
    }
}