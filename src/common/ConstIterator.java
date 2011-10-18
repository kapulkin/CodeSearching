package common;

public interface ConstIterator<E> {
	/**
	 * Returns true if the iteration has more elements. (In other words, returns
	 * true if next would return an element rather than throwing an exception.)
	 * 
	 * @return true if the iterator has more elements.
	 */
	boolean hasNext();
	
	/**
	 * Returns the next element in the iteration.
	 * 
	 * @return the next element in the iteration.
	 * @throws NoSuchElementException iteration has no more elements.
	 */
	E next();
}
