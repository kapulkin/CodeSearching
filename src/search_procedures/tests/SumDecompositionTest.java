package search_procedures.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.tests.ConvCodeTest;

import static org.junit.Assert.*;

import search_tools.SumDecomposition;


public class SumDecompositionTest {
	static final private Logger logger = LoggerFactory.getLogger(SumDecompositionTest.class);
	
	@Test
	public void sampleFor4By4() {
		int sample[][] = new int [][]{
				new int [] {4, 0, 0, 0},
				new int [] {3, 1, 0, 0},
				new int [] {2, 2, 0, 0},
				new int [] {2, 1, 1, 0},
				new int [] {1, 1, 1, 1}
			};
		SumDecomposition decomposition = new SumDecomposition(4, 4);
		checkDecomposition(sample, decomposition);
	}

	@Test
	public void sampleFor4By3() {
		int decompositions[][] = new int [][]{
				new int [] {4, 0, 0},
				new int [] {3, 1, 0},
				new int [] {2, 2, 0},
				new int [] {2, 1, 1}
			};
		SumDecomposition decomposition = new SumDecomposition(4, 3);
		checkDecomposition(decompositions, decomposition);
	}

	@Test
	public void sampleFor8By3() {
		int decompositions[][] = new int [][]{
				new int [] {8, 0, 0},
				new int [] {7, 1, 0},
				new int [] {6, 2, 0},
				new int [] {6, 1, 1},
				new int [] {5, 3, 0},
				new int [] {5, 2, 1},
				new int [] {4, 4, 0},
				new int [] {4, 3, 1},
				new int [] {4, 2, 2},
				new int [] {3, 3, 2}
			};
		SumDecomposition decomposition = new SumDecomposition(8, 3	);
		checkDecomposition(decompositions, decomposition);
	}

	private void checkDecomposition(int[][] sample, SumDecomposition decomposition) {
		logger.debug("number = " + decomposition.getNumber() + ", count = " + decomposition.getSumCount());
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
