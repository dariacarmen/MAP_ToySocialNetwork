package socialnetwork.exceptions;

/**
 * Thrown for the errors of the persistence layer and of its configuration.
 * <p>
 * Examples: a data file cannot be read or written, a line of a data file is
 * corrupted, the configuration file is missing or has a missing or unknown
 * value. When it replaces another exception ({@link java.io.IOException},
 * {@link NumberFormatException}, {@link java.time.format.DateTimeParseException}
 * etc.), the original exception is kept as the cause (exception chaining).
 * It is an unchecked exception.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Creates a repository exception with the given message.
     * @param message the description of the error
     */
    public RepositoryException(String message) {
        super(message);
    }
    /**
     * Creates a repository exception with the given message and cause.
     * @param message the description of the error
     * @param cause   the original exception that produced the error
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}