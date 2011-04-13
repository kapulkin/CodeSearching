package trellises;

import common.Common;

import math.BitSet;

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
		public BitSet Bits;
		
		@Override
		public String toString() {
			return Src + "‒" + Common.bitsToString(Bits, Bits.getFixedSize()) + "→" + Dst;
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
	public TrellisIterator iterator(int layer, int vertexIndex) {
		return new CyclicTrellisIterator(this, layer, vertexIndex);
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
