package dataaccess;

/**
 * Indicates that a user already exists in the database.
 */
public class UserAlreadyExistsException extends DataAccessException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}