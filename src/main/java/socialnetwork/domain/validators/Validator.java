package socialnetwork.domain.validators;
/**
 * Validation strategy for an entity (Strategy pattern).
 * It is a functional interface, so a validation rule can be written as a
 * lambda expression. Several rules can be combined with {@link #andThen(Validator)},
 * without writing new classes.
 * @param <T> the type of the validated entity
 * @see UtilizatorValidator
 * @see PrietenieValidator
 */
@FunctionalInterface
public interface Validator<T> {
    /**
     * Checks whether the entity respects the validation rules.
     * @param entity the entity to validate
     * @throws ValidationException if the entity is not valid; the message
     *                             contains the errors found
     */
    void validate(T entity);
    /**
     * Returns a validator that first applies this validator and then the
     * given one. If this validator fails, the other one is not applied.
     * @param other the validator applied after this one; must not be {@code null}
     * @return a validator that applies both rules, in order
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Validator<T> andThen(Validator<T> other) {
        if (other == null) throw new NullPointerException("other must be not null");
        return entity -> {
            validate(entity);
            other.validate(entity);
        };
    }
}