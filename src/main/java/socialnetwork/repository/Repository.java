package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.exceptions.RepositoryException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Generic interface for the CRUD operations on entities.
 * <p>
 * It defines how entities are found, added, removed and updated, without
 * saying where they are kept (in memory, in a file, in a database). The
 * service and the user interface depend only on this interface.
 * <p>
 * {@link #findOne(Object)}, {@link #save(Entity)}, {@link #delete(Object)} and
 * {@link #update(Entity)} keep returning {@code null} (not {@code Optional});
 * the service wraps the result of {@code findOne} in an {@code Optional}.
 *
 * @param <ID> the type of the identifier of the entities
 * @param <E>  the type of the entities; it must extend {@link Entity}
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds the entity with the given identifier.
     * @param id the identifier to look for; must not be {@code null}
     * @return the entity with that identifier, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    E findOne(ID id);

    /**
     * Returns all the entities.
     * @return all the entities kept in the repository
     */
    Iterable<E> findAll();

    /**
     * Returns a sequential stream over all the entities. It is the only place
     * where the {@link Iterable} returned by {@link #findAll()} is turned into
     * a {@link Stream}.
     * @return a new stream over all the entities; it can be used only once
     */
    default Stream<E> stream() {
        return StreamSupport.stream(findAll().spliterator(), false);
    }

    /**
     * Returns the entities that satisfy the given criterion.
     * @param filter the criterion the entities must satisfy; must not be {@code null}
     * @return the list of entities for which {@code filter} returns {@code true}
     * @throws NullPointerException if {@code filter} is {@code null}
     */
    default List<E> findAll(Predicate<E> filter) {
        if (filter == null) throw new NullPointerException("filter must be not null");
        return stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Saves a new entity.
     * @param entity the entity to save; must not be {@code null}
     * @return {@code null} if the entity was saved, otherwise the given entity
     *         (an entity with the same id already exists)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws RepositoryException      if the entity cannot be persisted
     */
    E save(E entity);

    /**
     * Removes the entity with the given identifier.
     * @param id the identifier of the entity to remove; must not be {@code null}
     * @return the removed entity, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if the change cannot be persisted
     */
    E delete(ID id);

    /**
     * Replaces an existing entity with a new one that has the same id.
     * @param entity the new entity; must not be {@code null}
     * @return {@code null} if the entity was updated, otherwise the given entity
     *         (there is no entity with this id)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws RepositoryException      if the change cannot be persisted
     */
    E update(E entity);
}