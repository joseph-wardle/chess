package dataaccess;

import models.User;
import models.Game;
import models.AuthToken;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * MySQL implementation of the DataAccess interface.
 */
public class DataAccessMySQLImpl implements DataAccess {

    public DataAccessMySQLImpl() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            // Create User table
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

            // Create Game table with ON DELETE SET NULL
            String createGameTable = """
                CREATE TABLE IF NOT EXISTS Game (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    gameName VARCHAR(255) NOT NULL,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    FOREIGN KEY (whiteUsername) REFERENCES User(username) ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES User(username) ON DELETE SET NULL
                );
                """;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createGameTable);
            }

            // Create AuthToken table
            String createAuthTable = """
                CREATE TABLE IF NOT EXISTS AuthToken (
                    token VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255),
                    FOREIGN KEY (username) REFERENCES User(username) ON DELETE CASCADE
                );
                """;
            try (var stmt = conn.createStatement()) {
                stmt.execute(createAuthTable);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }

    // User operations
    @Override
    public User getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM User WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createUser(User user) throws DataAccessException {
        if (getUser(user.getUsername()) != null) {
            throw new UserAlreadyExistsException("User already exists.");
        }
        String sql = "INSERT INTO User (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Password is already hashed
            stmt.setString(3, user.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    // AuthToken operations
    @Override
    public AuthToken getAuth(String token) throws DataAccessException {
        String sql = "SELECT * FROM AuthToken WHERE token = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthToken(
                            rs.getString("token"),
                            rs.getString("username")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth token: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createAuth(AuthToken auth) throws DataAccessException {
        String sql = "INSERT INTO AuthToken (token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auth.getToken());
            stmt.setString(2, auth.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM AuthToken WHERE token = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllAuthTokens() throws DataAccessException {
        String sql = "DELETE FROM AuthToken";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting all auth tokens: " + e.getMessage());
        }
    }

    // Game operations
    @Override
    public Game getGame(int gameId) throws DataAccessException {
        String sql = "SELECT * FROM Game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Game(
                            rs.getInt("gameID"),
                            rs.getString("gameName"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void createGame(Game game) throws DataAccessException {
        String sql = "INSERT INTO Game (gameName, whiteUsername, blackUsername) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, game.getGameName());
            stmt.setString(2, game.getWhiteUsername());
            stmt.setString(3, game.getBlackUsername());
            stmt.executeUpdate();

            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    game.setGameID(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(Game game) throws DataAccessException {
        String sql = "UPDATE Game SET gameName = ?, whiteUsername = ?, blackUsername = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getGameName());
            stmt.setString(2, game.getWhiteUsername());
            stmt.setString(3, game.getBlackUsername());
            stmt.setInt(4, game.getGameID());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game not found.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public List<Game> getAllGames() throws DataAccessException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM Game";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                games.add(new Game(
                        rs.getInt("gameID"),
                        rs.getString("gameName"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void deleteAllGames() throws DataAccessException {
        String sql = "DELETE FROM Game";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting all games: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllUsers() throws DataAccessException {
        String sql = "DELETE FROM User";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting all users: " + e.getMessage());
        }
    }
}
