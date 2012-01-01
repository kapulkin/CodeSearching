package trellises;

import java.util.NoSuchElementException;


public class CyclicTrellisIterator implements ITrellisIterator {
	Trellis trellis;
	int layer;
	int vertexIndex;
	
	public CyclicTrellisIterator(Trellis trellis, int layer, int vertexIndex) {
		this.trellis = trellis;
		this.layer = layer;
		this.vertexIndex = vertexIndex;
	}
	
	@Override
	public IntEdge[] getAccessors() {
		return trellis.Layers[layer][vertexIndex].Accessors;
	}

	@Override
	public IntEdge[] getPredecessors() {
		return trellis.Layers[layer][vertexIndex].Predecessors;
	}

	@Override
	public boolean hasForward() {
		return true;
	}

	@Override
	public boolean hasBackward() {
		return true;
	}

	@Override
	public int layer() {
		return layer;
	}

	@Override
	public void moveForward(int edgeIndex) throws NoSuchElementException {
		vertexIndex = getAccessors()[edgeIndex].dst;
		layer = (layer + 1) % trellis.Layers.length;
	}

	@Override
	public void moveBackward(int edgeIndex) throws NoSuchElementException {
		vertexIndex = getPredecessors()[edgeIndex].src;
		layer = (layer + trellis.Layers.length - 1) % trellis.Layers.length;
	}

	@Override
	public long vertexIndex() {
		return vertexIndex;
	}

	@Override
	public ITrellisIterator clone() {
		return new CyclicTrellisIterator(trellis, layer, vertexIndex);
	}	
}
