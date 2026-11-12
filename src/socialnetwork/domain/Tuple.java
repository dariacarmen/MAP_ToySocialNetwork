package socialnetwork.domain;

import java.util.Objects;
/**
 * Immutable ordered pair of two values.
 * It is used as the identifier of a {@link Prietenie}: the two values are the
 * ids of the two users. Two pairs are equal if they have equal values on the
 * same positions.
 * @param <E1> the type of the first element
 * @param <E2> the type of the second element
 */
public class Tuple<E1, E2> {
    /** The first element of the pair. */
    private final E1 left;
    /** The second element of the pair. */
    private final E2 right;
    /**
     * Creates a pair from two values.
     * @param left  the first element
     * @param right the second element
     */
    public Tuple(E1 left, E2 right) {
        this.left = left;
        this.right = right;
    }
    /**
     * Returns the first element of the pair.
     * @return the first element
     */
    public E1 getLeft() {
        return left;
    }
    /**
     * Returns the second element of the pair.
     * @return the second element
     */
    public E2 getRight() {
        return right;
    }
    /**
     * Checks whether this pair is equal to another object.
     * Returns {@code false} for {@code null} and for objects of another type.
     * @param o the object to compare with
     * @return {@code true} if {@code o} is a pair with equal elements on the
     *         same positions, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple<?, ?> t)) return false;
        return Objects.equals(left, t.left) && Objects.equals(right, t.right);
    }
    /**
     * Computes the hash code from both elements, consistently with
     * {@link #equals(Object)}.
     * @return the hash code of the pair
     */
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
    /**
     * Returns the pair in the form {@code (left, right)}.
     * @return the text representation of the pair
     */
    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}