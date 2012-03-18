package trellises.algorithms;

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
}