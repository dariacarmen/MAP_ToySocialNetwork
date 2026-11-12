package socialnetwork.domain.validators;

import socialnetwork.domain.Prietenie;

/**
 * Concrete validation strategy for {@link Prietenie}.
 * <p>
 * A friendship is valid if:
 * <ul>
 *     <li>both user ids are not null;</li>
 *     <li>the two users are different (nobody is friends with himself);</li>
 *     <li>the date of the friendship is set.</li>
 * </ul>
 * All the errors are reported at once, in a single exception.
 */
public class PrietenieValidator implements Validator<Prietenie> {

    /**
     * Creates a validator for friendships.
     */
    public PrietenieValidator() {
    }

    /**
     * Validates a friendship.
     * @param p the friendship to validate
     * @throws ValidationException if the friendship is not valid; the message
     *                             contains all the errors found
     */
    @Override
    public void validate(Prietenie p) {
        String errors = "";
        Long a = p.getId().getLeft();
        Long b = p.getId().getRight();
        if (a == null || b == null) {
            errors += "Id-urile utilizatorilor nu pot fi nule. ";
        } else if (a.equals(b)) {
            errors += "Un utilizator nu poate fi prieten cu el insusi. ";
        }
        if (p.getFriendsFrom() == null) {
            errors += "Data prieteniei lipseste. ";
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.trim());
        }
    }
}