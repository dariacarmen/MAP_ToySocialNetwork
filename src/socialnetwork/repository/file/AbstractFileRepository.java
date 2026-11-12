package socialnetwork.repository.file;

import socialnetwork.domain.Entity;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.repository.memory.InMemoryRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Repository that also keeps the entities in a UTF-8 text file, one line for
 * each entity (Template Method pattern).
 * <p>
 * When it is created it loads the data from the file with
 * {@link Files#lines(Path, java.nio.charset.Charset)}. After every successful
 * save, delete or update it rewrites the whole file with
 * {@link Files#write(Path, Iterable, java.nio.charset.Charset, java.nio.file.OpenOption...)}.
 * If the file does not exist, the repository starts empty and the file is
 * created at the first change.
 * <p>
 * The data loaded from the file goes through the validator. If a line is
 * corrupted or invalid, loading stops with a {@link RepositoryException} that
 * says the file, the line number and the reason.
 * <p>
 * Subclasses only decide the format of a line, with
 * {@link #extractEntity(String)} and {@link #createEntityAsString(Entity)}.
 * Because these methods are called from the constructor, they must not use
 * instance fields of the subclass (only constants).
 *
 * @param <ID> the type of the identifier of the entities
 * @param <E>  the type of the entities; it must extend {@link Entity}
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    /** The path of the text file in which the entities are kept. */
    private final Path path;

    /**
     * Creates the repository and loads the entities from the file.
     *
     * @param validator the validation strategy for the entities
     * @param fileName  the path of the text file
     * @throws IllegalArgumentException if {@code fileName} is {@code null} or empty
     * @throws RepositoryException      if the file exists but cannot be read,
     *                                  or contains a corrupted or invalid line
     */
    public AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must be not empty");
        }
        this.path = Path.of(fileName);
        loadData();
    }

    /**
     * Converts a line of the file into an entity.
     *
     * @param line the line read from the file, without spaces at the ends
     * @return the entity built from the line
     * @throws IllegalArgumentException if the line does not have the expected
     *                                  format (for example a wrong number of
     *                                  fields or an id that is not a number)
     * @throws DateTimeParseException   if a date in the line has a wrong format
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into the line that will be written in the file.
     *
     * @param entity the entity to convert
     * @return the line for the entity
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Reads all the lines of the file and adds the entities in memory.
     * Empty lines are skipped. If the file does not exist, nothing is loaded.
     * The lines are first collected in a list, so that every line can be
     * reported with its number.
     *
     * @throws RepositoryException if the file cannot be read or contains a
     *                             corrupted or invalid line
     */
    private void loadData() {
        if (!Files.exists(path)) {
            return;
        }
        List<String> lines;
        try (Stream<String> fileLines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines = fileLines.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi fisierul " + path, e);
        }
        IntStream.range(0, lines.size())
                .filter(i -> !lines.get(i).isBlank())
                .forEach(i -> loadLine(lines.get(i).trim(), i + 1));
    }

    /**
     * Converts one line into an entity, validates it and adds it in memory.
     * It calls {@code super.save}, not {@code save}, so that the file is not
     * rewritten while it is being read.
     *
     * @param line       the line read from the file
     * @param lineNumber the number of the line in the file, starting from 1
     * @throws RepositoryException if the line is corrupted, invalid or has a
     *                             duplicated id; the original exception is kept
     *                             as the cause
     */
    private void loadLine(String line, int lineNumber) {
        try {
            E entity = extractEntity(line);
            if (super.save(entity) != null) {
                throw new RepositoryException(lineError(lineNumber, line, "id duplicat"));
            }
        } catch (IllegalArgumentException | DateTimeParseException | ValidationException e) {
            throw new RepositoryException(lineError(lineNumber, line, e.getMessage()), e);
        }
    }

    /**
     * Builds the error message for a wrong line of the file.
     *
     * @param lineNumber the number of the line
     * @param line       the content of the line
     * @param reason     why the line is wrong
     * @return the error message
     */
    private String lineError(int lineNumber, String line, String reason) {
        return "Fisierul " + path + ", linia " + lineNumber + " (\"" + line + "\") este invalida: " + reason;
    }

    /**
     * Rewrites the file with all the entities from memory. The lines are
     * built with a stream over the entities. The folder of the file is
     * created if it does not exist.
     *
     * @throws RepositoryException if the folder cannot be created or the file
     *                             cannot be written
     */
    private void writeToFile() {
        List<String> lines = stream()
                .map(this::createEntityAsString)
                .collect(Collectors.toList());
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Nu pot scrie fisierul " + path, e);
        }
    }

    /**
     * Validates and saves a new entity, then rewrites the file.
     *
     * @param entity the entity to save; must not be {@code null}
     * @return {@code null} if the entity was saved, otherwise the given entity
     *         (an entity with the same id already exists)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws RepositoryException      if the file cannot be written
     */
    @Override
    public E save(E entity) {
        E result = super.save(entity);
        if (result == null) writeToFile();
        return result;
    }

    /**
     * Removes the entity with the given identifier, then rewrites the file.
     * @param id the identifier of the entity to remove; must not be {@code null}
     * @return the removed entity, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if the file cannot be written
     */
    @Override
    public E delete(ID id) {
        E result = super.delete(id);
        if (result != null) writeToFile();
        return result;
    }

    /**
     * Updates an existing entity, then rewrites the file.
     * @param entity the new entity; must not be {@code null}
     * @return {@code null} if the entity was updated, otherwise the given entity
     *         (there is no entity with this id)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws RepositoryException      if the file cannot be written
     */
    @Override
    public E update(E entity) {
        E result = super.update(entity);
        if (result == null) writeToFile();
        return result;
    }
}
