package dataaccess;

import models.AuthToken;
import models.Game;
import models.User;

import java.sql.SQLException;
import java.util.List;

public class DataAccessMySQLImpl implements DataAccess {

    public DataAccessMySQLImpl() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            // Create Users table
            String createUserTable = """
                    CREATE TABLE IF NOT EXISTS User (
                        username VARCHAR(255) PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    );
                    """;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createUserTable);
            }

            // Create AuthToken table
            String createAuthTable = """
                    CREATE TABLE IF NOT EXISTS AuthToken (
                        token VARCHAR(255) PRIMARY KEY,
                        username VARCHAR(255),
                        FOREIGN KEY (username) REFERENCES User(username)
                    );
                    """;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createAuthTable);
            }

            // Create Game table
            String createGameTable = """
                    CREATE TABLE IF NOT EXISTS Game (
                        gameID INT AUTO_INCREMENT PRIMARY KEY,
                        gameName VARCHAR(255) NOT NULL,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        FOREIGN KEY (whiteUsername) REFERENCES User(username),
                        FOREIGN KEY (blackUsername) REFERENCES User(username)
                    );
                    """;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createGameTable);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }

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
