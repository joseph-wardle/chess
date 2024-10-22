package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.*;
import models.*;
import services.AuthService;
import services.UserService;

public class AuthServiceTest {

    private DataAccess dataAccess;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DataAccessImpl();
        userService = new UserService(dataAccess);
        authService = new AuthService(dataAccess);
        userService.clearData();
    }

    @Test
    public void authenticateSuccess() throws DataAccessException {
        User user = new User("testuser", "password123", "test@example.com");
        AuthToken auth = userService.register(user);

        AuthToken authenticated = authService.authenticate(auth.getToken());
        assertNotNull(authenticated, "Authenticated AuthToken should not be null");
        assertEquals(auth, authenticated, "Authenticated AuthToken should match the created AuthToken");
    }

    @Test
    public void authenticateInvalidToken() throws DataAccessException {
        String invalidToken = "invalidtoken123";

        Exception exception = assertThrows(InvalidAuthTokenException.class, () -> {
            authService.authenticate(invalidToken);
        }, "Authenticating with an invalid token should throw InvalidAuthTokenException");
        assertEquals("Invalid auth token.", exception.getMessage());
    }
}