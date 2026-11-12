package socialnetwork.repository.file;

import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;

/**
 * Repository of users kept in a text file.
 * <p>
 * Every line of the file describes a user, in the format
 * {@code id;prenume;nume} (for example {@code 1;Ana;Pop}).
 */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {
    /** The number of fields of a line. */
    private static final int FIELDS = 3;

    /**
     * Creates the repository and loads the users from the file.
     *
     * @param validator the validation strategy for users
     * @param fileName  the path of the text file
     * @throws RepositoryException if the file exists but cannot be read, or
     *                             contains a corrupted or invalid line
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * Builds a user from a line of the form {@code id;prenume;nume}.
     *
     * @param line the line read from the file
     * @return the user described by the line
     * @throws IllegalArgumentException if the line does not have 3 fields
     * @throws NumberFormatException    if the id is not a number
     */
    @Override
    protected Utilizator extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != FIELDS) {
            throw new IllegalArgumentException("trebuie sa aiba " + FIELDS
                    + " campuri separate prin ';', are " + parts.length);
        }
        Utilizator u = new Utilizator(parts[1].trim(), parts[2].trim());
        u.setId(Long.parseLong(parts[0].trim()));
        return u;
    }

    /**
     * Converts a user into a line of the form {@code id;prenume;nume}.
     *
     * @param u the user to convert
     * @return the line for the user
     */
    @Override
    protected String createEntityAsString(Utilizator u) {
        return u.getId() + ";" + u.getFirstName() + ";" + u.getLastName();
    }
}