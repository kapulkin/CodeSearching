package search_procedures;

import java.util.NoSuchElementException;

/**
 * Enumerates a set of elements of type E in some order. 
 * @author Stas
 *
 * @param <E> the type of elements in the enumerated set.
 */
public interface Enumerator<E> {
	/**
	 * Returns <code>true</code> if the enumeration has more elements.
	 * @return <code>true</code> if the enumeration has more elements.
	 */
	boolean hasNext();
	/**
	 * Returns the next element in the enumeration.
	 * @return the next element in the enumeration.
	 * @throws NoSuchElementException enumeration has no more elements.
	 */
	E next();
}
