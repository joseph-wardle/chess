package models;

import java.util.Objects;

/**
 * Represents an authentication token associated with a user.
 */
public class AuthToken {
    private String token;
    private String username;

    public AuthToken(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}