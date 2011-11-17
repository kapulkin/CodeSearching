package trellises.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.ConvCodeTrellis;
import trellises.ITrellis;
import trellises.ITrellisEdge;
import trellises.ITrellisIterator;
import codes.ConvCode;
import codes.tests.ConvCodeTest;
import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.Poly;
import math.PolyMatrix;


public class ConvCodeTrellisTest {
	static final private Logger logger = LoggerFactory.getLogger(ConvCodeTrellisTest.class);
	
	PolyMatrix G;

	public ConvCodeTrellisTest() {
		G = new PolyMatrix(2, 3);
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
	public void trellisShouldCorresponfToCodeWords() {
		trellisShouldCorresponfToCodeWords(G);
	}
	
	@Test
	public void CaseWith01111111111111Code() {
		PolyMatrix generator = new PolyMatrix(1, 2);
		// row 0
		generator.set(0, 0, new Poly(new boolean[] {false, true, true, true, true, true, true}));
		generator.set(0, 1, new Poly(new boolean[] {true, true, true, true, true, true, true}));
		trellisShouldCorresponfToCodeWords(generator);
		randomTraversalShouldBeEquivalentForTrellises(generator);
	}

	@Test
	public void randomTraversalShouldBeEquivalentForTrellises() {
		randomTraversalShouldBeEquivalentForTrellises(G);
	}

	private void randomTraversalShouldBeEquivalentForTrellises(PolyMatrix generator) {
		PolyMatrix minBaseG = ConvCodeAlgs.getMinimalBaseGenerator(generator);
		ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(minBaseG); // should be done without exceptions

		ITrellis explicitTrellis = ConvCodeAlgs.buildTrellis(spanForm);
		ITrellis lightTrellis = new ConvCodeTrellis(spanForm);
		
		assertEquals(explicitTrellis.layersCount(), lightTrellis.layersCount());
		for (int layer = 0; layer < explicitTrellis.layersCount(); ++layer) {
			assertEquals(explicitTrellis.layerSize(layer), lightTrellis.layerSize(layer));
		}
		
		// Пройдемся в течении ~1000 шагов
		int steps = 1000;
		
		Random rand = new Random();
		int layer = rand.nextInt(explicitTrellis.layersCount());
		int vertexIndex = rand.nextInt((int)explicitTrellis.layerSize(layer)); // вершины в явной решетке только размера int
		logger.debug("layer = " + layer + ", vertexIndex = " + vertexIndex);
		
		ITrellisIterator explicit = explicitTrellis.iterator(layer, vertexIndex);
		ITrellisIterator light = lightTrellis.iterator(layer, vertexIndex);
		
		for (int i = 0; i < steps; ++i) {
			boolean direction = rand.nextBoolean();
			
			ITrellisEdge explicitEdges[];
			ITrellisEdge lightEdges[];
			if (direction) {
				explicitEdges = explicit.getAccessors();
				lightEdges = light.getAccessors();
				
			} else {
				explicitEdges = explicit.getPredecessors();
				lightEdges = light.getPredecessors();
			}
			
			assertEquals(explicitEdges.length, lightEdges.length);
			int edge = rand.nextInt(explicitEdges.length);
			if (direction) {
				logger.debug(explicitEdges[edge].src() + "‒" + explicitEdges[edge].bits() + "→" + explicitEdges[edge].dst());
			} else {
				logger.debug(explicitEdges[edge].src() + "←" + explicitEdges[edge].bits() + "‒" + explicitEdges[edge].dst());
			}

			for (int e = 0; e < explicitEdges.length; ++e) {
				assertEquals(explicitEdges[e].src(), lightEdges[e].src());
				assertEquals(explicitEdges[e].dst(), lightEdges[e].dst());
				assertEquals(explicitEdges[e].bits(), lightEdges[e].bits());
			}
			
			if (direction) {
				explicit.moveForward(edge);
				light.moveForward(edge);
			} else {
				explicit.moveBackward(edge);
				light.moveBackward(edge);
			}
			
			assertEquals(explicit.layer(), light.layer());
			assertEquals(explicit.vertexIndex(), light.vertexIndex());
			logger.debug("layer = " + explicit.layer() + ", vertexIndex = " + explicit.vertexIndex());
		}
	}

	private void trellisShouldCorresponfToCodeWords(PolyMatrix generator) {
		PolyMatrix minBaseG = ConvCodeAlgs.getMinimalBaseGenerator(generator);
		ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(minBaseG); // should be done without exceptions

		ConvCodeAlgs.sortHeads(spanForm);
		ITrellis trellis1 = new ConvCodeTrellis(spanForm);
		
		try {
			IOTrellis.writeTrellisInGVZFormat(trellis1, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		int delay = spanForm.delay;
		TrellisesTest.trellisForwardTraversalShouldGiveCodeWord(new ConvCode(spanForm), trellis1, delay + 1);
	}
}
