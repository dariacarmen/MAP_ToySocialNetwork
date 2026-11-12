package socialnetwork.repository;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.repository.database.PrietenieDbRepository;
import socialnetwork.repository.database.UtilizatorDbRepository;
import socialnetwork.repository.file.PrietenieFileRepository;
import socialnetwork.repository.file.UtilizatorFileRepository;
import socialnetwork.repository.memory.InMemoryRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Creates the repositories of the application (Simple Factory pattern).
 * There is no {@code switch}: every {@link RepositoryType} is mapped, in a
 * {@link Map}, to a {@link Function} that receives the validator and creates
 * the repository. Only the function of the chosen type is called.
 * <p>
 * Properties used:
 * <ul>
 *     <li>{@code repository.type} - {@code MEMORY}, {@code FILE} or {@code DATABASE};</li>
 *     <li>{@code file.utilizatori}, {@code file.prietenii} - the data files (for {@code FILE});</li>
 *     <li>{@code db.url}, {@code db.user}, {@code db.password} - the connection (for {@code DATABASE}).</li>
 * </ul>
 */
public class RepositoryFactory {
    /** The default path of the configuration file, relative to the project folder. */
    public static final String DEFAULT_CONFIG_FILE = "src/main/resources/config.properties";

    /** The path of the configuration file, used in error messages. */
    private final String configFile;
    /** The properties read from the configuration file. */
    private final Properties properties;
    /** The kind of persistence used for all the repositories. */
    private final RepositoryType type;

    /** For every kind of persistence, the function that creates the repository of users. */
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Long, Utilizator>>> userCreators;
    /** For every kind of persistence, the function that creates the repository of friendships. */
    private final Map<RepositoryType, Function<Validator<Prietenie>, Repository<Tuple<Long, Long>, Prietenie>>> friendshipCreators;

    /**
     * Creates a factory that reads its settings from the given configuration file.
     * @param configFile the path of the configuration file
     * @throws RepositoryException if the file cannot be read, or if
     *                             {@code repository.type} is missing or unknown
     */
    public RepositoryFactory(String configFile) {
        this.configFile = configFile;
        this.properties = loadProperties(configFile);
        this.type = readType();
        this.userCreators = Map.of(
                RepositoryType.MEMORY, InMemoryRepository::new,
                RepositoryType.FILE, validator -> new UtilizatorFileRepository(validator,
                        getRequired("file.utilizatori")),
                RepositoryType.DATABASE, validator -> new UtilizatorDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getPresent("db.password")));
        this.friendshipCreators = Map.of(
                RepositoryType.MEMORY, InMemoryRepository::new,
                RepositoryType.FILE, validator -> new PrietenieFileRepository(validator,
                        getRequired("file.prietenii")),
                RepositoryType.DATABASE, validator -> new PrietenieDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getPresent("db.password")));
    }
    /**
     * Reads the configuration file into a {@link Properties} object.
     * @param configFile the path of the configuration file
     * @return the properties read from the file
     * @throws RepositoryException if the file cannot be read
     */
    private static Properties loadProperties(String configFile) {
        Properties result = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile, StandardCharsets.UTF_8))) {
            result.load(reader);
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi fisierul de configurare " + configFile, e);
        }
        return result;
    }
    /**
     * Returns the value of a property that must exist and must not be empty.
     * @param key the name of the property
     * @return the value of the property, without spaces at the ends
     * @throws RepositoryException if the property is missing or empty
     */
    private String getRequired(String key) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> missing(key));
    }
    /**
     * Returns the value of a property that must exist, but may be empty
     * (for example a database password).
     * @param key the name of the property
     * @return the value of the property, without spaces at the ends
     * @throws RepositoryException if the property is missing
     */
    private String getPresent(String key) {
        return Optional.ofNullable(properties.getProperty(key))
                .map(String::trim)
                .orElseThrow(() -> missing(key));
    }
    /**
     * Builds the exception for a missing property.
     * @param key the name of the missing property
     * @return the exception to throw
     */
    private RepositoryException missing(String key) {
        return new RepositoryException("Lipseste proprietatea '" + key
                + "' din fisierul de configurare " + configFile);
    }
    /**
     * Reads and checks the {@code repository.type} property.
     * @return the kind of persistence
     * @throws RepositoryException if the property is missing or has an unknown value
     */
    private RepositoryType readType() {
        String value = getRequired("repository.type");
        try {
            return RepositoryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Tip de persistenta necunoscut: '" + value
                    + "'. Valori permise: MEMORY, FILE, DATABASE", e);
        }
    }
    /**
     * Returns the kind of persistence read from the configuration file.
     * @return the kind of persistence used by this factory
     */
    public RepositoryType getType() {
        return type;
    }
    /**
     * Creates the repository of users for the configured kind of persistence.
     * @param validator the validation strategy for users
     * @return the repository of users, seen through the {@link Repository} interface
     * @throws RepositoryException if a needed property is missing, or if the
     *                             data file exists but cannot be loaded
     */
    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        return Optional.ofNullable(userCreators.get(type))
                .orElseThrow(() -> new RepositoryException("Tip de persistenta nesuportat: " + type))
                .apply(validator);
    }
    /**
     * Creates the repository of friendships for the configured kind of persistence.
     * @param validator the validation strategy for friendships
     * @return the repository of friendships, seen through the {@link Repository} interface
     * @throws RepositoryException if a needed property is missing, or if the
     *                             data file exists but cannot be loaded
     */
    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        return Optional.ofNullable(friendshipCreators.get(type))
                .orElseThrow(() -> new RepositoryException("Tip de persistenta nesuportat: " + type))
                .apply(validator);
    }
}