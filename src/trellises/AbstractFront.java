package trellises;

import java.util.AbstractSet;

abstract public class AbstractFront<T extends PathTracker> extends AbstractSet<T> implements Front<T> {
	protected int size;

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		T t = (T)o;
		if (t == null) {
			return false;
		}
		return contains(t.layer(), t.vertexIndex());
	}

	@Override
	public boolean remove(Object o) {
		T t = (T)o;
		if (t == null) {
			return false;
		}
		return remove(t.layer(), t.vertexIndex());
	}
}
