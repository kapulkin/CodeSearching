package trellises;

import java.util.NoSuchElementException;


public class TrellisIterator implements ITrellisIterator {
	final Trellis trellis;
	int layer;
	int vertexIndex;
	
	public TrellisIterator(final Trellis trellis, int layer, int vertexIndex) {
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
	public boolean hasBackward() {
		return trellis.Layers[layer][vertexIndex].Predecessors.length > 0;
	}

	@Override
	public boolean hasForward() {
		return trellis.Layers[layer][vertexIndex].Accessors.length > 0;
	}

	@Override
	public int layer() {
		return layer;
	}

	@Override
	public void moveBackward(int edgeIndex) throws NoSuchElementException {
		if (!hasBackward()) {
			throw new NoSuchElementException();
		}
		
		vertexIndex = getPredecessors()[edgeIndex].src;
		layer = (layer + trellis.Layers.length - 1) % trellis.Layers.length;
	}

	@Override
	public void moveForward(int edgeIndex) throws NoSuchElementException {
		if (!hasForward()) {
			throw new NoSuchElementException();
		}
		
		vertexIndex = getAccessors()[edgeIndex].dst;
		layer = (layer + 1) % trellis.Layers.length;
	}

	@Override
	public long vertexIndex() {
		return vertexIndex;
	}

	@Override
	public ITrellisIterator clone() {
		return new TrellisIterator(trellis, layer, vertexIndex);
	}	
}
