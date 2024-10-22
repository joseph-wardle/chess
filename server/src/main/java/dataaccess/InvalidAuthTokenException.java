package dataaccess;

/**
 * Indicates that an authentication token is invalid.
 */
public class InvalidAuthTokenException extends DataAccessException {
    public InvalidAuthTokenException(String message) {
        super(message);
    }
}