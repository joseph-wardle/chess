package services;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.InvalidAuthTokenException;
import models.AuthToken;

public class AuthService {
    private DataAccess dataAccess;

    public AuthService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthToken authenticate(String token) throws DataAccessException {
        AuthToken auth = dataAccess.getAuth(token);
        if (auth == null) {
            throw new InvalidAuthTokenException("Invalid auth token.");
        }
        return auth;
    }
}