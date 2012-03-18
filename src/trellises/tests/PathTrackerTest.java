package trellises.tests;

import static org.junit.Assert.*;

import java.util.Iterator;

import trellises.algorithms.PathTracker;

public class PathTrackerTest {
	void checkConsistency(PathTracker<?> tracker) {
		assertEquals(tracker.hasForward(), tracker.forwardIterator().hasNext());
		assertEquals(tracker.hasBackward(), tracker.backwardIterator().hasNext());
	}
	
	void checkZeroEdgeForwardTransition(PathTracker tracker) {
		if (!tracker.hasForward()) {
			return ;
		}
		
		Iterator<PathTracker> forwardIterator = tracker.forwardIterator();
		PathTracker forwardTracker = forwardIterator.next();
		assertEquals(tracker.weight(), forwardTracker.weight(), 0);
	}

	void checkZeroEdgeBackwardTransition(PathTracker tracker) {
		if (!tracker.hasBackward()) {
			return ;
		}
		
		Iterator<PathTracker> backwardIterator = tracker.backwardIterator();
		PathTracker backwardTracker = backwardIterator.next();
		assertEquals(tracker.weight(), backwardTracker.weight(), 0);
	}
}
