package socialnetwork.repository.memory;

import socialnetwork.domain.Entity;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link Repository} that keeps the entities in memory,
 * in a {@link HashMap}.
 * <p>
 * The entities are validated with the {@link Validator} strategy received in
 * the constructor, before every save and update.
 *
 * @param <ID> the type of the identifier of the entities
 * @param <E>  the type of the entities; it must extend {@link Entity}
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    /** The validation strategy used on save and update. */
    private final Validator<E> validator;
    /** The entities, indexed by their identifier. */
    protected final Map<ID, E> entities = new HashMap<>();

    /**
     * Creates an empty repository that uses the given validator.
     * @param validator the validation strategy for the entities
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
    }

    /**
     * Finds the entity with the given identifier.
     * @param id the identifier to look for; must not be {@code null}
     * @return the entity with that identifier, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    @Override
    public E findOne(ID id) {
        if (id == null) throw new IllegalArgumentException("id must be not null");
        return entities.get(id);
    }

    /**
     * Returns all the entities.
     * @return all the entities kept in memory
     */
    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }
    /**
     * Validates and saves a new entity.
     * @param entity the entity to save; must not be {@code null}
     * @return {@code null} if the entity was saved, otherwise the given entity
     *         (an entity with the same id already exists)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    @Override
    public E save(E entity) {
        if (entity == null) throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) return entity;
        entities.put(entity.getId(), entity);
        return null;
    }
    /**
     * Removes the entity with the given identifier.
     * @param id the identifier of the entity to remove; must not be {@code null}
     * @return the removed entity, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    @Override
    public E delete(ID id) {
        if (id == null) throw new IllegalArgumentException("id must be not null");
        return entities.remove(id);
    }
    /**
     * Validates the entity and replaces the existing one with the same id.
     * @param entity the new entity; must not be {@code null}
     * @return {@code null} if the entity was updated, otherwise the given entity
     *         (there is no entity with this id)
     * @throws ValidationException      if the entity is not valid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    @Override
    public E update(E entity) {
        if (entity == null) throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);
        if (!entities.containsKey(entity.getId())) return entity;
        entities.put(entity.getId(), entity);
        return null;
    }
}