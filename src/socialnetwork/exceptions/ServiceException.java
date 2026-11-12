package socialnetwork.exceptions;

/**
 * Thrown for the special situations of the application logic.
 * <p>
 * Examples: the user does not exist, two users are already friends or are
 * not friends, the network has no users. It is an unchecked exception.
 */
public class ServiceException extends RuntimeException {

    /**
     * Creates a service exception with the given message.
     * @param message the description of the situation
     */
    public ServiceException(String message) {
        super(message);
    }
}