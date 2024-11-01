package dataaccess;

import models.AuthToken;
import models.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenDAOTest {

    private DataAccessMySQLImpl dataAccess;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessMySQLImpl();
        dataAccess.deleteAllAuthTokens();
        dataAccess.deleteAllUsers();
        // Create a user for testing
        User user = new User("testuser", "password123", "test@example.com");
        dataAccess.createUser(user);
    }

    @Test
    public void testCreateAuthTokenSuccess() throws DataAccessException {
        AuthToken authToken = new AuthToken("token123", "testuser");
        dataAccess.createAuth(authToken);
        AuthToken retrievedAuth = dataAccess.getAuth("token123");
        assertNotNull(retrievedAuth);
        assertEquals(authToken.getToken(), retrievedAuth.getToken());
        assertEquals(authToken.getUsername(), retrievedAuth.getUsername());
    }

    @Test
    public void testGetAuthTokenNotFound() throws DataAccessException {
        AuthToken authToken = dataAccess.getAuth("nonexistent");
        assertNull(authToken);
    }

    @Test
    public void testDeleteAuthToken() throws DataAccessException {
        AuthToken authToken = new AuthToken("token123", "testuser");
        dataAccess.createAuth(authToken);
        dataAccess.deleteAuth("token123");
        AuthToken retrievedAuth = dataAccess.getAuth("token123");
        assertNull(retrievedAuth);
    }

    @Test
    public void testDeleteAllAuthTokens() throws DataAccessException {
        AuthToken authToken1 = new AuthToken("token1", "testuser");
        AuthToken authToken2 = new AuthToken("token2", "testuser");
        dataAccess.createAuth(authToken1);
        dataAccess.createAuth(authToken2);
        dataAccess.deleteAllAuthTokens();
        assertNull(dataAccess.getAuth("token1"));
        assertNull(dataAccess.getAuth("token2"));
    }
}
