package trellises;

import java.util.Iterator;

/**
 * Позволяет двигаться по решетке, отслеживая необходимую информацию о пройденном в решетке пути.
 * 
 * Используется в алгоритме BEAST.
 * @author stas
 *
 */
public interface PathTracker {
	public int layer();
	public long vertexIndex();
	/**
	 * @return вес пройденного пути
	 */
	public double weight();
	/**
	 * @return <code>true</code>, если возможно движение вперед
	 */
	boolean hasForward();
	/**
	 * @return <code>true</code>, если возможно движение назад
	 */
	boolean hasBackward();
	/**
	 * Метод возвращает итератор на множество следующих вершин в решетке. Первой
	 * вершиной, возвращаемой итератором, должна быть вершина, полученная при 
	 * переходе по ребру нулевого веса.
	 * @return итератор на следующие вершины в решетке 
	 */
	Iterator<PathTracker> forwardIterator();
	/**
	 * Метод возвращает итератор на множество предыдущих вершин в решетке. Первой
	 * вершиной, возвращаемой итератором, должна быть вершина, полученная при 
	 * переходе по ребру нулевого веса.
	 * @return итератор на предыдущие вершины в решетке
	 */
	Iterator<PathTracker> backwardIterator();
}
