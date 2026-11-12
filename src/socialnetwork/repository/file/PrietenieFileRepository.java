package socialnetwork.repository.file;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Repository of friendships kept in a text file.
 * <p>
 * Every line of the file describes a friendship, in the format
 * {@code idUtilizator1;idUtilizator2;dataPrieteniei}, with the date in ISO
 * format (for example {@code 1;2;2026-10-19T10:30}).
 */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {
    /** The number of fields of a line. */
    private static final int FIELDS = 3;
    /** The ISO date format used in the file: {@code yyyy-MM-ddTHH:mm}. */
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Creates the repository and loads the friendships from the file.
     *
     * @param validator the validation strategy for friendships
     * @param fileName  the path of the text file
     * @throws RepositoryException if the file exists but cannot be read, or
     *                             contains a corrupted or invalid line
     */
    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * Builds a friendship from a line of the form {@code id1;id2;data}.
     *
     * @param line the line read from the file
     * @return the friendship described by the line
     * @throws IllegalArgumentException if the line does not have 3 fields
     * @throws NumberFormatException    if an id is not a number
     * @throws java.time.format.DateTimeParseException if the date is not in
     *         the format {@code yyyy-MM-ddTHH:mm}
     */
    @Override
    protected Prietenie extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != FIELDS) {
            throw new IllegalArgumentException("trebuie sa aiba " + FIELDS
                    + " campuri separate prin ';', are " + parts.length);
        }
        Long id1 = Long.parseLong(parts[0].trim());
        Long id2 = Long.parseLong(parts[1].trim());
        LocalDateTime date = LocalDateTime.parse(parts[2].trim(), FILE_DATE_FORMAT);
        return new Prietenie(id1, id2, date);
    }

    /**
     * Converts a friendship into a line of the form {@code id1;id2;data}.
     *
     * @param p the friendship to convert
     * @return the line for the friendship
     */
    @Override
    protected String createEntityAsString(Prietenie p) {
        return p.getId().getLeft() + ";" + p.getId().getRight() + ";"
                + p.getFriendsFrom().format(FILE_DATE_FORMAT);
    }
}