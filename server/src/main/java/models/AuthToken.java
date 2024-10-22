package models;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthToken authToken = (AuthToken) o;
        return Objects.equals(token, authToken.token) && Objects.equals(username, authToken.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, username);
    }

    @Override
    public String toString() {
        return "AuthToken{" +
                "token='" + token + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}