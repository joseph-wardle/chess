package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.*;
import models.*;
import services.GameService;
import services.UserService;

import java.util.List;

public class GameServiceTest {

    private DataAccess dataAccess;
    private GameService gameService;
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessImpl();
        gameService = new GameService(dataAccess);
        userService = new UserService(dataAccess);
        userService.clearData();
    }

    @Test
    public void createGame_Success() throws DataAccessException {
        String gameName = "Test Game";
        Game game = gameService.createGame(gameName);

        assertNotNull(game, "Created game should not be null");
        assertTrue(game.getGameID() > 0, "GameID should be positive");
        assertEquals(gameName, game.getGameName(), "Game name should match");

        Game retrievedGame = dataAccess.getGame(game.getGameID());
        assertEquals(game, retrievedGame, "Retrieved game should match the created game");
    }

    @Test
    public void createGame_InvalidName() throws DataAccessException {
        String gameName = "";

        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(gameName);
        }, "Creating a game with an empty name should throw DataAccessException");
        assertEquals("Game name is required.", exception.getMessage());
    }

    @Test
    public void joinGame_Success() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        AuthToken auth = userService.register(user);

        Game game = gameService.createGame("Test Game");

        gameService.joinGame(game.getGameID(), "testuser", "white");

        Game updatedGame = dataAccess.getGame(game.getGameID());
        assertEquals("testuser", updatedGame.getWhiteUsername(), "White username should match");
        assertNull(updatedGame.getBlackUsername(), "Black username should be null");
    }

    @Test
    public void joinGame_GameNotFound() throws DataAccessException {
        String username = "testuser";
        String playerColor = "white";
        int invalidGameId = 999;

        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(invalidGameId, username, playerColor);
        }, "Joining a non-existent game should throw DataAccessException");
        assertEquals("Game not found.", exception.getMessage());
    }

    @Test
    public void joinGame_InvalidPlayerColor() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        AuthToken auth = userService.register(user);

        Game game = gameService.createGame("Test Game");

        String invalidColor = "green";

        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(game.getGameID(), "testuser", invalidColor);
        }, "Joining a game with an invalid player color should throw DataAccessException");
        assertEquals("Invalid player color.", exception.getMessage());
    }

    @Test
    public void joinGame_ColorAlreadyTaken() throws DataAccessException {
        User user1 = new User("user1", "password1", "user1@example.com");
        AuthToken auth1 = userService.register(user1);

        User user2 = new User("user2", "password2", "user2@example.com");
        AuthToken auth2 = userService.register(user2);

        Game game = gameService.createGame("Test Game");

        gameService.joinGame(game.getGameID(), "user1", "white");

        Exception exception = assertThrows(TeamColorAlreadyTakenException.class, () -> {
            gameService.joinGame(game.getGameID(), "user2", "white");
        }, "Joining a game with an already taken color should throw TeamColorAlreadyTakenException");
        assertEquals("White player is already taken.", exception.getMessage());
    }

    @Test
    public void listGames_Success() throws DataAccessException {
        Game game1 = gameService.createGame("Game 1");
        Game game2 = gameService.createGame("Game 2");

        List<Game> games = gameService.listGames();
        assertEquals(2, games.size(), "There should be two games listed");
        assertTrue(games.contains(game1), "List should contain Game 1");
        assertTrue(games.contains(game2), "List should contain Game 2");
    }

    @Test
    public void listGames_NoGames() throws DataAccessException {
        List<Game> games = gameService.listGames();
        assertNotNull(games, "Games list should not be null");
        assertTrue(games.isEmpty(), "Games list should be empty when no games are created");
    }

    @Test
    public void getGame_Success() throws DataAccessException {
        Game game = gameService.createGame("Test Game");
        Game retrievedGame = gameService.getGame(game.getGameID());

        assertEquals(game, retrievedGame, "Retrieved game should match the created game");
    }

    @Test
    public void getGame_NotFound() throws DataAccessException {
        int invalidGameId = 999;

        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.getGame(invalidGameId);
        }, "Retrieving a non-existent game should throw DataAccessException");
        assertEquals("Game not found.", exception.getMessage());
    }
}