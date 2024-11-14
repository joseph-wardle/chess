package models;

/**
 * Represents authentication data returned by the server.
 */
public class AuthToken {
    private final String username;
    private final String authToken;

    public AuthToken(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return authToken;
    }
}
