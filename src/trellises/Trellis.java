package trellises;

import math.BitSet;

/**
 * 
 * @author fedor
 *
 */
public class Trellis {
	
	public class Edge{
		
		/**
		 * Индекс начала ребра в слое 
		 */
		public int Src;
		
		/**
		 * Индекс конца ребра в слое
		 */
		public int Dst;
		
		/**
		 * Метрики ребра (кол-во единичек, вероятность и т.д.)
		 */
		public double[] Metrics;
		
		/**
		 * Кодовые символы на ребре
		 */
		public BitSet Bits;
	} 
	
	public class Vertex{
		
		/**
		 * Ребра с началом в данной вершине
		 */
		public Edge[] Accessors;
		
		/**
		 * Ребра с концом в данной вершине
		 */
		public Edge[] Predecessors;
				
	}
	
	/**
	 *  Массив слоев
	 */
	public Vertex[][] Layers;
}
