package trellises;

import java.util.NoSuchElementException;

public class TrellisPath {
	static class PathTail {
		long index;
		PathTail prev;
		
		public PathTail(long index) {
			this.index = index;
			this.prev = null;
		}
		
		public PathTail(long index, PathTail prev) {
			this.index = index;
			this.prev = prev;
		}
	}
	
	static class PathIterator {
		PathTail tail;
		public PathIterator(PathTail tail) {
			this.tail = tail;
		}
		
		public boolean hasPrev() {
			return tail != null;
		}
		
		public long prev() {
			if (!hasPrev()) {
				throw new NoSuchElementException();
			}
			long index = tail.index;
			tail = tail.prev;
			
			return index;
		}
	}
	
	/**
	 * Длина пути, т.е. колличество вершин в пути.
	 */
	private int length;
	/**
	 * Вес пути
	 */
	private double weight;

	private PathTail tail;
	
	public TrellisPath(long vertexIndex) {
		length = 1;
		weight = 0;
		tail = new PathTail(vertexIndex);
	}
	
	public TrellisPath(TrellisPath path) {
		length = path.length;
		weight = path.weight;
		tail = path.tail;
	}
	
	public int length() {
		return length;
	}
	
	public double weight() {
		return weight;
	}
	
	public void addVertex(long vertexIndex, double delta_weight) {
		PathTail newTail = new PathTail(vertexIndex, tail);
		tail = newTail;
		
		++length;
		weight += delta_weight;
	}
	
	public void addReversedPath(PathIterator iterator, double weight) {
		for (; iterator.hasPrev(); ) {
			addVertex(iterator.prev(), 0);
		}
		
		this.weight += weight;
	}

	public PathIterator iterator() {
		return new PathIterator(tail);
	}
	
	@Override
	public String toString() {
		// По построению путь содержит хотя бы одну вершину.
		PathIterator iter = iterator();
		String str = iter.prev() + " : " + weight;
		while (iter.hasPrev()) {
			str = iter.prev() + "-" + str;
		}
		return str;
	}
}
