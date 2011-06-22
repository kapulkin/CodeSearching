package trellises.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.BeastAlgorithm;
import trellises.BlockCodeTrellis;
import trellises.ITrellis;
import trellises.ITrellisIterator;
import trellises.TBCodeTrellisIterator;
import trellises.TrellisPath;

import codes.ConvCode;
import codes.TBCode;

public class TBCodeTrellisIteratorTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void testIteratorWithBEAST1() {
		PolyMatrix generator = new PolyMatrix(1, 2);
		generator.set(0, 0, new Poly(new int [] {2}));
		generator.set(0, 1, new Poly(new int [] {0, 1, 2}));
		
		ConvCode code = new ConvCode(generator, true);
		TBCode tbCode = new TBCode(code, 4);
		ITrellis trellis = new BlockCodeTrellis(tbCode.getGeneratorSpanForm());
		try {
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}

		int minDist = Integer.MAX_VALUE;
		for (int vertexIndex = 0; vertexIndex < trellis.layerSize(0); ++vertexIndex) {
			ITrellisIterator root = new TBCodeTrellisIterator(trellis, 1, 0, vertexIndex);
			ITrellisIterator toor = new TBCodeTrellisIterator(trellis, 1, trellis.layersCount(), vertexIndex);
			TrellisPath paths[] = BeastAlgorithm.findOptimalPaths(root, toor, 0, 5);
			
			for (int i = 0; i < paths.length; ++i) {
				minDist = Math.min(minDist, (int)paths[i].weight());
			}
		}
		
		logger.debug("min dist = " + minDist);
		assertEquals(4, minDist);
	}

	@Test
	public void testIteratorWithBEAST2() {
		PolyMatrix generator = new PolyMatrix(1, 2);
		generator.set(0, 0, new Poly(new int [] {0, 1, 2}));
		generator.set(0, 1, new Poly(new int [] {0, 1, 3}));
		
		ConvCode code = new ConvCode(generator, true);
		TBCode tbCode = new TBCode(code, 10);
		ITrellis trellis = new BlockCodeTrellis(tbCode.getGeneratorSpanForm());
		try {
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}

		int minDist = Integer.MAX_VALUE;
		for (int vertexIndex = 0; vertexIndex < trellis.layerSize(0); ++vertexIndex) {
			ITrellisIterator root = new TBCodeTrellisIterator(trellis, 1, 0, vertexIndex);
			ITrellisIterator toor = new TBCodeTrellisIterator(trellis, 1, trellis.layersCount(), vertexIndex);
			TrellisPath paths[] = BeastAlgorithm.findOptimalPaths(root, toor, 0, 5);
			
			for (int i = 0; i < paths.length; ++i) {
				minDist = Math.min(minDist, (int)paths[i].weight());
			}
		}
		
		logger.debug("min dist = " + minDist);
		assertEquals(6, minDist);
	}
}
