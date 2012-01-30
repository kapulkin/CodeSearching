package trellises;

import java.util.NoSuchElementException;

import math.BitArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConjunctedEdgesTrellis implements ITrellis {
	static private Logger logger = LoggerFactory.getLogger(ConjunctedEdgesTrellis.class);
	
	static public class Vertex {
		public int accessorsEdges[];
		public int predecessorsEdges[];
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
			return layers[layer][vertexIndex].accessorsEdges.length > 0;
		}

		@Override
		public boolean hasBackward() {
			return layers[layer][vertexIndex].predecessorsEdges.length > 0;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!hasForward()) {
				throw new NoSuchElementException();
			}

			vertexIndex = edges[layer].getEdge(layers[layer][vertexIndex].accessorsEdges[edgeIndex]).dst;
			layer = (layer + 1) % layersCount();
		}

		@Override
		public void moveBackward(int edgeIndex) throws NoSuchElementException {
			if (!hasBackward()) {
				throw new NoSuchElementException();
			}

			int prevLayer = (layer - 1 + layersCount()) % layersCount();
			vertexIndex = edges[prevLayer].getEdge(layers[layer][vertexIndex].predecessorsEdges[edgeIndex]).src;
			layer = prevLayer;
		}

		@Override
		public ITrellisEdge[] getAccessors() {
			if (!hasForward()) {
				return new ITrellisEdge[0];
			}
			ITrellisEdge newEdges[] = new ITrellisEdge[layers[layer][vertexIndex].accessorsEdges.length];
			
			for (int i = 0; i < newEdges.length; ++i) {
				newEdges[i] = edges[layer].getEdge(layers[layer][vertexIndex].accessorsEdges[i]);
			}

			return newEdges;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			if (!hasBackward()) {
				return new ITrellisEdge[0];
			}
			ITrellisEdge newEdges[] = new ITrellisEdge[layers[layer][vertexIndex].predecessorsEdges.length];
			
			int prevLayer = (layer - 1 + layersCount()) % layersCount();
			for (int i = 0; i < newEdges.length; ++i) {
				newEdges[i] = edges[prevLayer].getEdge(layers[layer][vertexIndex].predecessorsEdges[i]);
			}

			return newEdges;
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
	
	public Vertex layers[][];
	public EdgesArray edges[];

	public ConjunctedEdgesTrellis(ITrellis trellis) {
		long start, end;
		
		start = System.currentTimeMillis();
		layers = new Vertex[trellis.layersCount()][];
		int edgeSizes[] = new int[layers.length];
		
		for (int layer = 0; layer < layers.length; ++layer) {
			long layerSize = trellis.layerSize(layer);
			if (layerSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Trellis contains layers of length more, then " + Integer.MAX_VALUE);
			}
			layers[layer] = new Vertex[(int)layerSize];
			for (int j = 0; j < layers[layer].length; ++j) {
				Vertex vertex = layers[layer][j] = new Vertex();
				
				ITrellisIterator iterator = trellis.iterator(layer, j);
				vertex.accessorsEdges = new int[iterator.getAccessors().length]; 
				edgeSizes[layer] += vertex.accessorsEdges.length;
				
				vertex.predecessorsEdges = new int[iterator.getPredecessors().length];
			}
		}
		end = System.currentTimeMillis();
		logger.info("common structure building time = {}s", (double)(end - start) / 1000);

		start = System.currentTimeMillis();
		edges = new EdgesArray[edgeSizes[layers.length - 1] > 0 ? layers.length : layers.length - 1];
		for (int i = 0; i < edges.length; ++i) {
			BitArray bits = trellis.iterator(i, 0).getAccessors()[0].bits();
			
			edges[i] = new EdgesArray(edgeSizes[i], (bits != null ? bits.getFixedSize() : 0));
			
			int nextLayer = (i + 1 ) % layersCount();

			for (int j = 0; j < layers[i].length; ++j) {
				Vertex vertex = layers[i][j];
				ITrellisEdge iEdges[] = trellis.iterator(i, j).getAccessors();
				for (int e = 0; e < vertex.accessorsEdges.length; ++e) {
					int edgeIndex = edges[i].addEdge(iEdges[e]);

					vertex.accessorsEdges[e] = edgeIndex;

					Vertex nextVertex = layers[nextLayer][(int)iEdges[e].dst()];
					
					// в последнем элементе nextVertex.predecessorsEdges храним колличество заполненных элементов.
					// когда заполнится сам последний элемент, то это будет последняя запись в nextVertex.predecessorsEdges
					int pos = nextVertex.predecessorsEdges[nextVertex.predecessorsEdges.length-1]++;
					nextVertex.predecessorsEdges[pos] = edgeIndex;
				}
			}
		}
		end = System.currentTimeMillis();
		logger.info("edges building time = {}s", (double)(end - start) / 1000);
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
		if (layer >= layersCount() || vertexIndex >= layerSize(layer)) {
			throw new IndexOutOfBoundsException(layer + ", " + vertexIndex);
		}

		return new Iterator(layer, (int)vertexIndex);
	}
}
