package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import models.User;
import models.AuthToken;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * Service for handling user-related operations.
 */
public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Registers a new user and creates an authentication token.
     *
     * @param user The user to register.
     * @return The generated AuthToken.
     * @throws DataAccessException If required fields are missing or the user already exists.
     */
    public AuthToken register(User user) throws DataAccessException {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            throw new DataAccessException("Missing required fields.");
        }
        dataAccess.createUser(user);
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, user.getUsername());
        dataAccess.createAuth(auth);
        return auth;
    }

    /**
     * Logs in a user by verifying credentials and generating a new authentication token.
     *
     * @param username The username.
     * @param password The password.
     * @return The generated AuthToken.
     * @throws DataAccessException If the username or password is invalid.
     */
    public AuthToken login(String username, String password) throws DataAccessException {
        User user = dataAccess.getUser(username);
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new DataAccessException("Invalid username or password.");
        }
        String token = UUID.randomUUID().toString();
        AuthToken auth = new AuthToken(token, username);
        dataAccess.createAuth(auth);
        return auth;
    }

    /**
     * Logs out a user by deleting their authentication token.
     *
     * @param token The authentication token.
     * @throws DataAccessException If the token is invalid.
     */
    public void logout(String token) throws DataAccessException {
        AuthToken auth = dataAccess.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("Invalid auth token.");
        }
        dataAccess.deleteAuth(token);
    }

    /**
     * Clears all application data, including users, games, and authentication tokens.
     *
     * @throws DataAccessException If an error occurs during data clearing.
     */
    public void clearData() throws DataAccessException {
        dataAccess.deleteAllUsers();
        dataAccess.deleteAllGames();
        dataAccess.deleteAllAuthTokens();
    }
}