package trellises.algorithms;

import java.util.Set;

/**
 * Фронт вершин при обходе в алгоритме BEAST.
 * @author Stas
 *
 * @param <T>
 */
public interface Front<T> extends Set<T> {
	T get(int layer, long vertexIndex);
	
	Iterable<T> getLayer(int layer);
	
	boolean remove(int layer, long vertexIndex);
	
	boolean contains(int layer, long vertexIndex);
}
