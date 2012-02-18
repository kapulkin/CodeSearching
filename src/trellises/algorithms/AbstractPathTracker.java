package trellises.algorithms;

import java.util.Iterator;
import java.util.NoSuchElementException;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

public abstract class AbstractPathTracker<T extends AbstractPathTracker<T>> implements PathTracker<T>, Comparable<PathTracker<?>> {
	/**
	 * Итератор текущей вершины
	 */
	protected final ITrellisIterator iterator;
	/**
	 * Номер метрики на ребрах, используемой для вычисления длины пути
	 */
	protected final int metric;
	/**
	 * Вес пройденнного пути
	 */
	protected int weight;
	
	public AbstractPathTracker(ITrellisIterator iterator, int metric) {
		this.iterator = iterator;
		this.metric = metric;
		this.weight = 0;
	}
	
	public AbstractPathTracker(AbstractPathTracker<T> vertex) {
		iterator = vertex.iterator.clone();
		metric = vertex.metric;
		weight = vertex.weight;
	}
	
	@Override
	public int layer() {
		return iterator.layer();
	}

	@Override
	public long vertexIndex() {
		return iterator.vertexIndex();
	}

	@Override
	public int weight() {
		return weight;
	}

	@Override
	public boolean hasForward() {
		return iterator.hasForward();
	}

	@Override
	public boolean hasBackward() {
		return iterator.hasBackward();
	}

	@Override
	public Iterator<T> forwardIterator() {
		final ITrellisEdge edges[] = iterator.getAccessors();
		
		return new ForwardIterator(edges);
	}

	@Override
	public Iterator<T> backwardIterator() {
		final ITrellisEdge edges[] = iterator.getPredecessors();
		
		return new BackwardIterator(edges);
	}

	public abstract T clone();

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		PathTracker<?> vertex = (PathTracker<?>) obj;
		if (vertex == null) {
			return false;
		}

		return iterator.layer() == vertex.layer() &&
			iterator.vertexIndex() == vertex.vertexIndex();
	}

	public int compareTo(PathTracker<?> tracker) {
		return BeastAlgorithm.comparePathTrackers(this, tracker);
	}

	@Override
	public String toString() {
		return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + weight + ")";
	}

	private class ForwardIterator implements java.util.Iterator<T> {
		int i;
		final ITrellisEdge edges[];
		
		ForwardIterator(ITrellisEdge edges[]) {
			this.i = 0;
			this.edges = edges;
		}
		@Override
		public boolean hasNext() {
			return i < edges.length;
		}

		@Override
		public T next() {
			try {
				final T tracker = T.this.clone();
				tracker.iterator.moveForward(i);
				tracker.weight += edges[i++].metric(metric);
				return tracker;
			} catch (NullPointerException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class BackwardIterator implements java.util.Iterator<T> {
		int i;
		final ITrellisEdge edges[];
		
		BackwardIterator(ITrellisEdge edges[]) {
			this.i = 0;
			this.edges = edges;
		}
		@Override
		public boolean hasNext() {
			return i < edges.length;
		}

		@Override
		public T next() {
			try {
				final T tracker = T.this.clone();
				tracker.iterator.moveBackward(i);
				tracker.weight += edges[i++].metric(metric);
				return tracker;
			} catch (NullPointerException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
