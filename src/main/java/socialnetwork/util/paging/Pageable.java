package socialnetwork.util.paging;

/**
 * A request for one page of a list: which page and how many elements a page has.
 * <p>
 * Pages are numbered from 0: the first page is page 0. The page that starts at
 * the element with the index {@code pageNumber * pageSize} is requested.
 */
public class Pageable {
    /** The number of the requested page, starting from 0. */
    private final int pageNumber;
    /** The maximum number of elements on a page. */
    private final int pageSize;

    /**
     * Creates a request for a page.
     *
     * @param pageNumber the number of the page, starting from 0
     * @param pageSize   the maximum number of elements on a page
     * @throws IllegalArgumentException if {@code pageNumber < 0} or {@code pageSize <= 0}
     */
    public Pageable(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Numarul paginii trebuie sa fie cel putin 1.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Marimea paginii trebuie sa fie pozitiva.");
        }
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    /**
     * Returns the number of the requested page.
     *
     * @return the number of the page, starting from 0
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Returns the maximum number of elements on a page.
     *
     * @return the size of a page
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns the index of the first element of the page, used as the
     * {@code OFFSET} of the SQL query.
     *
     * @return {@code pageNumber * pageSize}
     */
    public long getOffset() {
        return (long) pageNumber * pageSize;
    }
}
