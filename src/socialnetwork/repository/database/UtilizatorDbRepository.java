package socialnetwork.repository.database;

import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

/**
 * Repository of users kept in a database.
 * This is only a skeleton: the methods are implemented with JDBC in
 * laboratory 5. For now every method throws
 * {@link UnsupportedOperationException}.
 */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    /** The validation strategy for users. */
    private final Validator<Utilizator> validator;
    /** The JDBC url of the database. */
    private final String url;
    /** The user name for the database connection. */
    private final String user;
    /** The password for the database connection. */
    private final String password;

    /**
     * Creates the repository with the validator and the connection parameters.
     * @param validator the validation strategy for users
     * @param url       the JDBC url of the database
     * @param user      the user name for the connection
     * @param password  the password for the connection
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    /**
     * Not implemented yet (laboratory 5).
     * @param id the identifier to look for
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Utilizator findOne(Long id) {
        throw notImplemented("findOne");
    }
    /**
     * Not implemented yet (laboratory 5).
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Iterable<Utilizator> findAll() {
        throw notImplemented("findAll");
    }
    /**
     * Not implemented yet (laboratory 5).
     * @param entity the user to save
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Utilizator save(Utilizator entity) {
        throw notImplemented("save");
    }
    /**
     * Not implemented yet (laboratory 5).
     * @param id the identifier of the user to remove
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Utilizator delete(Long id) {
        throw notImplemented("delete");
    }
    /**
     * Not implemented yet (laboratory 5).
     * @param entity the new user
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Utilizator update(Utilizator entity) {
        throw notImplemented("update");
    }
    /**
     * Builds the exception thrown by the methods that are not implemented yet.
     * @param method the name of the method
     * @return the exception to throw
     */
    private UnsupportedOperationException notImplemented(String method) {
        return new UnsupportedOperationException("UtilizatorDbRepository." + method
                + " nu este implementata inca (laboratorul 5)");
    }
}