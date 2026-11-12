package socialnetwork.domain.validators;

import socialnetwork.domain.Utilizator;

/**
 * Concrete validation strategy for {@link Utilizator}.
 * <p>
 * A user is valid if:
 * <ul>
 *     <li>the id is not null and positive;</li>
 *     <li>the first name is not empty and starts with an upper case letter;</li>
 *     <li>the last name is not empty and starts with an upper case letter.</li>
 * </ul>
 * All the errors are reported at once, in a single exception.
 */
public class UtilizatorValidator implements Validator<Utilizator> {

    /**
     * Creates a validator for users.
     */
    public UtilizatorValidator() {
    }

    /**
     * Validates a user.
     * @param u the user to validate
     * @throws ValidationException if the user is not valid; the message
     *                             contains all the errors found
     */
    @Override
    public void validate(Utilizator u) {
        String errors = "";
        if (u.getId() == null || u.getId() <= 0) {
            errors += "Id-ul trebuie sa fie nenul si pozitiv. ";
        }
        if (u.getFirstName() == null || u.getFirstName().isEmpty()
                || !Character.isUpperCase(u.getFirstName().charAt(0))) {
            errors += "Prenumele trebuie sa fie nevid si sa inceapa cu litera mare. ";
        }
        if (u.getLastName() == null || u.getLastName().isEmpty()
                || !Character.isUpperCase(u.getLastName().charAt(0))) {
            errors += "Numele trebuie sa fie nevid si sa inceapa cu litera mare. ";
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.trim());
        }
    }
}