package math.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;

import trellises.Trellis;
import trellises.tests.TrellisesTest;

import codes.ConvCode;

public class ConvCodeAlgsTest {
	PolyMatrix G;
	
	public ConvCodeAlgsTest() {
		G = new PolyMatrix(2, 3);
/*		// row 0
		G.set(0, 0, new Poly(new int[] {0, 1}));
		G.set(0, 1, new Poly(new int[] {1}));
		G.set(0, 2, Poly.getUnitPoly());
		// row 1
		G.set(1, 0, new Poly(new int[] {2}));
		G.set(1, 1, Poly.getUnitPoly());
		G.set(1, 2, new Poly(new int[] {0, 1, 2}));/**/
		
		// row 0
		G.set(0, 0, new Poly(new int[] {1}));
		G.set(0, 1, new Poly(new int[] {0, 1}));
		G.set(0, 2, new Poly(new int[] {0, 1}));
		// row 1
		G.set(1, 0, new Poly(new int[] {0, 1, 2}));
		G.set(1, 1, new Poly(new int[] {1}));
		G.set(1, 2, Poly.getUnitPoly());
	}
	
	@Test
	public void testBuildSpanForm() {
		try {
			PolyMatrix minBaseG = ConvCodeAlgs.getMinimalBaseGenerator(G);
			ConvCodeAlgs.buildSpanForm(minBaseG); // should be done without exceptions
		} catch (Exception e) {
			fail("Unexpected exception:\n" + e);
		}
	}

	@Test
	public void testBuildTrellis() {
		PolyMatrix minBaseG = ConvCodeAlgs.getMinimalBaseGenerator(G);
		ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(minBaseG); // should be done without exceptions

		ConvCodeAlgs.sortHeads(spanForm);
		Trellis trellis1 = ConvCodeAlgs.buildTrellis(spanForm);
		
		try {
			IOTrellis.writeTrellisInGVZFormat(trellis1, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		int delay = spanForm.matrices.length - 1;
		TrellisesTest.trellisForwardTraversalShouldGiveCodeWord(new ConvCode(spanForm.matrices), trellis1, delay + 1);

		ConvCodeAlgs.sortHeads(spanForm);
		Trellis trellis2 = ConvCodeAlgs.buildTrellis(spanForm);
		TrellisesTest.trellisBackwardTraversalShouldGiveCodeWord(new ConvCode(spanForm.matrices), trellis2);
	}
}
