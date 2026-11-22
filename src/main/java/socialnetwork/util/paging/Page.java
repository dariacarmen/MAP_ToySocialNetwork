package socialnetwork.util.paging;

import java.util.List;

/**
 * One page of a list: the elements on the page and the number of elements of
 * the whole list.
 *
 * @param <E> the type of the elements
 */
public class Page<E> {
    /** The elements on this page (at most the size of a page). */
    private final List<E> elementsOnPage;
    /** The number of elements of the whole list, on all the pages. */
    private final long totalNumberOfElements;

    /**
     * Creates a page.
     *
     * @param elementsOnPage        the elements on this page
     * @param totalNumberOfElements the number of elements of the whole list
     */
    public Page(List<E> elementsOnPage, long totalNumberOfElements) {
        this.elementsOnPage = List.copyOf(elementsOnPage);
        this.totalNumberOfElements = totalNumberOfElements;
    }

    /**
     * Returns the elements on this page.
     *
     * @return the elements on this page; an empty list for a page after the last one
     */
    public List<E> getElementsOnPage() {
        return elementsOnPage;
    }

    /**
     * Returns the number of elements of the whole list.
     *
     * @return the number of elements on all the pages
     */
    public long getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

    /**
     * Computes the number of pages for the given page size. An empty list has
     * one (empty) page.
     *
     * @param pageSize the maximum number of elements on a page
     * @return the number of pages, at least 1
     * @throws IllegalArgumentException if {@code pageSize <= 0}
     */
    public long getNumberOfPages(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Marimea paginii trebuie sa fie pozitiva.");
        }
        return Math.max(1, (totalNumberOfElements + pageSize - 1) / pageSize);
    }
}