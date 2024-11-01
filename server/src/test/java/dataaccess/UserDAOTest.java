package dataaccess;

import models.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private DataAccessMySQLImpl dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessMySQLImpl();
        dataAccess.deleteAllGames();
        dataAccess.deleteAllUsers();

    }

    @Test
    public void testCreateUserSuccess() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        dataAccess.createUser(user);
        User retrievedUser = dataAccess.getUser("testuser");
        assertNotNull(retrievedUser);
        assertEquals(user.getUsername(), retrievedUser.getUsername());
        assertEquals(user.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void testCreateUserDuplicate() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        dataAccess.createUser(user);
        assertThrows(UserAlreadyExistsException.class, () -> dataAccess.createUser(user));
    }

    @Test
    public void testGetUserNotFound() throws DataAccessException {
        User user = dataAccess.getUser("nonexistent");
        assertNull(user);
    }

    @Test
    public void testDeleteAllUsers() throws DataAccessException {
        User user1 = new User("user1", "pass1", "user1@example.com");
        User user2 = new User("user2", "pass2", "user2@example.com");
        dataAccess.createUser(user1);
        dataAccess.createUser(user2);
        dataAccess.deleteAllUsers();
        assertNull(dataAccess.getUser("user1"));
        assertNull(dataAccess.getUser("user2"));
    }
}
