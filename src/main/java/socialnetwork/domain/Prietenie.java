package socialnetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A friendship between two users of the social network.
 * The identifier of a friendship is the pair of the two user ids, always kept
 * in ascending order. This way the friendship between users 1 and 2 has the
 * same id no matter in which order the users are given. A friendship also
 * stores the date from which the two users are friends.
 */
public class Prietenie extends Entity<Tuple<Long, Long>> {
    /** The date format used for display: {@code yyyy-MM-dd HH:mm}. */
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** The date and time from which the two users are friends. */
    private final LocalDateTime friendsFrom;

    /**
     * Creates a friendship between two users.
     * @param id1         the id of the first user
     * @param id2         the id of the second user
     * @param friendsFrom the date and time from which they are friends
     */
    public Prietenie(Long id1, Long id2, LocalDateTime friendsFrom) {
        setId(makeId(id1, id2));
        this.friendsFrom = friendsFrom;
    }

    /**
     * Builds the identifier of a friendship, with the ids in ascending order.
     * @param id1 the id of one user
     * @param id2 the id of the other user
     * @return the pair {@code (min, max)} of the two ids
     */
    public static Tuple<Long, Long> makeId(Long id1, Long id2) {
        if (id1 != null && id2 != null && id1 > id2) {
            return new Tuple<>(id2, id1);
        }
        return new Tuple<>(id1, id2);
    }

    /**
     * Returns the date from which the two users are friends.
     * @return the date and time the friendship started
     */
    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    /**
     * Returns the friendship in the form {@code id1 <-> id2 (din date)}.
     * @return the text representation of the friendship
     */
    @Override
    public String toString() {
        return getId().getLeft() + " <-> " + getId().getRight()
                + " (din " + friendsFrom.format(FORMAT) + ")";
    }
}