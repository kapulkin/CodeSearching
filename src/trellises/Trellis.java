package trellises;

import math.BitArray;

/**
 * 
 * @author fedor
 *
 */
public class Trellis implements ITrellis{
	
	static public class Edge{
		
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
		public BitArray Bits;
		
		@Override
		public String toString() {
			return Src + "‒" + Bits + "→" + Dst;
		}
	} 
	
	static public class Vertex{
		
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

	@Override
	public ITrellisIterator iterator(int layer, int vertexIndex) {
		return new TrellisIterator(this, layer, vertexIndex);
	}

	@Override
	public int layerSize(int layer) {
		return Layers[layer].length;
	}

	@Override
	public int layersCount() {
		return Layers.length;
	}
}
