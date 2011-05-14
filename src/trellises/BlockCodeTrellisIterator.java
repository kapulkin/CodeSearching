package trellises;

import java.util.NoSuchElementException;

public class BlockCodeTrellisIterator implements ITrellisIterator {
	Trellis trellis;
	int layer;
	int vertexIndex;

	public BlockCodeTrellisIterator(Trellis trellis, int layer, int vertexIndex) {
		if (layer < 0 || layer >= trellis.Layers.length) {
			throw new IndexOutOfBoundsException("Wrong value of layer: " + layer);
		}
		
		this.trellis = trellis;
		this.layer = layer;
		this.vertexIndex = vertexIndex;
	}

	@Override
	public int layer() {
		return layer;
	}

	@Override
	public int vertexIndex() {
		return vertexIndex;
	}

	@Override
	public Trellis.Edge[] getAccessors() {
		return trellis.Layers[layer][vertexIndex].Accessors;
	}
	
	@Override
	public Trellis.Edge[] getPredecessors() {
		return trellis.Layers[layer][vertexIndex].Predecessors;
	}

	@Override
	public boolean hasBackward() {
		return layer > 0;
	}

	@Override
	public boolean hasForward() {
		return layer < trellis.Layers.length - 1;
	}

	@Override
	public void moveForward(int edgeIndex) {
		if (!hasForward()) {
			throw new NoSuchElementException();
		}
		vertexIndex = getAccessors()[edgeIndex].Dst;
		layer = (layer + 1) % trellis.Layers.length;
	}
	
	@Override
	public void moveBackward(int edgeIndex) {
		if (!hasBackward()) {
			throw new NoSuchElementException();
		}
		vertexIndex = getPredecessors()[edgeIndex].Src;
		layer = (layer + trellis.Layers.length - 1) % trellis.Layers.length;
	}

	@Override
	public ITrellisIterator clone() {
		return new BlockCodeTrellisIterator(trellis, layer, vertexIndex);
	}
}
