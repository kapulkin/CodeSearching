package trellises;

import java.util.NoSuchElementException;


public class LightTrellis implements ITrellis {
	static public class Vertex {
		public LightIntEdge accessors[];
		public LightIntEdge predecessors[];
	}

	public class Iterator implements ITrellisIterator {
		int layer;
		int vertexIndex;
		
		public Iterator(int layer, int vertexIndex) {
			this.layer = layer;
			this.vertexIndex = vertexIndex;					
		}
		
		@Override
		public boolean hasForward() {
			return layers[layer][vertexIndex].accessors.length > 0;
		}

		@Override
		public boolean hasBackward() {
			return layers[layer][vertexIndex].predecessors.length > 0;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!hasForward()) {
				throw new NoSuchElementException();
			}

			vertexIndex = layers[layer][vertexIndex].accessors[edgeIndex].dst;
			layer = (layer + 1) % layersCount();;
		}

		@Override
		public void moveBackward(int edgeIndex) throws NoSuchElementException {
			if (!hasBackward()) {
				throw new NoSuchElementException();
			}

			int prevLayer = (layer - 1 + layersCount()) % layersCount();
			vertexIndex = layers[layer][vertexIndex].predecessors[edgeIndex].src;
			layer = prevLayer;
		}

		@Override
		public ITrellisEdge[] getAccessors() {
			return layers[layer][vertexIndex].accessors;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			return layers[layer][vertexIndex].predecessors;
		}

		@Override
		public int layer() {
			return layer;
		}

		@Override
		public long vertexIndex() {
			return vertexIndex;
		}
		
		@Override
		public Iterator clone() {
			return new Iterator(layer, vertexIndex);
		}
	}
	
	Vertex layers[][];

	public LightTrellis(ITrellis trellis) {
		layers = new Vertex[trellis.layersCount()][];
		
		for (int layer = 0; layer < layers.length; ++layer) {
			long layerSize = trellis.layerSize(layer);
			if (layerSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Trellis contains layers of length more, then " + Integer.MAX_VALUE);
			}
			layers[layer] = new Vertex[(int)layerSize];
			for (int j = 0; j < layers[layer].length; ++j) {
				Vertex vertex = layers[layer][j] = new Vertex();
				
				ITrellisIterator iterator = trellis.iterator(layer, j);
				ITrellisEdge accessors[] = iterator.getAccessors();
				vertex.accessors = new LightIntEdge[accessors.length];
				for (int i = 0; i < accessors.length; ++i) {
					vertex.accessors[i] = new LightIntEdge(accessors[i]);
				}
				
				ITrellisEdge predecessors[] = iterator.getPredecessors();
				vertex.predecessors = new LightIntEdge[predecessors.length];
				for (int i = 0; i < predecessors.length; ++i) {
					vertex.predecessors[i] = new LightIntEdge(predecessors[i]);
				}
			}
		}
	}
	
	@Override
	public int layersCount() {
		return layers.length;
	}

	@Override
	public long layerSize(int layer) {
		return layers[layer].length;
	}

	@Override
	public ITrellisIterator iterator(int layer, long vertexIndex) {
		return new Iterator(layer, (int)vertexIndex);
	}

}
