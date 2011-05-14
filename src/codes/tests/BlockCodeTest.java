package codes.tests;

import static org.junit.Assert.*;

import math.BitArray;
import math.Matrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;

public class BlockCodeTest {
	BlockCode code;
	Logger logger;

	public BlockCodeTest() {
		logger = LoggerFactory.getLogger(this.getClass());

		BitArray row0 = new BitArray(6); row0.set(0); row0.set(1); row0.set(3);
		BitArray row1 = new BitArray(6); row1.set(1); row1.set(4); row1.set(5);
		BitArray row2 = new BitArray(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitArray[] { row0, row1, row2 });

		code = new BlockCode(generator, true);
	}

	@Test
	public void shouldEncodeWithAProperRate() {
		int k = code.getK();
		int n = code.getN();
		
		logger.debug("(" + k + ", " + n + ") code.");
		
		BitArray word = new BitArray(k); word.set(0); word.set(2);
		assertTrue(code.encodeSeq(word).getFixedSize() == n);

		BitArray word2 = new BitArray(2 * k); word2.set(0); word2.set(2); word2.set(4); word2.set(5);
		assertTrue(code.encodeSeq(word2).getFixedSize() == 2 * n);

		BitArray word3 = new BitArray(3 * k); word3.set(0); word3.set(2); word3.set(4); word3.set(5); word3.set(7);
		assertTrue(code.encodeSeq(word3).getFixedSize() == 3 * n);
	}

}
