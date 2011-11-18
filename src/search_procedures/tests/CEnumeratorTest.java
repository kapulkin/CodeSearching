package search_procedures.tests;

import org.junit.Test;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.tests.ConvCodeTest;

import search_tools.CEnumerator;

public class CEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(CEnumeratorTest.class);
	
	@Test
	public void sample() {
		long n = 5;
		CEnumerator enumerator = new CEnumerator(n, 2);
		for (long i = 0; i < n*(n-1)/2; ++i) {
			assertTrue(enumerator.hasNext());
			enumerator.next();
		}
		assertFalse(enumerator.hasNext());
	}

	public void zeroElementsShouldGiveTheOnlyOneCombination() {
		long n = 5;
		CEnumerator enumerator = new CEnumerator(n, 0);
		assertTrue(enumerator.hasNext());
		assertEquals(0, enumerator.next().length);
		assertFalse(enumerator.hasNext());
	}

	public void fullElementsShouldGiveTheOnlyOneCombination() {
		int n = 5;
		CEnumerator enumerator = new CEnumerator(n, n);
		assertTrue(enumerator.hasNext());
		assertEquals(n, enumerator.next().length);
		assertFalse(enumerator.hasNext());
	}
}
