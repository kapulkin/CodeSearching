package trellises.tests;

import static org.junit.Assert.*;

import math.BitArray;
import math.BlockCodeAlgs;
import math.Matrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.MinDistance;

import search_procedures.FreeDist4CCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class BeastAlgorithmTest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testConvCodeDistanceSearch() {
		FreeDist4CCEnumerator codeEnumerator = new FreeDist4CCEnumerator(5, 8);
		
		while (codeEnumerator.hasNext()) {
			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.checkMatrix());

			Trellis trellis = Trellises.trellisFromParityCheckHR(code.checkMatrix());
			MinDistance.computeDistanceMetrics(trellis);
			
			int VDminDist = MinDistance.findMinDist(trellis, 0, 2 * (code.getDelay() + 1));
			int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (code.getDelay() + 1));
			
			System.out.println("Viterby: " + VDminDist);
			System.out.println("BEAST: " + BEASTminDist);
			
			assertEquals(VDminDist, BEASTminDist);
		}
	}
	
	@Test
	public void testBlockCodeDistanceSearch() {
		BitArray row0 = new BitArray(6); row0.set(0); row0.set(1); row0.set(3);
		BitArray row1 = new BitArray(6); row1.set(1); row1.set(4); row1.set(5);
		BitArray row2 = new BitArray(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitArray[] { row0, row1, row2 });
		
		BlockCode code = new BlockCode(generator, true);
		Trellis trellis = BlockCodeAlgs.buildTrellis(code);
		
		int VDminDist = MinDistance.findMinDist(trellis, 0, 0);
		int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, 0, 0);
		
		System.out.println("Viterby: " + VDminDist);
		System.out.println("BEAST: " + BEASTminDist);
		
		assertEquals(VDminDist, BEASTminDist);
	}
}
