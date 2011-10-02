package search_procedures.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import search_tools.SumDecomposition;


public class SumDecompositionTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void sampleForFourByFour() {
		int sample[][] = new int [][]{
				new int [] {0, 0, 0, 4},
				new int [] {0, 0, 1, 3},
				new int [] {0, 0, 2, 2},
				new int [] {0, 1, 1, 2},
				new int [] {1, 1, 1, 1}
			};
		SumDecomposition decomposition = new SumDecomposition(4, 4);
		checkDecomposition(sample, decomposition);
	}

	@Test
	public void sampleForFourByTree() {
		int decompositions[][] = new int [][]{
				new int [] {0, 0, 4},
				new int [] {0, 1, 3},
				new int [] {0, 2, 2},
				new int [] {1, 1, 2}
			};
		SumDecomposition decomposition = new SumDecomposition(3, 4);
		checkDecomposition(decompositions, decomposition);
	}

	private void checkDecomposition(int[][] sample, SumDecomposition decomposition) {
		
		for (int i = 0; i < sample.length; ++i) {
			assertTrue(decomposition.hasNext());
			int items[] = decomposition.next();
			logger.debug(i + ": " + arrayToString(items));
			assertDeepEquals(sample[i], items);
		}
		assertFalse(decomposition.hasNext());
	}
	
	private void assertDeepEquals(int expected[], int actual[]) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], actual[i]);
		}
	}
	
	private String arrayToString(int array[]) {
		if (array.length == 0) {
			return "[]";
		}
		String str = "[" + array[0];
		for (int i = 1; i < array.length; ++i) {
			str += ", " + array[i];
		}
		str += "]";
		
		return str;
	}
}
