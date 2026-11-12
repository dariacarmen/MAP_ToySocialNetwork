package socialnetwork.repository;
/**
 * The kinds of persistence the application can use.
 * The kind is chosen with the {@code repository.type} property of the
 * configuration file and is used by {@link RepositoryFactory}.
 */
public enum RepositoryType {
    /** The data is kept only in memory and is lost when the application stops. */
    MEMORY,
    /** The data is kept in text files. */
    FILE,
    /** The data is kept in a database (implemented in laboratory 5). */
    DATABASE
}