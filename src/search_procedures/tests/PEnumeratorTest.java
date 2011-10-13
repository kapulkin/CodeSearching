package search_procedures.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.TestUtils;
import common.ToString;

import search_procedures.PEnumerator;

public class PEnumeratorTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void sampleFor3() {
		int sample[][] = new int [][]{
//				new int [] {0, 1, 2},
//				new int [] {1, 0, 2},
//				new int [] {0, 2, 1},
//				new int [] {1, 2, 0},
//				new int [] {2, 0, 1},
//				new int [] {2, 1, 0}

				new int [] {0, 1, 2},
				new int [] {0, 2, 1},
				new int [] {1, 0, 2},
				new int [] {1, 2, 0},
				new int [] {2, 0, 1},
				new int [] {2, 1, 0}
			};
		PEnumerator enumerator = new PEnumerator(3);
		checkPermutation(sample, enumerator);
	}

	@Test
	public void sampleFor1() {
		PEnumerator enumerator = new PEnumerator(1);
		checkPermutation(new int[][] {new int[] {0}}, enumerator);
	}
	
	@Test
	public void shouldGiveNFactorialCombinations() {
		for (int n = 1, f = 1; n <= 11; ++n) {
			logger.debug("n = " + n);
			f *= n;
			PEnumerator enumerator = new PEnumerator(n);
			for (int i = 0; i < f; ++i) {
				assertTrue(enumerator.hasNext());
				enumerator.next();
			}
			assertFalse(enumerator.hasNext());
		}
		logger.debug("done");
	}
	
	private void checkPermutation(int[][] sample, PEnumerator enumerator) {
		logger.debug("number = " + enumerator.getN());
		for (int i = 0; i < sample.length; ++i) {
			assertTrue(enumerator.hasNext());
			int items[] = enumerator.next();
			logger.debug(i + ": " + ToString.arrayToString(items));
			TestUtils.assertDeepEquals(sample[i], items);
		}
		assertFalse(enumerator.hasNext());
	}
}
