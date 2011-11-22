package trellises;

import java.util.NoSuchElementException;

import math.BitArray;

public class TrellisProduction implements ITrellis {
	ITrellis trellis1, trellis2;

	public class Iterator implements ITrellisIterator {
		ITrellisIterator iter1, iter2;
		
		public Iterator(int layer, long vertexIndex) {
			iter1 = trellis1.iterator(layer, vertexIndex % trellis1.layerSize(layer));
			iter2 = trellis2.iterator(layer, vertexIndex / trellis1.layerSize(layer));
		}
		
		public Iterator(ITrellisIterator iter1, ITrellisIterator iter2) {
			this.iter1 = iter1.clone();
			this.iter2 = iter2.clone();
		}

		@Override
		public boolean hasForward() {
			return iter1.hasForward();
		}

		@Override
		public boolean hasBackward() {
			return iter1.hasBackward();
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!iter1.hasForward()) {
				throw new NoSuchElementException();
			}

			ITrellisEdge edges1[] = iter1.getAccessors();
			int edgeIndex1 = edgeIndex % edges1.length;
			int edgeIndex2 = edgeIndex / edges1.length;
			
			iter1.moveForward(edgeIndex1);
			iter2.moveForward(edgeIndex2);
		}

		@Override
		public void moveBackward(int edgeIndex) throws NoSuchElementException {
			if (!iter1.hasBackward()) {
				throw new NoSuchElementException();
			}

			ITrellisEdge edges1[] = iter1.getPredecessors();
			int edgeIndex1 = edgeIndex % edges1.length;
			int edgeIndex2 = edgeIndex / edges1.length;
			
			iter1.moveBackward(edgeIndex1);
			iter2.moveBackward(edgeIndex2);
		}

		@Override
		public ITrellisEdge[] getAccessors() {
			if (!hasForward()) {
				return new ITrellisEdge[0];
			}
			ITrellisEdge edges1[] = iter1.getAccessors();
			ITrellisEdge edges2[] = iter2.getAccessors();
			
			ITrellisEdge edges[] = new LongEdge[edges1.length * edges2.length];
			
			long src = edges2[0].src() * edges1.length + edges1[0].src();
			for (int i = 0; i < edges1.length; ++i) {
				for (int j = 0; j < edges2.length; ++j) {
					long dst = edges2[j].dst() * edges1.length + edges[i].dst();
					BitArray bits = edges1[i].bits().clone();
					bits.xor(edges2[j].bits());
					edges[i + j * edges1.length] = new LongEdge(src, dst, bits);
				}
			}
			
			return edges;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			if (!hasBackward()) {
				return new ITrellisEdge[0];
			}
			ITrellisEdge edges1[] = iter1.getPredecessors();
			ITrellisEdge edges2[] = iter2.getPredecessors();
			
			ITrellisEdge edges[] = new LongEdge[edges1.length * edges2.length];
			
			long dst = edges2[0].dst() * edges1.length + edges1[0].dst();
			for (int i = 0; i < edges1.length; ++i) {
				for (int j = 0; j < edges2.length; ++j) {
					long src = edges2[j].src() * edges1.length + edges[i].src();
					BitArray bits = edges1[i].bits().clone();
					bits.xor(edges2[j].bits());
					edges[i + j * edges1.length] = new LongEdge(src, dst, bits);
				}
			}
			
			return edges;
		}

		@Override
		public int layer() {
			return iter1.layer();
		}

		@Override
		public long vertexIndex() {
			return iter1.vertexIndex() + iter2.vertexIndex() * trellis1.layerSize(layer());
		}
		
		public Iterator clone() {
			return new Iterator(iter1, iter2);
		}
	}
	
	/**
	 * Строит произведение решеток. Входные решетки должны иметь одинаковое секционирование. 
	 * @param trellis1
	 * @param trellis2
	 */
	public TrellisProduction(ITrellis trellis1, ITrellis trellis2) {
		if (trellis1.layersCount() != trellis2.layersCount()) {
			throw new IllegalArgumentException("Trellises have different layers counts: " + trellis1.layersCount() + ", " + trellis2.layersCount());
		}

		ITrellisIterator iter1 = trellis1.iterator(0, 0);
		ITrellisIterator iter2 = trellis2.iterator(0, 0);
		for (int i = 0; i < trellis1.layersCount(); ++i) {
			ITrellisEdge edges1[] = iter1.getAccessors();
			ITrellisEdge edges2[] = iter2.getAccessors();
			if ((edges1.length == 0) ^ (edges2.length == 0)) {
				throw new IllegalArgumentException("Trellises have different sections.");
			}
			if (edges1.length != 0 && edges1.length != 0) {
				if (edges1[0].bits().getFixedSize() != edges2[0].bits().getFixedSize()) {
					throw new IllegalArgumentException("Trellises have different sections.");
				}
			}
		}
		
		this.trellis1 = trellis1;
		this.trellis2 = trellis2;
	}
	
	@Override
	public int layersCount() {
		return trellis1.layersCount();
	}

	@Override
	public long layerSize(int layer) {
		return trellis1.layerSize(layer) * trellis2.layerSize(layer);
	}

	@Override
	public ITrellisIterator iterator(int layer, long vertexIndex) {
		return new Iterator(layer, vertexIndex);
	}
}
