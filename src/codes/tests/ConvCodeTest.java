package codes.tests;

import static org.junit.Assert.*;

import math.BitArray;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

public class ConvCodeTest {
	static final private Logger logger = LoggerFactory.getLogger(ConvCodeTest.class);

	ConvCode code;

	
	public ConvCodeTest() {
		PolyMatrix G = new PolyMatrix(2, 3);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 1}));
		G.set(0, 1, new Poly(new int[] {1}));
		G.set(0, 2, Poly.getUnitPoly());
		// row 1
		G.set(1, 0, new Poly(new int[] {2}));
		G.set(1, 1, Poly.getUnitPoly());
		G.set(1, 2, new Poly(new int[] {0, 1, 2}));
		
		code = new ConvCode(G, true);
	}

	@Test
	public void shouldEncodeWithProperRate() {
		int k = code.getK();
		int n = code.getN();
		
		logger.debug("(" + k + ", " + n + ") code.");
		
		BitArray word = new BitArray(k); word.set(0); word.set(1);
		assertTrue(code.encodeSeq(word).getFixedSize() == n);

		BitArray word2 = new BitArray(2 * k); word2.set(0); word2.set(2); word2.set(3);
		assertTrue(code.encodeSeq(word2).getFixedSize() == 2 * n);

		BitArray word3 = new BitArray(3 * k); word3.set(0); word3.set(2); word3.set(4); word3.set(5);
		assertTrue(code.encodeSeq(word3).getFixedSize() == 3 * n);
	}

}
