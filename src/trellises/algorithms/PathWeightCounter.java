package trellises.algorithms;

import trellises.ITrellisIterator;

public class PathWeightCounter extends AbstractPathTracker<PathWeightCounter> {
	public PathWeightCounter(ITrellisIterator iterator, int metric) {
		super(iterator, metric);
	}
	
	public PathWeightCounter(PathWeightCounter vertex) {
		super(vertex);
	}

	@Override
	public PathWeightCounter clone() {
		return new PathWeightCounter(this);
	}
}
