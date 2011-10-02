package trellises;

import math.BitArray;

/**
 * 
 * @author fedor
 *
 */
public class Trellis implements ITrellis{
	
	static public class Edge implements ITrellisEdge {
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

		public Edge() {}
		
		public Edge(int src, int dst, BitArray bits, double metrics[]) {
			this.Src = src;
			this.Dst = dst;
			this.Bits = bits;
			this.Metrics = metrics;
		}
		
		@Override
		public long src() {
			return Src;
		}
		@Override
		public long dst() {
			return Dst;
		}
		@Override
		public BitArray bits() {
			return Bits;
		}
		@Override
		public double[] metrics() {
			return Metrics;
		}
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
		if (layer >= layersCount() || vertexIndex >= layerSize(layer)) {
			throw new IndexOutOfBoundsException(layer + ", " + vertexIndex);
		}

		return new TrellisIterator(this, layer, vertexIndex);
	}

	@Override
	public long layerSize(int layer) {
		return Layers[layer].length;
	}

	@Override
	public int layersCount() {
		return Layers.length;
	}
	
	public int stateComplexity(){
		int max_s = 0;
		
		for(int l = 0;l < layersCount();++ l){
			int s = (int)Long.numberOfTrailingZeros(layerSize(l));
			
			if(s > max_s){
				max_s = s;
			}
		}
		
		return max_s;
	}
}
