package trellises;

import java.util.NoSuchElementException;

import trellises.Trellis.Edge;

public class TrellisIterator implements ITrellisIterator {
	Trellis trellis;
	int layer;
	int vertexIndex;
	
	public TrellisIterator(Trellis trellis, int layer, int vertexIndex) {
		this.trellis = trellis;
		this.layer = layer;
		this.vertexIndex = vertexIndex;
	}
	
	@Override
	public Edge[] getAccessors() {
		return trellis.Layers[layer][vertexIndex].Accessors;
	}

	@Override
	public Edge[] getPredecessors() {
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
		
		vertexIndex = getPredecessors()[edgeIndex].Src;
		layer = (layer + trellis.Layers.length - 1) % trellis.Layers.length;
	}

	@Override
	public void moveForward(int edgeIndex) throws NoSuchElementException {
		if (!hasForward()) {
			throw new NoSuchElementException();
		}
		
		vertexIndex = getAccessors()[edgeIndex].Dst;
		layer = (layer + 1) % trellis.Layers.length;
	}

	@Override
	public int vertexIndex() {
		return vertexIndex;
	}

	@Override
	public ITrellisIterator clone() {
		return new TrellisIterator(trellis, layer, vertexIndex);
	}	
}
