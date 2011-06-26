package trellises;

/**
 * Объект следует вдоль пути решетки при обходе в алгоритме Beast.
 * @author stas
 *
 */
public interface PathTracker extends Cloneable, Comparable<PathTracker> {
	/**
	 * @return итератор, движение которого отслеживается
	 */
	public ITrellisIterator iterator();
	/**
	 * @return вес пути, пройденного итератором
	 */
	public double weight();
	/**
	 * Продвижение по пути вперед.  
	 * @param edgeIndex индекс ребра, по которому происходит движение.
	 * @param edgeWeight вес ребра
	 */
	public void moveForward(int edgeIndex, double edgeWeight);
	/**
	 * Продвижение по пути назад.  
	 * @param edgeIndex индекс ребра, по которому происходит движение.
	 * @param edgeWeight вес ребра
	 */
	public void moveBackward(int edgeIndex, double edgeWeight);
	
	public Object clone();
	public boolean equals(Object obj);
	public int compareTo(PathTracker tracker);
}
