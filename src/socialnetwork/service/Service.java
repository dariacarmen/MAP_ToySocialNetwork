package socialnetwork.service;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.exceptions.ServiceException;
import socialnetwork.repository.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The application logic: users, friendships and communities.
 * <p>
 * The network is seen as an undirected graph: the nodes are the users and the
 * edges are the friendships. A community is a connected component of this graph.
 * The service depends only on the {@link Repository} interface, so it does not
 * know where the data is kept. All the operations on collections are written
 * with streams.
 */
public class Service {
    /** Users ordered by id: the order used when users are shown. */
    private static final Comparator<Utilizator> BY_ID = Comparator.comparing(Utilizator::getId);

    /** The repository of users. */
    private final Repository<Long, Utilizator> userRepo;
    /** The repository of friendships. */
    private final Repository<Tuple<Long, Long>, Prietenie> friendshipRepo;

    /**
     * Creates the service and rebuilds the friend lists of the users from the
     * saved friendships.
     *
     * @param userRepo       the repository of users
     * @param friendshipRepo the repository of friendships
     * @throws RepositoryException if a friendship to a missing user cannot be
     *                             removed from the storage
     */
    public Service(Repository<Long, Utilizator> userRepo,
                   Repository<Tuple<Long, Long>, Prietenie> friendshipRepo) {
        this.userRepo = userRepo;
        this.friendshipRepo = friendshipRepo;
        rebuildFriendLists();
    }

    /**
     * Rebuilds the friend lists of all the users from the friendships in the
     * repository. Friendships to users that do not exist are removed, so that
     * users and friendships stay consistent.
     */
    private void rebuildFriendLists() {
        userRepo.stream().forEach(u -> u.getFriends().clear());
        Map<Boolean, List<Prietenie>> byValidity = friendshipRepo.stream()
                .collect(Collectors.partitioningBy(this::bothUsersExist));
        byValidity.get(true).forEach(this::link);
        byValidity.get(false).stream()
                .map(Prietenie::getId)
                .forEach(friendshipRepo::delete);
    }

    /**
     * Checks whether both users of a friendship exist.
     *
     * @param p the friendship to check
     * @return {@code true} if both users exist, {@code false} otherwise
     */
    private boolean bothUsersExist(Prietenie p) {
        return findUser(p.getId().getLeft()).isPresent()
                && findUser(p.getId().getRight()).isPresent();
    }

    /**
     * Adds the two users of a friendship to each other's friend list.
     *
     * @param p the friendship whose users are linked
     */
    private void link(Prietenie p) {
        Utilizator a = userRepo.findOne(p.getId().getLeft());
        Utilizator b = userRepo.findOne(p.getId().getRight());
        a.getFriends().add(b);
        b.getFriends().add(a);
    }

    /**
     * Adds a new user to the network. The id is generated automatically, as
     * the largest existing id plus 1.
     *
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @return the added user, with its id set
     * @throws ValidationException if the first name or the last name is not valid
     * @throws RepositoryException if the user cannot be persisted
     */
    public Utilizator addUser(String firstName, String lastName) {
        Utilizator user = new Utilizator(firstName, lastName);
        user.setId(nextUserId());
        userRepo.save(user);
        return user;
    }

    /**
     * Computes the next free id for a user.
     *
     * @return the largest existing id plus 1, or 1 if there are no users
     */
    private Long nextUserId() {
        return userRepo.stream()
                .map(Utilizator::getId)
                .max(Comparator.naturalOrder())
                .map(max -> max + 1)
                .orElse(1L);
    }

