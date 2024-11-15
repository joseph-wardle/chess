package models;

/**
 * Represents an authentication token associated with a user.
 */
public class AuthTokenClientModel {
    private String token;
    private String username;

    public AuthTokenClientModel(String token, String username) {
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