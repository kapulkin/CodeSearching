package trellises.algorithms;

import java.util.Iterator;
import java.util.TreeSet;


public class TreeSetFront extends TreeSet<PathTracker<?>> implements Front<PathTracker<?>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7982211384892302419L;

	static class MockTracker implements PathTracker<MockTracker>, Comparable<PathTracker<?>> {
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
		public int weight() {
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
		public Iterator<MockTracker> forwardIterator() {
			return null;
		}

		@Override
		public Iterator<MockTracker> backwardIterator() {
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			PathTracker<?> vertex = (PathTracker<?>) obj;
			if (vertex == null) {
				return false;
			}

			return layer == vertex.layer() && vertexIndex == vertex.vertexIndex();
		}

		@Override
		public int compareTo(PathTracker<?> tracker) {
			return BeastAlgorithm.comparePathTrackers(this, tracker);
		}

	}
	
	@Override
	public PathTracker<?> get(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		PathTracker<?> t = floor(tracker);
		return tracker.equals(t) ? t : null;
	}

	@Override
	public Iterable<PathTracker<?>> getLayer(int layer) {
		MockTracker low = new MockTracker(layer, 0);
		MockTracker high = new MockTracker(layer + 1, 0);
		return subSet(low, high);
	}

	@Override
	public boolean remove(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		return remove(tracker);
	}

	@Override
	public boolean contains(int layer, long vertexIndex) {
		MockTracker tracker = new MockTracker(layer, vertexIndex);
		return contains(tracker);
	}
}
