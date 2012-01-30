package trellises.algorithms;

import java.util.NoSuchElementException;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

class PathCounter implements PathTracker, Comparable<PathTracker> {
	//TODO: после рефакторинга сделать 4 поля ниже приватными 
	/**
	 * Итератор текущей вершины
	 */
	ITrellisIterator iterator;
	/**
	 * Номер метрики на ребрах, используемой для вычисления длины пути
	 */
	int metric;
	/**
	 * Вес пройденнного пути
	 */
	int weight;
	/**
	 * Колличество путей до текущей вершины
	 */
	long pathNumber;
	
	public PathCounter(ITrellisIterator iterator, int metric) {
		this.iterator = iterator;
		this.metric = metric;
		this.weight = 0;
		this.pathNumber = 1;
	}
	
	public PathCounter(PathCounter vertex) {
		iterator = vertex.iterator.clone();
		metric = vertex.metric;
		weight = vertex.weight;
		pathNumber = vertex.pathNumber;
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
	public java.util.Iterator<PathTracker> forwardIterator() {
		final ITrellisEdge edges[] = iterator.getAccessors();
		
		return new ForwardIterator(edges);
	}

	@Override
	public java.util.Iterator<PathTracker> backwardIterator() {
		final ITrellisEdge edges[] = iterator.getPredecessors();
		
		return new BackwardIterator(edges);
	}

	@Override
	public int layer() {
		return iterator.layer();
	}

	@Override
	public long vertexIndex() {
		return iterator.vertexIndex();
	}

	public int weight() {
		return weight;
	}

	public PathCounter clone() {
		return new PathCounter(this);
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

		return iterator.layer() == vertex.layer() &&
			iterator.vertexIndex() == vertex.vertexIndex();
	}

	public int compareTo(PathTracker tracker) {
		return BeastAlgorithm.comparePathTrackers(this, tracker);
	}

	@Override
	public String toString() {
		return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + weight + ")";
	}
	
	private class ForwardIterator implements java.util.Iterator<PathTracker> {
		int i, length;
		ITrellisEdge edges[];
		
		ForwardIterator(ITrellisEdge edges[]) {
			this.i = 0;
			this.length = edges.length;
			this.edges = edges;
		}
		@Override
		public boolean hasNext() {
			return i < length;
		}

		@Override
		public PathTracker next() {
			try {
				PathCounter tracker = PathCounter.this.clone();
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

	private class BackwardIterator implements java.util.Iterator<PathTracker> {
		int i, length;
		ITrellisEdge edges[];
		
		BackwardIterator(ITrellisEdge edges[]) {
			this.i = 0;
			this.length = edges.length;
			this.edges = edges;
		}
		@Override
		public boolean hasNext() {
			return i < length;
		}

		@Override
		public PathTracker next() {
			try {
				PathCounter tracker = PathCounter.this.clone();
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