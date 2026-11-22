package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.util.paging.Page;
import socialnetwork.util.paging.Pageable;

/**
 * A repository that can also return the entities one page at a time.
 *
 * @param <ID> the type of the identifier of the entities
 * @param <E>  the type of the entities
 */
public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {

    /**
     * Returns one page of the entities, ordered by id.
     *
     * @param pageable which page and the size of a page; must not be {@code null}
     * @return the entities on the page and the number of all the entities; a page
     *         after the last one has no elements
     * @throws IllegalArgumentException if {@code pageable} is {@code null}
     * @throws RepositoryException      if the entities cannot be read
     */
    Page<E> findAllOnPage(Pageable pageable);
}
