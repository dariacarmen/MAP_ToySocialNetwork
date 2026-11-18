package socialnetwork.repository.database;

import socialnetwork.exceptions.RepositoryException;

import java.sql.SQLException;

/**
 * Converts a {@link SQLException} into a {@link RepositoryException} with a
 * clear message for the user.
 * <p>
 * The message is chosen from the SQL state of the exception (a five character
 * code defined by the SQL standard and by PostgreSQL): the server is stopped,
 * the password is wrong, the database or a table is missing, or a constraint
 * is violated. The original exception is kept as the cause (exception
 * chaining), so the details are not lost.
 */
final class DbErrors {

    /** SQL state of a duplicate primary key or unique value. */
    static final String UNIQUE_VIOLATION = "23505";
    /** SQL state of a foreign key violation. */
    static final String FOREIGN_KEY_VIOLATION = "23503";

    /**
     * Private constructor: the class has only static methods.
     */
    private DbErrors() {
    }

    /**
     * Builds the repository exception for a database error.
     *
     * @param action what the repository was doing (for example
     *               {@code "salvarea utilizatorului"})
     * @param e      the exception thrown by JDBC
     * @return the repository exception, with {@code e} as the cause
     */
    static RepositoryException wrap(String action, SQLException e) {
        return new RepositoryException("Eroare la " + action + ": " + describe(e), e);
    }

    /**
     * Describes a database error in a few words, using its SQL state.
     *
     * @param e the exception thrown by JDBC
     * @return the description of the error
     */
    private static String describe(SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.startsWith("08")) {
            return "baza de date nu este disponibila (serverul PostgreSQL este oprit "
                    + "sau db.url din config.properties este gresit)";
        }
        switch (state) {
            case "28P01":
                return "autentificare esuata (verificati db.user si db.password din config.properties)";
            case "3D000":
                return "baza de date nu exista (creati baza de date socialnetwork in pgAdmin)";
            case "42P01":
                return "tabela nu exista (rulati scriptul sql/socialnetwork.sql)";
            case UNIQUE_VIOLATION:
                return "exista deja o inregistrare cu aceeasi cheie";
            case FOREIGN_KEY_VIOLATION:
                return "operatia incalca o cheie straina (utilizatorul nu exista "
                        + "sau mai are prietenii)";
            case "23514":
                return "operatia incalca o constrangere CHECK";
            case "23502":
                return "o valoare obligatorie lipseste";
            default:
                return e.getMessage();
        }
    }
}
