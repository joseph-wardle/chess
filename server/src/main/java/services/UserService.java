package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import models.User;
import models.AuthToken;

import java.util.UUID;

public class UserService {
    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthToken register(User user) throws DataAccessException {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            throw new DataAccessException("Missing required fields.");
        }
        dataAccess.createUser(user);
        // Create AuthToken
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, user.getUsername());
        dataAccess.createAuth(auth);
        return auth;
    }

    public AuthToken login(String username, String password) throws DataAccessException {
        User user = dataAccess.getUser(username);
        if (user == null || !user.getPassword().equals(password)) { // Use hashed passwords in production
            throw new DataAccessException("Invalid username or password.");
        }
        // Create new AuthToken
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, username);
        dataAccess.createAuth(auth);
        return auth;
    }

    public void logout(String token) throws DataAccessException {
        AuthToken auth = dataAccess.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("Invalid auth token.");
        }
        dataAccess.deleteAuth(token);
    }

    public void clearData() throws DataAccessException {
        dataAccess.deleteAllUsers();
        dataAccess.deleteAllGames();
        dataAccess.deleteAllAuthTokens();
    }
}