    /**
     * Removes a user from the network, together with all his friendships.
     *
     * @param id the id of the user to remove
     * @return the removed user
     * @throws ServiceException    if there is no user with this id
     * @throws RepositoryException if the change cannot be persisted
     */
    public Utilizator removeUser(Long id) {
        Utilizator user = getUser(id);
        List<Tuple<Long, Long>> toDelete = friendshipRepo.stream()
                .map(Prietenie::getId)
                .filter(fid -> fid.getLeft().equals(id) || fid.getRight().equals(id))
                .collect(Collectors.toList());
        toDelete.forEach(friendshipRepo::delete);
        user.getFriends().forEach(friend -> friend.getFriends().remove(user));
        user.getFriends().clear();
        userRepo.delete(id);
        return user;
    }

    /**
     * Looks for the user with the given id.
     *
     * @param id the id to look for
     * @return the user with this id, or an empty {@link Optional} if there is none
     */
    public Optional<Utilizator> findUser(Long id) {
        return Optional.ofNullable(userRepo.findOne(id));
    }

    /**
     * Returns the user with the given id.
     *
     * @param id the id to look for
     * @return the user with this id
     * @throws ServiceException if there is no user with this id
     */
    public Utilizator getUser(Long id) {
        return findUser(id)
                .orElseThrow(() -> new ServiceException("Nu exista utilizatorul cu id " + id));
    }

    /**
     * Returns all the users of the network, ordered by id.
     *
     * @return all the users
     */
    public List<Utilizator> getAllUsers() {
        return userRepo.stream()
                .sorted(BY_ID)
                .collect(Collectors.toList());
    }

    /**
     * Returns the users that satisfy the given criterion, ordered by id.
     *
     * @param criterion the condition the users must satisfy
     * @return the users that satisfy the criterion
     */
    public List<Utilizator> searchUsers(Predicate<Utilizator> criterion) {
        return searchUsers(criterion, BY_ID);
    }

    /**
     * Returns the users that satisfy the given criterion, in the given order.
     *
     * @param criterion the condition the users must satisfy
     * @param order     the order of the result
     * @return the users that satisfy the criterion, sorted with {@code order}
     */
    public List<Utilizator> searchUsers(Predicate<Utilizator> criterion, Comparator<Utilizator> order) {
        return userRepo.findAll(criterion).stream()
                .sorted(order)
                .collect(Collectors.toList());
    }

    /**
     * Creates a friendship between two existing users, with the current date.
     *
     * @param id1 the id of the first user
     * @param id2 the id of the second user
     * @throws ServiceException    if a user does not exist or the two users
     *                             are already friends
     * @throws ValidationException if {@code id1} is equal to {@code id2}
     * @throws RepositoryException if the friendship cannot be persisted
     */
    public void addFriend(Long id1, Long id2) {
        Utilizator a = getUser(id1);
        Utilizator b = getUser(id2);
        Prietenie p = new Prietenie(id1, id2, LocalDateTime.now());
        if (friendshipRepo.save(p) != null) {
            throw new ServiceException("Utilizatorii " + id1 + " si " + id2 + " sunt deja prieteni");
        }
        a.getFriends().add(b);
        b.getFriends().add(a);
    }

    /**
     * Removes the friendship between two users.
     *
     * @param id1 the id of the first user
     * @param id2 the id of the second user
     * @throws ServiceException    if a user does not exist or the two users
     *                             are not friends
     * @throws RepositoryException if the change cannot be persisted
     */
    public void removeFriend(Long id1, Long id2) {
        Utilizator a = getUser(id1);
        Utilizator b = getUser(id2);
        if (friendshipRepo.delete(Prietenie.makeId(id1, id2)) == null) {
            throw new ServiceException("Utilizatorii " + id1 + " si " + id2 + " nu sunt prieteni");
        }
        a.getFriends().remove(b);
        b.getFriends().remove(a);
    }

