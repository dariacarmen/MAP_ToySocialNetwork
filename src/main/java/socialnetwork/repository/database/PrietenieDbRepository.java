package socialnetwork.repository.database;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.repository.FriendshipPagingRepository;
import socialnetwork.util.paging.Page;
import socialnetwork.util.paging.Pageable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository of friendships kept in the PostgreSQL table {@code friendships},
 * with JDBC.
 * <p>
 * The id of a friendship is the pair {@code (id1, id2)}, with {@code id1 < id2},
 * which is also the primary key of the table. Every method opens its own
 * {@link Connection}, {@link PreparedStatement} and {@link ResultSet} in
 * try-with-resources, and the values are always sent as parameters ({@code ?}).
 * A {@link SQLException} is converted into a {@link RepositoryException}.
 * <p>
 * {@link #findFriendsOnPage(Long, Pageable)} returns one page of the friends of
 * a user with a single {@code SELECT} that joins {@code friendships} with
 * {@code users}, paginated in SQL.
 */
public class PrietenieDbRepository implements FriendshipPagingRepository {
    /** Selects all the friendships, ordered by id. */
    private static final String SELECT_ALL = "SELECT id1, id2, friends_from FROM friendships ORDER BY id1, id2";
    /** Selects one friendship by id. */
    private static final String SELECT_ONE = "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?";
    /** Inserts a friendship. */
    private static final String INSERT = "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?)";
    /** Updates the date of a friendship. */
    private static final String UPDATE = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";
    /** Deletes a friendship and returns the deleted row. */
    private static final String DELETE = "DELETE FROM friendships WHERE id1 = ? AND id2 = ? "
            + "RETURNING id1, id2, friends_from";

    /**
     * Selects one page of the friends of a user: the friend is the other user
     * of every friendship of the user ({@code id1 = ? OR id2 = ?}).
     */
    private static final String SELECT_FRIENDS_PAGE = "SELECT u.id, u.first_name, u.last_name "
            + "FROM friendships f JOIN users u ON u.id = CASE WHEN f.id1 = ? THEN f.id2 ELSE f.id1 END "
            + "WHERE f.id1 = ? OR f.id2 = ? ORDER BY u.id LIMIT ? OFFSET ?";
    /** Counts the friends of a user. */
    private static final String COUNT_FRIENDS = "SELECT count(*) FROM friendships WHERE id1 = ? OR id2 = ?";

    /** The validation strategy for friendships. */
    private final Validator<Prietenie> validator;
    /** The JDBC url of the database. */
    private final String url;
    /** The user name for the database connection. */
    private final String user;
    /** The password for the database connection. */
    private final String password;

    /**
     * Creates the repository with the validator and the connection parameters
     * received from the factory.
     *
     * @param validator the validation strategy for friendships
     * @param url       the JDBC url of the database
     * @param user      the user name for the connection
     * @param password  the password for the connection
     */
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Opens a new connection to the database.
     *
     * @return the connection; the caller must close it
     * @throws SQLException if the connection cannot be opened
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Builds a friendship from the current row of a result set.
     *
     * @param rs the result set, positioned on a row with the columns
     *           {@code id1, id2, friends_from}
     * @return the friendship from the row
     * @throws SQLException if a column cannot be read
     */
    private static Prietenie extractFriendship(ResultSet rs) throws SQLException {
        return new Prietenie(rs.getLong("id1"), rs.getLong("id2"),
                rs.getObject("friends_from", LocalDateTime.class));
    }

    /**
     * Checks that a friendship id and both of its user ids are not null.
     *
     * @param id the id to check
     * @throws IllegalArgumentException if the id or one of the user ids is {@code null}
     */
    private static void checkId(Tuple<Long, Long> id) {
        if (id == null || id.getLeft() == null || id.getRight() == null) {
            throw new IllegalArgumentException("id must be not null");
        }
    }

    /**
     * Finds the friendship with the given id.
     *
     * @param id the pair of user ids, with the smaller id first; must not be {@code null}
     * @return the friendship, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if the database cannot be read
     */
    @Override
    public Prietenie findOne(Tuple<Long, Long> id) {
        checkId(id);
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_ONE)) {
            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? extractFriendship(rs) : null;
            }
        } catch (SQLException e) {
            throw DbErrors.wrap("cautarea prieteniei " + id, e);
        }
    }

    /**
     * Returns all the friendships, ordered by the user ids.
     *
     * @return the list of all the friendships
     * @throws RepositoryException if the database cannot be read
     */
    @Override
    public Iterable<Prietenie> findAll() {
        List<Prietenie> result = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(extractFriendship(rs));
            }
        } catch (SQLException e) {
            throw DbErrors.wrap("citirea prieteniilor", e);
        }
        return result;
    }

    /**
     * Returns one page of the friends of a user, ordered by id. The friends are
     * read with one {@code SELECT} with {@code JOIN users}, paginated in SQL
     * ({@code LIMIT ? OFFSET ?}); the number of all the friends is read with
     * {@code count(*)} on the same connection.
     *
     * @param userId   the id of the user; must not be {@code null}
     * @param pageable which page and the size of a page; must not be {@code null}
     * @return the friends on the page (without their own friend lists) and the
     *         number of all the friends; a page after the last one has no elements
     * @throws IllegalArgumentException if {@code userId} or {@code pageable} is {@code null}
     * @throws RepositoryException      if the database cannot be read
     */
    @Override
    public Page<Utilizator> findFriendsOnPage(Long userId, Pageable pageable) {
        if (userId == null) throw new IllegalArgumentException("userId must be not null");
        if (pageable == null) throw new IllegalArgumentException("pageable must be not null");
        List<Utilizator> friends = new ArrayList<>();
        try (Connection connection = connect()) {
            try (PreparedStatement statement = connection.prepareStatement(SELECT_FRIENDS_PAGE)) {
                statement.setLong(1, userId);
                statement.setLong(2, userId);
                statement.setLong(3, userId);
                statement.setInt(4, pageable.getPageSize());
                statement.setLong(5, pageable.getOffset());
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Utilizator friend = new Utilizator(rs.getString("first_name"), rs.getString("last_name"));
                        friend.setId(rs.getLong("id"));
                        friends.add(friend);
                    }
                }
            }
            long total;
            try (PreparedStatement statement = connection.prepareStatement(COUNT_FRIENDS)) {
                statement.setLong(1, userId);
                statement.setLong(2, userId);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    total = rs.getLong(1);
                }
            }
            return new Page<>(friends, total);
        } catch (SQLException e) {
            throw DbErrors.wrap("citirea prietenilor utilizatorului " + userId, e);
        }
    }

    /**
     * Validates the friendship and inserts it in the database.
     *
     * @param entity the friendship to save; must not be {@code null}
     * @return {@code null} if the friendship was saved, otherwise the given
     *         friendship (it already exists: the primary key is duplicated)
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException      if the friendship is not valid
     * @throws RepositoryException      if the friendship cannot be saved (for
     *                                  example a user does not exist)
     */
    @Override
    public Prietenie save(Prietenie entity) {
        if (entity == null) throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            statement.setObject(3, entity.getFriendsFrom());
            statement.executeUpdate();
            return null;
        } catch (SQLException e) {
            if (DbErrors.UNIQUE_VIOLATION.equals(e.getSQLState())) {
                return entity;
            }
            throw DbErrors.wrap("salvarea prieteniei " + entity.getId(), e);
        }
    }

    /**
     * Deletes the friendship with the given id.
     *
     * @param id the pair of user ids, with the smaller id first; must not be {@code null}
     * @return the deleted friendship, or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException      if the friendship cannot be deleted
     */
    @Override
    public Prietenie delete(Tuple<Long, Long> id) {
        checkId(id);
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? extractFriendship(rs) : null;
            }
        } catch (SQLException e) {
            throw DbErrors.wrap("stergerea prieteniei " + id, e);
        }
    }

    /**
     * Validates the friendship and updates its date in the database.
     *
     * @param entity the friendship with the new date; must not be {@code null}
     * @return {@code null} if the friendship was updated, otherwise the given
     *         friendship (it does not exist; {@code executeUpdate()} returned 0)
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException      if the friendship is not valid
     * @throws RepositoryException      if the friendship cannot be updated
     */
    @Override
    public Prietenie update(Prietenie entity) {
        if (entity == null) throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setObject(1, entity.getFriendsFrom());
            statement.setLong(2, entity.getId().getLeft());
            statement.setLong(3, entity.getId().getRight());
            return statement.executeUpdate() == 0 ? entity : null;
        } catch (SQLException e) {
            throw DbErrors.wrap("modificarea prieteniei " + entity.getId(), e);
        }
    }
}