package math.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.ConvCodeSpanForm.SpanFormException;
import math.MinDistance;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;

import trellises.Trellis;

import codes.ConvCode;
import codes.ZTCode;


public class MinDistanceTest {
	@Test
	public void freeDistanceComputingTimeScore() throws SpanFormException {
		PolyMatrix G = new PolyMatrix(2, 3);
		// row 0
		G.set(0, 0, new Poly(new int[] {1}));
		G.set(0, 1, new Poly(new int[] {0, 1}));
		G.set(0, 2, new Poly(new int[] {0, 1}));
		// row 1
		G.set(1, 0, new Poly(new int[] {0, 1, 2}));
		G.set(1, 1, new Poly(new int[] {1}));
		G.set(1, 2, Poly.getUnitPoly());

		ConvCode code = new ConvCode(G, true);
		ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(ConvCodeAlgs.getMinimalBaseGenerator(code.generator()));
		Trellis trellis = ConvCodeAlgs.buildTrellis(spanForm);
		MinDistance.computeDistanceMetrics(trellis);

		try {
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
		
		long startTime, endTime;

		startTime = System.nanoTime();
		int TrivialMinDist = MinDistance.findMinDist(new ZTCode(code, code.getDelay()).generator());
		endTime = System.nanoTime();
		System.out.println("Trivial: dist = " + TrivialMinDist + ", time = " + (endTime - startTime));

		startTime = System.nanoTime();
		int VDminDist = MinDistance.findMinDistWithViterby(trellis, 0, 2 * (code.getDelay() + 1), true);
		endTime = System.nanoTime();
		System.out.println("Viterby: dist = " + VDminDist + ", time = " + (endTime - startTime));

		startTime = System.nanoTime();
		int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, code.getN() * (code.getDelay() + 1));
		endTime = System.nanoTime();
		System.out.println("BEAST:   dist = " + BEASTminDist + ", time = " + (endTime - startTime));
		
		assertEquals(TrivialMinDist, BEASTminDist);
		assertEquals(VDminDist, BEASTminDist);
	}
}
