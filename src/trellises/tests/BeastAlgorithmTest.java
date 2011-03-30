package trellises.tests;

import static org.junit.Assert.*;

import math.Poly;
import math.PolyMatrix;

import org.junit.Test;

import codes.MinDistance;

import trellises.Trellis;
import trellises.Trellises;


public class BeastAlgorithmTest {
	@Test
	public void testConvCodeDistanceSearch() {
		PolyMatrix parityCheck = new PolyMatrix(1, 4);
		parityCheck.set(0, 0, new Poly(new Boolean[]{true, false}));
		parityCheck.set(0, 1, new Poly(new Boolean[]{true, true, true}));
		parityCheck.set(0, 2, new Poly(new Boolean[]{true, true, false, true}));
		parityCheck.set(0, 3, new Poly(new Boolean[]{true, true, true, true}));
		
		Trellis trellis = Trellises.trellisFromParityCheckHR(parityCheck);
		
		MinDistance.computeDistanceMetrics(trellis);

		int VDminDist = MinDistance.findMinDist(trellis, 0, 6);
		int BEASTminDist = MinDistance.findFreeDistWithBEAST(trellis, 0, 6);
		
		System.out.println("Viterby: " + VDminDist);
		System.out.println("BEAST: " + BEASTminDist);
		
		assertEquals(VDminDist, BEASTminDist);
	}
}
