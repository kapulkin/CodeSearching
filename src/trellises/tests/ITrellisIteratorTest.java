package trellises.tests;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

public class ITrellisIteratorTest {
	public void checkConsistency(ITrellisIterator iterator) {
		assertEquals(iterator.hasForward(), iterator.getAccessors().length > 0);
		assertEquals(iterator.hasBackward(), iterator.getPredecessors().length > 0);
		
		ITrellisEdge accessors[] = iterator.getAccessors();
		if (accessors.length > 0) {
			try {
				iterator.clone().moveForward(accessors.length - 1);
				iterator.clone().moveForward(0);
			} catch (NoSuchElementException e) {
				fail("Unexpected exception: " + e);
			}
			try {
				iterator.clone().moveForward(accessors.length);
				fail("This code shouldn't be excecuted, exception must be thrown before.");
			} catch (NoSuchElementException e) {
			
			}
		}
		
		ITrellisEdge predecessors[] = iterator.getPredecessors();
		if (predecessors.length > 0) {
			try {
				iterator.clone().moveForward(predecessors.length - 1);
				iterator.clone().moveForward(0);
			} catch (NoSuchElementException e) {
				fail("Unexpected exception: " + e);
			}
			try {
				iterator.clone().moveForward(predecessors.length);
				fail("This code shouldn't be excecuted, exception must be thrown before.");
			} catch (NoSuchElementException e) {
			
			}
		}
	}
}
