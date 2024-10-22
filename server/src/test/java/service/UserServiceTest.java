package java.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.*;
import models.*;
import services.UserService;

import java.util.List;

public class UserServiceTest {

    private DataAccess dataAccess;
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessImpl();
        userService = new UserService(dataAccess);
        userService.clearData();
    }

    @Test
    public void register_Success() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        AuthToken auth = userService.register(user);

        assertNotNull(auth, "AuthToken should not be null");
        assertNotNull(auth.getToken(), "AuthToken token should not be null");
        assertEquals("testuser", auth.getUsername(), "AuthToken username should match");

        User retrievedUser = dataAccess.getUser("testuser");
        assertEquals(user, retrievedUser, "Retrieved user should match the registered user");

        AuthToken retrievedAuth = dataAccess.getAuth(auth.getToken());
        assertEquals(auth, retrievedAuth, "Retrieved AuthToken should match the created AuthToken");
    }

    @Test
    public void register_UserAlreadyExists() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        userService.register(user);

        User duplicateUser = new User("testuser", "newpassword", "new@example.com");
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(duplicateUser);
        }, "Registering a duplicate user should throw UserAlreadyExistsException");
    }

    @Test
    public void login_Success() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        userService.register(user);

        AuthToken auth = userService.login("testuser", "password123");
        assertNotNull(auth, "AuthToken should not be null");
        assertNotNull(auth.getToken(), "AuthToken token should not be null");
        assertEquals("testuser", auth.getUsername(), "AuthToken username should match");

        AuthToken retrievedAuth = dataAccess.getAuth(auth.getToken());
        assertEquals(auth, retrievedAuth, "Retrieved AuthToken should match the created AuthToken");
    }

    @Test
    public void login_InvalidCredentials() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        userService.register(user);

        Exception exception1 = assertThrows(DataAccessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        }, "Logging in with incorrect password should throw DataAccessException");
        assertEquals("Invalid username or password.", exception1.getMessage());

        Exception exception2 = assertThrows(DataAccessException.class, () -> {
            userService.login("nonexistent", "password123");
        }, "Logging in with non-existent user should throw DataAccessException");
        assertEquals("Invalid username or password.", exception2.getMessage());
    }

    @Test
    public void logout_Success() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        AuthToken auth = userService.register(user);

        userService.logout(auth.getToken());

        AuthToken retrievedAuth = dataAccess.getAuth(auth.getToken());
        assertNull(retrievedAuth, "AuthToken should be null after logout");
    }

    @Test
    public void logout_InvalidToken() throws DataAccessException {
        String invalidToken = "invalidtoken123";

        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.logout(invalidToken);
        }, "Logging out with invalid token should throw DataAccessException");
        assertEquals("Invalid auth token.", exception.getMessage());
    }

    @Test
    public void clearData_Success() throws DataAccessException {
        User user1 = new User("user1", "pass1", "user1@example.com");
        User user2 = new User("user2", "pass2", "user2@example.com");
        userService.register(user1);
        userService.register(user2);

        AuthToken auth1 = userService.login("user1", "pass1");
        AuthToken auth2 = userService.login("user2", "pass2");

        userService.clearData();

        assertNull(dataAccess.getUser("user1"), "User1 should be null after clearing data");
        assertNull(dataAccess.getUser("user2"), "User2 should be null after clearing data");

        assertNull(dataAccess.getAuth(auth1.getToken()), "AuthToken1 should be null after clearing data");
        assertNull(dataAccess.getAuth(auth2.getToken()), "AuthToken2 should be null after clearing data");

        List<Game> games = dataAccess.getAllGames();
        assertTrue(games.isEmpty(), "All games should be cleared");
    }
}