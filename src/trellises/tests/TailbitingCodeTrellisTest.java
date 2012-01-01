package trellises.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import math.MinDistance;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.ITrellis;

import codes.ConvCode;
import codes.TBCode;

public class TailbitingCodeTrellisTest {
	static final private Logger logger = LoggerFactory.getLogger(TailbitingCodeTrellisTest.class);
	
	@Test
	public void testTrellisWithBEAST1() {
		PolyMatrix generator = new PolyMatrix(1, 2);
		generator.set(0, 0, new Poly(new int [] {2}));
		generator.set(0, 1, new Poly(new int [] {0, 1, 2}));
		
		ConvCode code = new ConvCode(generator, true);
		TBCode tbCode = new TBCode(code, 4);
		ITrellis trellis = tbCode.getTrellis();
		try {
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}

		int minDist = MinDistance.findMinDist(tbCode);
		
		logger.debug("min dist = " + minDist);
		assertEquals(4, minDist);
	}

	@Test
	public void testTrellisWithBEAST2() {
		PolyMatrix generator = new PolyMatrix(1, 2);
		generator.set(0, 0, new Poly(new int [] {0, 1, 2}));
		generator.set(0, 1, new Poly(new int [] {0, 1, 3}));
		
		ConvCode code = new ConvCode(generator, true);
		TBCode tbCode = new TBCode(code, 10);
		ITrellis trellis = tbCode.getTrellis();
		try {
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
//			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}

		int minDist = MinDistance.findMinDist(tbCode);
		
		logger.debug("min dist = " + minDist);
		assertEquals(6, minDist);
	}
}
