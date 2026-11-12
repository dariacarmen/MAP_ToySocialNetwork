package socialnetwork.domain;

import java.util.function.Predicate;
/**
 * Search criteria for users, written as {@link Predicate} objects.
 * The criteria are combined with {@link Predicate#and(Predicate)},
 * {@link Predicate#or(Predicate)}, {@link Predicate#negate()} and
 * {@link Predicate#not(Predicate)}, and are given to
 * {@code Repository.findAll(Predicate)}. A new criterion is just a new static
 * method or a lambda, not a new repository method.
 */
public final class CriteriiUtilizator {
    /**
     * Private constructor: the class only has static methods.
     */
    private CriteriiUtilizator() {
    }
    /**
     * Checks whether a user has no friends. It is meant to be used as a
     * method reference: {@code CriteriiUtilizator::faraPrieteni}.
     * @param u the user to check
     * @return {@code true} if the user has no friends, {@code false} otherwise
     */
    public static boolean faraPrieteni(Utilizator u) {
        return u.getFriends().isEmpty();
    }
    /**
     * Criterion: the last name starts with the given text (case is ignored).
     * @param prefix the text the last name must start with
     * @return the criterion
     */
    public static Predicate<Utilizator> numeIncepeCu(String prefix) {
        return u -> u.getLastName().toLowerCase().startsWith(prefix.toLowerCase());
    }
    /**
     * Criterion: the first name starts with the given text (case is ignored).
     * @param prefix the text the first name must start with
     * @return the criterion
     */
    public static Predicate<Utilizator> prenumeIncepeCu(String prefix) {
        return u -> u.getFirstName().toLowerCase().startsWith(prefix.toLowerCase());
    }
    /**
     * Criterion: the user has at least the given number of friends.
     * @param n the minimum number of friends
     * @return the criterion
     */
    public static Predicate<Utilizator> celPutinPrieteni(int n) {
        return u -> u.getFriends().size() >= n;
    }
    /**
     * Criterion: the user is friends with the user that has the given id.
     * @param id the id of the other user
     * @return the criterion
     */
    public static Predicate<Utilizator> prietenCu(Long id) {
        return u -> u.getFriends().stream().anyMatch(f -> f.getId().equals(id));
    }
}