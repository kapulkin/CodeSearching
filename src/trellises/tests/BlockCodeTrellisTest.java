package trellises.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.BlockCodeTrellis;
import math.BitArray;
import math.BlockCodeAlgs;
import math.Matrix;
import codes.BlockCode;


public class BlockCodeTrellisTest {
	BlockCode code;
	BlockCodeTrellis trellis;

	Logger logger;
	
	public BlockCodeTrellisTest () {
		logger = LoggerFactory.getLogger(this.getClass());
		
		// Генератор задан в минимальной спеновой форме. Секционированная решетка должна иметь 6 ярусов. 
		BitArray row0 = new BitArray(6); row0.set(0); row0.set(1); row0.set(3);
		BitArray row1 = new BitArray(6); row1.set(1); row1.set(4); row1.set(5);
		BitArray row2 = new BitArray(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitArray[] { row0, row1, row2 });

		code = new BlockCode(generator, true);
	}
	
	@Test
	public void layersCountOfSectionalizedTrellisRestriction() {
		trellis = new BlockCodeTrellis(code);

		assertTrue(trellis.layersCount() <= 2 * code.getK() + 1);
		// для данного кода ярусов должно быть 6
		assertEquals(trellis.layersCount(), 6);
	}
	
	@Test
	public void trellisProfileTest() {
		trellis = new BlockCodeTrellis(code);

		int trellisProfile[] = {0, 1, 2, 2, 1, 0};
		
		for (int i = 0; i < trellis.layersCount(); ++i) {
			assertEquals(trellisProfile[i], trellis.layerComplexity(i));
		}
	}
	
	@Test
	public void trellisForwardTraversalShouldGiveCodeWord() {
		BlockCodeAlgs.sortHeads(code.getGeneratorSpanForm());
		trellis = new BlockCodeTrellis(code);

		TrellisesTest.trellisForwardTraversalShouldGiveCodeWord(code, trellis, 1);
	}

	@Test
	public void trellisBackwardTraversalShouldGiveCodeWord() {
		BlockCodeAlgs.sortTails(code.getGeneratorSpanForm());
		trellis = new BlockCodeTrellis(code);

		TrellisesTest.trellisBackwardTraversalShouldGiveCodeWord(code, trellis);
	}
	
	@Test
	public void visualizeTrellis() {
		trellis = new BlockCodeTrellis(code);
		
		try {
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
}
