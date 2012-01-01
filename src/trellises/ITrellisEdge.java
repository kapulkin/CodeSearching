package trellises;

import math.BitArray;

public interface ITrellisEdge {
	/**
	 * Индекс вершины, из которой выходит ребро
	 * @return индекс вершины, из которой выходит ребро
	 */
	public long src();
	/**
	 * Индекс вершины, в которую ведет ребро
	 * @return индекс вершины, в которую ведет ребро
	 */
	public long dst();

	public BitArray bits();
	
	public int metric(int i);
	
	public int[] metrics();
}
