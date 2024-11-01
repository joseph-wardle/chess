package dataaccess;

import models.Game;
import models.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class GameDAOTest {

    private DataAccessMySQLImpl dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessMySQLImpl();
        dataAccess.deleteAllGames();
        dataAccess.deleteAllUsers();
        // Create users for testing
        User user1 = new User("user1", "pass1", "user1@example.com");
        User user2 = new User("user2", "pass2", "user2@example.com");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        Game game = new Game(-1, "Test Game", "user1", "user2");
        dataAccess.createGame(game);
        assertTrue(game.getGameID() > 0);
        Game retrievedGame = dataAccess.getGame(game.getGameID());
        assertNotNull(retrievedGame);
        assertEquals(game.getGameName(), retrievedGame.getGameName());
    }

    @Test
    public void testGetGameNotFound() throws DataAccessException {
        Game game = dataAccess.getGame(999);
        assertNull(game);
    }

    @Test
    public void testUpdateGameSuccess() throws DataAccessException {
        Game game = new Game(-1, "Test Game", "user1", null);
        dataAccess.createGame(game);
        game.setBlackUsername("user2");
        dataAccess.updateGame(game);
        Game updatedGame = dataAccess.getGame(game.getGameID());
        assertEquals("user2", updatedGame.getBlackUsername());
    }

    @Test
    public void testUpdateGameNotFound() throws DataAccessException {
        Game game = new Game(999, "Nonexistent Game", null, null);
        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(game));
    }

    @Test
    public void testGetAllGames() throws DataAccessException {
        Game game1 = new Game(-1, "Game 1", "user1", null);
        Game game2 = new Game(-1, "Game 2", null, "user2");
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);
        List<Game> games = dataAccess.getAllGames();
        assertEquals(2, games.size());
    }

    @Test
    public void testDeleteAllGames() throws DataAccessException {
        Game game1 = new Game(-1, "Game 1", "user1", null);
        Game game2 = new Game(-1, "Game 2", null, "user2");
        dataAccess.createGame(game1);
        dataAccess.createGame(game2);
        dataAccess.deleteAllGames();
        List<Game> games = dataAccess.getAllGames();
        assertTrue(games.isEmpty());
    }
}
