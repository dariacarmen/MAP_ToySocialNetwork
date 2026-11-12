package socialnetwork.domain;

import java.util.Objects;

/**
 * Base class for all the entities of the application domain.
 * <p>
 * Every entity has a unique identifier. Two entities are equal if they have
 * the same class and the same identifier, regardless of their other fields.
 * @param <ID> the type of the identifier (for example {@code Long})
 */
public class Entity<ID> {

    /** The unique identifier of the entity. */
    private ID id;

    /**
     * Creates an entity without an identifier; the identifier is set later
     * with {@link #setId(Object)}.
     */
    public Entity() {
    }
    /**
     * Returns the identifier of the entity.
     * @return the identifier, or {@code null} if it was not set
     */
    public ID getId() {
        return id;
    }
    /**
     * Sets the identifier of the entity.
     * @param id the new identifier
     */
    public void setId(ID id) {
        this.id = id;
    }
    /**
     * Checks whether this entity is equal to another object.
     * Two entities are equal if they have the same class and the same identifier.
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }
    /**
     * Computes the hash code from the identifier, consistently with
     * {@link #equals(Object)}.
     * @return the hash code of the entity
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
