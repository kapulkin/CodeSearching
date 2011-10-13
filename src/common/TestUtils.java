package common;

import static org.junit.Assert.assertEquals;

public class TestUtils {
	public static void assertDeepEquals(int expected[], int actual[]) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; ++i) {
			assertEquals(expected[i], actual[i]);
		}
	}
}
