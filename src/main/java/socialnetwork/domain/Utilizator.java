package socialnetwork.domain;

import java.util.ArrayList;
import java.util.List;
/**
 * A user of the social network.
 * A user has a {@code Long} identifier, a first name, a last name and a list
 * of friends. Users are compared by id ({@code equals} and {@code hashCode}
 * are inherited from {@link Entity}).
 */
public class Utilizator extends Entity<Long> {
    /** The first name of the user. */
    private String firstName;
    /** The last name of the user. */
    private String lastName;
    /** The users this user is friends with. */
    private final List<Utilizator> friends = new ArrayList<>();
    /**
     * Creates a new user without friends. The id is set later with
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     */
    public Utilizator(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    /**
     * Returns the first name of the user.
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * Sets the first name of the user.
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /**
     * Returns the last name of the user.
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * Sets the last name of the user.
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * Returns the list of friends of the user.
     * The list is changed only by the service layer, when a friendship is
     * added or removed.
     * @return the list of friends
     */
    public List<Utilizator> getFriends() {
        return friends;
    }
    /**
     * Returns the user in the form {@code id | firstName lastName | prieteni: n}.
     * @return the text representation of the user
     */
    @Override
    public String toString() {
        return getId() + " | " + firstName + " " + lastName + " | prieteni: " + friends.size();
    }
}
