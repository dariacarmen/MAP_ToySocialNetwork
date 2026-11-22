package socialnetwork.repository;

import socialnetwork.domain.Prietenie;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.Utilizator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.util.paging.Page;
import socialnetwork.util.paging.Pageable;

/**
 * A repository of friendships that can also return the friends of a user one
 * page at a time.
 */
public interface FriendshipPagingRepository extends Repository<Tuple<Long, Long>, Prietenie> {

    /**
     * Returns one page of the friends of a user, ordered by id.
     *
     * @param userId   the id of the user; must not be {@code null}
     * @param pageable which page and the size of a page; must not be {@code null}
     * @return the friends on the page and the number of all the friends of the
     *         user; a page after the last one has no elements
     * @throws IllegalArgumentException if {@code userId} or {@code pageable} is {@code null}
     * @throws RepositoryException      if the friends cannot be read
     */
    Page<Utilizator> findFriendsOnPage(Long userId, Pageable pageable);
}
