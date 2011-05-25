package trellises;

import java.util.NoSuchElementException;

/**
 * Итератор для обхода решетки, постоенной по дереву (сверточного кода). В виду полубесконечности 
 * дерева, итератор принимает на вход индекс 
 * вершины в решетке и <b>глубину вершины в дереве</b> вместо яруса решетки.
 * Это позволяет итератору бесконечно двигаться по решетке вперед и не более, 
 * чем до корня дерева, назад.  
 * 
 * @author stas
 *
 */
public class ConvCodeTrellisIterator implements ITrellisIterator {
	Trellis trellis;
	int depth;
	int vertexIndex;
	
	ConvCodeTrellisIterator(Trellis trellis, int depth, int vertexIndex) {
		this.trellis = trellis;
		this.depth = depth;
		this.vertexIndex = vertexIndex;
	}
	
	public int depth() {
		return depth;
	}
	
	@Override
	public int layer() {
		return depth % trellis.Layers.length;
	}
	
	@Override
	public long vertexIndex() {
		return vertexIndex;
	}

	@Override
	public boolean hasForward() {
		return true;
	}

	@Override
	public boolean hasBackward() {
		return depth != 0;
	}
	
	@Override
	public Trellis.Edge[] getAccessors() {
		return trellis.Layers[layer()][vertexIndex].Accessors;
	}
	
	@Override
	public Trellis.Edge[] getPredecessors() {
		return trellis.Layers[layer()][vertexIndex].Predecessors;
	}
	
	@Override
	public void moveForward(int edgeIndex) {
		vertexIndex = getAccessors()[edgeIndex].Dst;
		++depth;
	}
	
	@Override
	public void moveBackward(int edgeIndex) {
		if (!hasBackward()) {
			throw new NoSuchElementException("The iterator points to the root of nodes tree.");
		}
		vertexIndex = getPredecessors()[edgeIndex].Src;
		--depth;
	}
	
	@Override
	public ITrellisIterator clone() {
		return new ConvCodeTrellisIterator(trellis, depth, vertexIndex);
	}
}
