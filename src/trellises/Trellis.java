package trellises;


/**
 * 
 * @author fedor
 *
 */
public class Trellis implements ITrellis{
	
	static public class Vertex{
		
		/**
		 * Ребра с началом в данной вершине
		 */
		public IntEdge[] Accessors;
		
		/**
		 * Ребра с концом в данной вершине
		 */
		public IntEdge[] Predecessors;
				
	}
	
	/**
	 *  Массив слоев
	 */
	public Vertex[][] Layers;

	@Override
	public ITrellisIterator iterator(int layer, long vertexIndex) {
		if (layer >= layersCount() || vertexIndex >= layerSize(layer)) {
			throw new IndexOutOfBoundsException(layer + ", " + vertexIndex);
		}

		return new TrellisIterator(this, layer, (int)vertexIndex);
	}

	@Override
	public long layerSize(int layer) {
		return Layers[layer].length;
	}

	@Override
	public int layersCount() {
		return Layers.length;
	}
	
}
