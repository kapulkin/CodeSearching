package trellises.algorithms;

import java.util.NoSuchElementException;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

class PathCounter extends AbstractPathTracker<PathCounter> {
	//TODO: после рефакторинга сделать поле ниже приватными 
	long pathNumber;
	
	public PathCounter(ITrellisIterator iterator, int metric) {
		super(iterator, metric);
		this.pathNumber = 1;
	}
	
	public PathCounter(PathCounter vertex) {
		super(vertex);
		pathNumber = vertex.pathNumber;
	}

	public PathCounter clone() {
		return new PathCounter(this);
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
	
	private class ForwardIterator implements java.util.Iterator<PathCounter> {
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
		public PathCounter next() {
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