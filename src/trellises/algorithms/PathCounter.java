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
}