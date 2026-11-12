package socialnetwork.domain.validators;

/**
 * Thrown when an entity does not respect the validation rules.
 * <p>
 * It is an unchecked exception ({@link RuntimeException}), so it is documented
 * with {@code @throws} but not declared in the {@code throws} clause of the
 * methods. It is handled in the user interface.
 */
public class ValidationException extends RuntimeException {

    /**
     * Creates a validation exception with the given message.
     * @param message the description of all the errors found
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a validation exception with the given message and cause.
     * @param message the description of the error
     * @param cause   the original exception that produced the error
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}