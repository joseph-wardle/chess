package client;

import org.junit.jupiter.api.*;
import server.Server;
import models.AuthToken;
import models.Game;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clearDatabase();
    }

    /**
     * Positive test for the register method.
     */
    @Test
    void testRegisterSuccess() throws Exception {
        AuthToken authData = facade.register("player1", "password", "p1@email.com");
        assertTrue(authData.getToken().length() > 10);
    }

    /**
     * Negative test: Registering a user that already exists.
     */
    @Test
    void testRegisterUserAlreadyExists() throws Exception {
        facade.register("player1", "password", "p1@example.com");
        Exception exception = assertThrows(Exception.class, () -> {
            facade.register("player1", "password", "p1@example.com");
        });
        String expectedMessage = "User already exists";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Positive test for the login method.
     */
    @Test
    void testLoginSuccess() throws Exception {
        facade.register("player1", "password", "p1@example.com");
        var authData = facade.login("player1", "password");
        assertNotNull(authData);
        assertNotNull(authData.getToken());
        assertEquals("player1", authData.getUsername());
    }

    /**
     * Negative test: Logging in with invalid credentials.
     */
    @Test
    void testLoginInvalidCredentials() throws Exception {
        facade.register("player1", "password", "p1@example.com");
        Exception exception = assertThrows(Exception.class, () -> {
            facade.login("player1", "wrongpassword");
        });
        String expectedMessage = "Invalid username or password";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Negative test: Logging in with a non-existent user.
     */
    @Test
    void testLoginNonExistentUser() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.login("nonexistent", "password");
        });
        String expectedMessage = "Invalid username or password";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Positive test for the logout method.
     */
    @Test
    void testLogoutSuccess() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        facade.logout(authToken);
        // No exception means success
        assertTrue(true);
    }

    /**
     * Negative test: Logging out with an invalid auth token.
     */
    @Test
    void testLogoutInvalidToken() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.logout("invalidtoken");
        });
        String expectedMessage = "Invalid auth token";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Positive test for the createGame method.
     */
    @Test
    void testCreateGameSuccess() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        var game = facade.createGame(authToken, "TestGame");
        assertNotNull(game);
        assertTrue(game.getGameID() > 0);
        assertEquals("TestGame", game.getGameName());
    }

    /**
     * Negative test: Creating a game without authentication.
     */
    @Test
    void testCreateGameNoAuth() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.createGame(null, "TestGame");
        });
        String expectedMessage = "Invalid auth token";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Positive test for the listGames method.
     */
    @Test
    void testListGamesSuccess() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        facade.createGame(authToken, "Game1");
        facade.createGame(authToken, "Game2");
        List<Game> games = facade.listGames(authToken);
        assertEquals(2, games.size());
        assertEquals("Game1", games.get(0).getGameName());
        assertEquals("Game2", games.get(1).getGameName());
    }

    /**
     * Negative test: Listing games without authentication.
     */
    @Test
    void testListGamesNoAuth() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.listGames(null);
        });
        String expectedMessage = "Invalid auth token";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Positive test for the joinGame method.
     */
    @Test
    void testJoinGameSuccess() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        var game = facade.createGame(authToken, "TestGame");
        facade.joinGame(authToken, game.getGameID(), "white");
        // No exception means success
        assertTrue(true);
    }

    /**
     * Negative test: Joining a game where the color is already taken.
     */
    @Test
    void testJoinGameColorAlreadyTaken() throws Exception {
        // First user joins as white
        var authData1 = facade.register("player1", "password", "p1@example.com");
        String authToken1 = authData1.getToken();var game = facade.createGame(authToken1, "TestGame");
        facade.joinGame(authToken1, game.getGameID(), "white");

        // Second user tries to join as white
        var authData2 = facade.register("player2", "password", "p2@example.com");
        String authToken2 = authData2.getToken();
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(authToken2, game.getGameID(), "white");
        });
        String expectedMessage = "White player is already taken";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Negative test: Joining a game with an invalid color.
     */
    @Test
    void testJoinGameInvalidColor() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        var game = facade.createGame(authToken, "TestGame");
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(authToken, game.getGameID(), "purple");
        });
        String expectedMessage = "Invalid player color";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    /**
     * Negative test: Joining a game without authentication.
     */
    @Test
    void testJoinGameNoAuth() throws Exception {
        var authData = facade.register("player1", "password", "p1@example.com");
        String authToken = authData.getToken();
        var game = facade.createGame(authToken, "TestGame");
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(null, game.getGameID(), "white");
        });
        String expectedMessage = "Invalid auth token";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }
}
