package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clearData();
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        assertNotNull(auth);
        assertNotNull(auth.getToken());
        assertEquals("testuser", auth.getUsername());
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.register("testuser", "password", "email@example.com");
            facade.register("testuser", "password", "email@example.com");
        });
        String expectedMessage = "User already exists";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        facade.register("testuser", "password", "email@example.com");
        AuthToken auth = facade.login("testuser", "password");
        assertNotNull(auth);
        assertNotNull(auth.getToken());
        assertEquals("testuser", auth.getUsername());
    }

    @Test
    public void testLoginInvalidCredentials() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.login("nonexistentuser", "wrongpassword");
        });
        String expectedMessage = "Invalid username or password";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        facade.setAuthToken(auth.getToken());
        facade.logout();
        assertNull(facade.getAuthToken());
    }

    @Test
    public void testCreateGameSuccess() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        facade.setAuthToken(auth.getToken());
        Game game = facade.createGame("Test Game");
        assertNotNull(game);
        assertEquals("Test Game", game.getGameName());
    }

    @Test
    public void testCreateGameWithoutAuth() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.createGame("Test Game");
        });
        String expectedMessage = "Invalid auth token";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testListGamesSuccess() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        facade.setAuthToken(auth.getToken());
        facade.createGame("Test Game");
        List<Game> games = facade.listGames();
        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals("Test Game", games.get(0).getGameName());
    }

    @Test
    public void testListGamesWithoutAuth() {
        Exception exception = assertThrows(Exception.class, () -> {
            facade.listGames();
        });
        String expectedMessage = "Invalid auth token";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testJoinGameSuccess() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        facade.setAuthToken(auth.getToken());
        Game game = facade.createGame("Test Game");
        facade.joinGame(game.getGameID(), "white");
        // No exception thrown, success
        assertTrue(true);
    }

    @Test
    public void testJoinGameInvalidColor() throws Exception {
        AuthToken auth = facade.register("testuser", "password", "email@example.com");
        facade.setAuthToken(auth.getToken());
        Game game = facade.createGame("Test Game");
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(game.getGameID(), "purple");
        });
        String expectedMessage = "Invalid player color";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testJoinGameColorAlreadyTaken() throws Exception {
        // First user joins as white
        AuthToken auth1 = facade.register("user1", "password", "email1@example.com");
        facade.setAuthToken(auth1.getToken());
        Game game = facade.createGame("Test Game");
        facade.joinGame(game.getGameID(), "white");

        // Second user tries to join as white
        AuthToken auth2 = facade.register("user2", "password", "email2@example.com");
        facade.setAuthToken(auth2.getToken());
        Exception exception = assertThrows(Exception.class, () -> {
            facade.joinGame(game.getGameID(), "white");
        });
        String expectedMessage = "White player is already taken";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}
