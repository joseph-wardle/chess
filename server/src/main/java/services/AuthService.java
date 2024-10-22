package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InvalidAuthTokenException;
import models.AuthToken;

/**
 * Service for handling authentication-related operations.
 */
public class AuthService {
    private final DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Authenticates a user based on the provided token.
     *
     * @param token The authentication token.
     * @return The associated AuthToken.
     * @throws DataAccessException If the token is invalid or an error occurs.
     */
    public AuthToken authenticate(String token) throws DataAccessException {
        AuthToken auth = dataAccess.getAuth(token);
        if (auth == null) {
            throw new InvalidAuthTokenException("Invalid auth token.");
        }
        return auth;
    }
}