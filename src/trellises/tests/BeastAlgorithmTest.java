package trellises.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import in_out_interfaces.IOPolyMatrix;
import in_out_interfaces.IOTrellis;

import math.BitArray;
import math.BlockCodeAlgs;
import math.Matrix;
import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;

import search_procedures.FreeDist4CCEnumerator;
import trellises.BeastAlgorithm;
import trellises.ITrellisIterator;
import trellises.Trellis;
import trellises.Trellises;


public class BeastAlgorithmTest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testHighRateConvCodeDistanceSearch() {
		FreeDist4CCEnumerator codeEnumerator = new FreeDist4CCEnumerator(5, 9);
		
		while (codeEnumerator.hasNext()) {
			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.parityCheck());

			Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());
			MinDistance.computeDistanceMetrics(trellis);
			
			int VDminDist = MinDistance.findMinDist(trellis, 0, 2 * (code.getDelay() + 1));
			int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
			
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
		int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN());
		
		System.out.println("Viterby: " + VDminDist);
		System.out.println("BEAST: " + BEASTminDist);
		
		assertEquals(VDminDist, BEASTminDist);
	}
	
	@Test
	public void testConvCodeSpectrumSearch() {
//		String codeStr = "601, 615, 753, 705, 537, 567, 551, 443, 635, 1161, 1067";
//		long expected[] = {0, 0, 0, 0, 231, 3529, 52239};
		String codeStr = "3, 5, 1, 7, 11, 13";
		long expected[] = {0, 0, 15, 98, 625};

		ConvCode code = null;
		try {
			code = new ConvCode(IOPolyMatrix.readMatrixOct(new BufferedReader(new StringReader(codeStr))), false);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}
		
		Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());

		try {
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
		
		MinDistance.computeDistanceMetrics(trellis);
		ITrellisIterator root = trellis.iterator(0, 0);
		ITrellisIterator toor = trellis.iterator(trellis.layersCount() - 1, 0);
		long spectrum[] = BeastAlgorithm.findSpectrum(root, toor, expected.length, 0);

		if (logger.isDebugEnabled()) {
			String str = "[";
			if (spectrum.length > 0) {
				str += spectrum[0];
				for (int i = 1; i < spectrum.length; ++i) {
					str += ", " + spectrum[i];
				}
			}
			str += "]";
			logger.debug(str);
		}
		
		for (int i = 0; i < spectrum.length; ++i) {
			assertEquals(expected[i], spectrum[i]);
		}
	}
}
