package trellises;

import java.util.Iterator;
import java.util.TreeSet;

public class TreeSetFront<T extends PathTracker> extends TreeSet<T> implements Front<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7982211384892302419L;

	static class MockTracker implements PathTracker, Comparable<PathTracker> {
		int layer;
		long vertexIndex;
		
		public MockTracker(int layer, long vertexIndex) {
			this.layer = layer;
			this.vertexIndex = vertexIndex;
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
		public double weight() {
			return 0;
		}

		@Override
		public boolean hasForward() {
			return false;
		}

		@Override
		public boolean hasBackward() {
			return false;
		}

		@Override
		public Iterator<PathTracker> forwardIterator() {
			return null;
		}

		@Override
		public Iterator<PathTracker> backwardIterator() {
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			PathTracker vertex = (PathTracker) obj;
			if (vertex == null) {
				return false;
			}

			return layer == vertex.layer() && vertexIndex == vertex.vertexIndex();
		}

		@Override
		public int compareTo(PathTracker tracker) {
			return BeastAlgorithm.comparePathTrackers(this, tracker);
		}

	}
	
	@Override
	public T get(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		T t = floor((T)tracker);
		return tracker.equals(t) ? t : null;
	}

	@Override
	public boolean remove(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		return remove((T)tracker);
	}

	@Override
	public boolean contains(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		return contains(tracker);
	}
}
