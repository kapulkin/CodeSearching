package trellises;

import java.util.NoSuchElementException;

/**
 * Итератор tailbitnig кода. Строится на основе циклической решетки.
 * @author stas
 *
 */
public class TBCodeTrellisIterator implements ITrellisIterator {
	ITrellisIterator iter;
	int layersCount;
	int layer;

	/**
	 * Создает итератор для tailbiting кода на основе циклической решетки. 
	 *   
	 * @param trellis циклическая решетка кода
	 * @param cycles
	 * @param layer
	 * @param vertexIndex
	 */
	public TBCodeTrellisIterator(ITrellis trellis, int cycles, int layer, int vertexIndex) {
		if (cycles <= 0) {
			throw new IllegalArgumentException("Cycles must be at least 1: " + cycles);
		}
		this.layersCount = cycles * trellis.layersCount();
		if (layer < 0 || layer > layersCount) {
			throw new IndexOutOfBoundsException("Wrong value of layer: " + layer);
		}
		
		this.iter = trellis.iterator(layer % trellis.layersCount(), vertexIndex);
		this.layer = layer;
	}

	@Override
	public int layer() {
		return layer;
	}

	@Override
	public long vertexIndex() {
		return iter.vertexIndex();
	}

	@Override
	public ITrellisEdge[] getAccessors() {
		return iter.getAccessors();
	}
	
	@Override
	public ITrellisEdge[] getPredecessors() {
		return iter.getPredecessors();
	}

	@Override
	public boolean hasBackward() {
		return layer > 0;
	}

	@Override
	public boolean hasForward() {
		return layer < layersCount;
	}

	@Override
	public void moveForward(int edgeIndex) {
		if (!hasForward()) {
			throw new NoSuchElementException();
		}
		iter.moveForward(edgeIndex);
		++layer;
	}
	
	@Override
	public void moveBackward(int edgeIndex) {
		if (!hasBackward()) {
			throw new NoSuchElementException();
		}
		iter.moveBackward(edgeIndex);
		--layer;
	}

	@Override
	public ITrellisIterator clone() {
		TBCodeTrellisIterator tbIter;
		try {
			tbIter = (TBCodeTrellisIterator) super.clone();
			tbIter.iter = iter.clone();
			return tbIter;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}