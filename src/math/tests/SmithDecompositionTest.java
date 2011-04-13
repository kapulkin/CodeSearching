package math.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import math.Poly;
import math.PolyMatrix;
import math.SmithDecomposition;

import org.junit.Test;


public class SmithDecompositionTest {
	@Test
	public void validSmithDecompositionExample() {
		PolyMatrix G = new PolyMatrix(2, 3);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 1}));
		G.set(0, 1, new Poly(new int[] {1}));
		G.set(0, 2, new Poly(new int[] {0}));
		// row 1
		G.set(1, 0, new Poly(new int[] {2}));
		G.set(1, 1, new Poly(new int[] {0}));
		G.set(1, 2, new Poly(new int[] {0, 1, 2}));
		
		SmithDecomposition decomposition = new SmithDecomposition(G);
		
		PolyMatrix A = new PolyMatrix(2, 2);
		// row 0
		A.set(0, 0, new Poly(new int[] {0}));
		A.set(0, 1, new Poly());
		// row 1
		A.set(1, 0, new Poly(new int[] {0, 1, 2}));
		A.set(1, 1, new Poly(new int[] {0}));
		
		PolyMatrix B = new PolyMatrix(3, 3);
		// row 0
		B.set(0, 0, new Poly(new int[] {0, 1}));
		B.set(0, 1, new Poly(new int[] {1}));
		B.set(0, 2, new Poly(new int[] {0}));
		// row 1
		B.set(1, 0, new Poly(new int[] {0, 2, 3}));
		B.set(1, 1, new Poly(new int[] {0, 1, 2, 3}));
		B.set(1, 2, new Poly());
		// row 2
		B.set(2, 0, new Poly(new int[] {1, 2}));
		B.set(2, 1, new Poly(new int[] {0, 1, 2}));
		B.set(2, 2, new Poly());
		
		PolyMatrix D = new PolyMatrix(2, 3);
		// row 0
		D.set(0, 0, Poly.getUnitPoly());
		D.set(0, 1, new Poly());
		D.set(0, 2, new Poly());
		// row 1
		D.set(1, 0, new Poly());
		D.set(1, 1, Poly.getUnitPoly());
		D.set(1, 2, new Poly());

		PolyMatrix G_ = decomposition.getA().mul(decomposition.getD()).mul(decomposition.getB());
		try {
			System.out.println("A:");
			IOPolyMatrix.writeMatrix(decomposition.getA(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("B:");
			IOPolyMatrix.writeMatrix(decomposition.getB(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("D:");
			IOPolyMatrix.writeMatrix(decomposition.getD(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("InvA:");
			IOPolyMatrix.writeMatrix(decomposition.getInvA(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("InvB:");
			IOPolyMatrix.writeMatrix(decomposition.getInvB(), new BufferedWriter(new OutputStreamWriter(System.out)));
			
			System.out.println("G':");
			IOPolyMatrix.writeMatrix(G_, new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}

//		assertTrue(A.equals(decomposition.getA()));
//		assertTrue(B.equals(decomposition.getB()));

		assertTrue(D.equals(decomposition.getD()));
		assertTrue(G.equals(G_));
		
		assertTrue(decomposition.getA().mul(decomposition.getInvA()).equals(PolyMatrix.getIdentity(2)));
		assertTrue(decomposition.getB().mul(decomposition.getInvB()).equals(PolyMatrix.getIdentity(3)));
	}
}