    /**
     * Returns all the friendships of the network, ordered by the ids of the users.
     *
     * @return all the friendships
     */
    public List<Prietenie> getAllFriendships() {
        return friendshipRepo.stream()
                .sorted(Comparator.comparing((Prietenie p) -> p.getId().getLeft())
                        .thenComparing(p -> p.getId().getRight()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the friends of a user, found from the saved friendships and
     * ordered by last name and first name.
     *
     * @param id the id of the user
     * @return the friends of the user
     * @throws ServiceException if there is no user with this id
     */
    public List<Utilizator> getFriendsOf(Long id) {
        getUser(id);
        return friendshipRepo.stream()
                .map(Prietenie::getId)
                .filter(fid -> fid.getLeft().equals(id) || fid.getRight().equals(id))
                .flatMap(fid -> Stream.of(fid.getLeft(), fid.getRight()))
                .filter(friendId -> !friendId.equals(id))
                .distinct()
                .map(userRepo::findOne)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Utilizator::getLastName)
                        .thenComparing(Utilizator::getFirstName))
                .collect(Collectors.toList());
    }

    /**
     * Splits the network into communities, that is the connected components
     * of the graph. For every user the component is computed, and equal
     * components are kept only once ({@code distinct}).
     *
     * @return the list of communities; every community is the set of its users
     */
    public List<Set<Utilizator>> getCommunities() {
        return userRepo.stream()
                .sorted(BY_ID)
                .map(this::componentOf)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns the connected component of a user: all the users that can be
     * reached from him through friendships.
     *
     * @param start the user the search starts from
     * @return the set of users of the component, {@code start} included
     */
    private Set<Utilizator> componentOf(Utilizator start) {
        Set<Utilizator> visited = new HashSet<>();
        dfs(start, visited);
        return visited;
    }

    /**
     * Depth-first search from a user: adds to {@code visited} all the users
     * that can be reached. The neighbours are processed with a stream.
     *
     * @param u       the current user
     * @param visited the users already reached (filled by this method)
     */
    private void dfs(Utilizator u, Set<Utilizator> visited) {
        visited.add(u);
        u.getFriends().stream()
                .filter(friend -> !visited.contains(friend))
                .forEach(friend -> dfs(friend, visited));
    }

    /**
     * Returns the number of communities (connected components) of the network.
     *
     * @return the number of communities
     */
    public long getNumberOfCommunities() {
        return userRepo.stream()
                .map(this::componentOf)
                .distinct()
                .count();
    }

    /**
     * Finds the most sociable community: the connected component that contains
     * the longest simple path (a path that does not visit a user twice).
     *
     * @return the pair made of the users of the community (ordered by id) and
     *         the length of its longest path (the number of friendships on the path)
     * @throws ServiceException if the network has no users
     */
    public Tuple<List<Utilizator>, Integer> getMostSociableCommunity() {
        Set<Utilizator> best = getCommunities().stream()
                .max(Comparator.comparingInt(this::longestPath))
                .orElseThrow(() -> new ServiceException("Reteaua nu are utilizatori"));
        List<Utilizator> members = best.stream()
                .sorted(BY_ID)
                .collect(Collectors.toList());
        return new Tuple<>(members, longestPath(best));
    }

    /**
     * Computes the length of the longest simple path of a community, trying
     * every user of the community as a starting point.
     *
     * @param community the users of the community
     * @return the length of the longest path, in number of friendships
     */
    private int longestPath(Set<Utilizator> community) {
        return community.stream()
                .map(u -> longestFrom(u, Set.of()))
                .reduce(0, Integer::max);
    }

    /**
     * Computes with backtracking the longest simple path that starts from the
     * given user. Every call works on its own copy of the path, so the
     * functions given to the stream have no side effects.
     *
     * @param u    the current user
     * @param path the users already on the path before {@code u}
     * @return the length of the longest path that starts from {@code u}
     */
    private int longestFrom(Utilizator u, Set<Utilizator> path) {
        Set<Utilizator> newPath = new HashSet<>(path);
        newPath.add(u);
        return u.getFriends().stream()
                .filter(friend -> !newPath.contains(friend))
                .mapToInt(friend -> 1 + longestFrom(friend, newPath))
                .max()
                .orElse(0);
    }
}