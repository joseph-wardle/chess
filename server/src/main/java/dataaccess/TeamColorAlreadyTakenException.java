package dataaccess;

/**
 * Indicates that the desired team color is already taken in the game.
 */
public class TeamColorAlreadyTakenException extends DataAccessException {
    public TeamColorAlreadyTakenException(String message) {
        super(message);
    }
}