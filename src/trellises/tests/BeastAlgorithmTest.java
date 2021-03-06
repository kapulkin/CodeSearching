package trellises.tests;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOPolyMatrix;
import in_out_interfaces.IOTrellis;

import math.BitArray;
import math.BlockCodeAlgs;
import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.ConvCodeSpanForm.SpanFormException;
import math.Matrix;
import math.MinDistance;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;
import codes.ZTCode;

import search_procedures.conv_codes.FreeDist4CCEnumerator;
import trellises.ITrellisIterator;
import trellises.Trellis;
import trellises.Trellises;
import trellises.algorithms.BeastAlgorithm;


public class BeastAlgorithmTest {
	static final private Logger logger = LoggerFactory.getLogger(BeastAlgorithmTest.class);

	@Test
	public void beastInfiniteEvaluation() throws IOException, SpanFormException {
		PolyMatrix mat = new PolyMatrix(1, 3);
		
		mat.set(0, 0, new Poly(new int[] {0}));
		mat.set(0, 1, new Poly(new int[] {0, 1, 2}));
		mat.set(0, 2, new Poly(new int[] {0, 1, 2}));
		
		ConvCode code = new ConvCode(mat, false);
		
		IOTrellis.writeTrellisInGVZFormat(code.getTrellis(), new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		
		long start = System.currentTimeMillis();
		logger.info("BEAST: {}", code.getFreeDist());
		logger.info("time: {}ms", System.currentTimeMillis() - start);
	}
	
	@Test
	public void distanceTest() throws SpanFormException {
		PolyMatrix mat = new PolyMatrix(1, 3);
		
		mat.set(0, 0, new Poly(new int[] {0, 1, 2, 4, 5, 6, 10}));
		mat.set(0, 1, new Poly(new int[] {0, 3, 4, 5, 9, 10}));
		mat.set(0, 2, new Poly(new int[] {0, 2, 5, 6, 8, 9, 10}));
		
		ConvCode code = new ConvCode(mat, false);
		
		logger.info("BEAST: {}", code.getFreeDist());
		assertTrue(code.getFreeDist() == 7);
	}
	
	//@Test
	public void testHighRateConvCodeDistanceSearch() {
		FreeDist4CCEnumerator codeEnumerator = new FreeDist4CCEnumerator(5, 9);
		
		while (codeEnumerator.hasNext()) {
			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.parityCheck());

			Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());
			MinDistance.computeDistanceMetrics(trellis);
			
			int VDminDist = MinDistance.findMinDistWithViterby(trellis, 0, 2 * (code.getDelay() + 1), true);
			int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, code.getN() * (code.getDelay() + 1));
			
			System.out.println("Viterby: " + VDminDist);
			System.out.println("BEAST: " + BEASTminDist);
			
			assertEquals(VDminDist, BEASTminDist);
		}
	}
	
	//@Test
	public void testLowRateConvCodeDistanceSearch() {
		try {
			// code of rate 1/3 with m=6 from page 357;
			String g[] = {"574", "664", "774"};
	
			PolyMatrix G = new PolyMatrix(1, g.length);
			for (int i = 0; i < g.length; ++i) {
				boolean p[] = new boolean[3 * g[i].length()];
				for (int j = 0; j < g[i].length(); ++j) {
					boolean oct[] = null;
					int digit = Integer.parseInt(g[i].substring(j, j+1), 8);
					switch (digit) {
					case 0: oct = new boolean[] {false, false, false}; break;
					case 1: oct = new boolean[] {false, false, true};  break;
					case 2: oct = new boolean[] {false, true, false};  break;
					case 3: oct = new boolean[] {false, true, true};   break;
					case 4: oct = new boolean[] {true, false, false};  break;
					case 5: oct = new boolean[] {true, false, true};   break;
					case 6: oct = new boolean[] {true, true, false};   break;
					case 7: oct = new boolean[] {true, true, true};    break;
					}
					for (int k = 0; k < oct.length; ++k) {
						p[3 * j + k] = oct[k];
					}
				}
				G.set(0, i, new Poly(p));
			}
		
			ConvCode code = new ConvCode(G, true);
			ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(ConvCodeAlgs.getMinimalBaseGenerator(code.generator()));
			Trellis trellis = ConvCodeAlgs.buildTrellis(spanForm);
			MinDistance.computeDistanceMetrics(trellis);

			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		
			int VDminDist = MinDistance.findMinDistWithViterby(trellis, 0, 2 * (code.getDelay() + 1), true);
			int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, code.getN() * (code.getDelay() + 1));
			int TrivialMinDist = MinDistance.findMinDist(new ZTCode(code, code.getDelay()).generator());
	
			System.out.println("Viterby: " + VDminDist);
			System.out.println("BEAST: " + BEASTminDist);
			System.out.println("Trivial: " + TrivialMinDist);
			
			assertEquals(TrivialMinDist, BEASTminDist);
			assertEquals(VDminDist, BEASTminDist);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	
	//@Test
	public void testBlockCodeDistanceSearch() {
		BitArray row0 = new BitArray(6); row0.set(0); row0.set(1); row0.set(3);
		BitArray row1 = new BitArray(6); row1.set(1); row1.set(4); row1.set(5);
		BitArray row2 = new BitArray(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitArray[] { row0, row1, row2 });
		
		BlockCode code = new BlockCode(generator, true);
		Trellis trellis = BlockCodeAlgs.buildExplicitTrellis(code);
		
		int VDminDist = MinDistance.findMinDistWithViterby(trellis, 0, 0, false);
		int BEASTminDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN());
		
		System.out.println("Viterby: " + VDminDist);
		System.out.println("BEAST: " + BEASTminDist);
		
		assertEquals(VDminDist, BEASTminDist);
	}
	
	//@Test
	public void testTailbitingCodeDistanceSearch() throws SpanFormException {
		String strCode = "131, 117";
		
		PolyMatrix G = null;
		try {
			G = IOPolyMatrix.readMatrixOct(new BufferedReader(new StringReader(strCode)));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}
		
		ConvCode code = new ConvCode(G, true);
		TBCode tbCode = new TBCode(code, 0);
		Trellis trellis = BlockCodeAlgs.buildExplicitTrellis((BlockCode)tbCode);
		
		try {
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

		int TrivialMinDist = MinDistance.findMinDist(tbCode.generator());
		int VDminDist = MinDistance.findMinDistWithViterby(trellis, 0, 0, false);
		int BEASTminDist = MinDistance.findMinDist(tbCode);
		
		System.out.println("Viterby: " + VDminDist);
		System.out.println("BEAST: " + BEASTminDist);
		System.out.println("Trivial: " + TrivialMinDist);
		
		assertEquals(TrivialMinDist, BEASTminDist);
		assertEquals(VDminDist, BEASTminDist);
	}
	
	@Test(timeout=1000)
	public void shouldNotLoop() throws SpanFormException {
		Poly poly0 = new Poly(new boolean[] {true, false, true});//new int[] {0, 2});
		Poly poly1 = new Poly(new boolean[] {true, true, true});//int[] {0, 1, 2});
		
		PolyMatrix parityCheck = new PolyMatrix(1, 2);
		parityCheck.set(0, 0, poly0);
		parityCheck.set(0, 1, poly1);
		
		ConvCode code = new ConvCode(parityCheck, false);
		
		long start = System.currentTimeMillis();
		System.out.println("BEAST: " + code.getFreeDist());
		System.out.println("time: " + (double) (System.currentTimeMillis() - start) / 1000 + "s");
	}

	@Test
	public void testCaseForFoundThrowingDownCode() throws SpanFormException {
		Poly poly0 = new Poly(new int[] {0, 3});
		Poly poly1 = new Poly(new int[] {0, 1, 2, 4, 5});
		Poly poly2 = new Poly(new int[] {0, 1, 2, 4, 5});

		PolyMatrix parityCheck = new PolyMatrix(1, 3);
		parityCheck.set(0, 0, poly0);
		parityCheck.set(0, 1, poly1);
		parityCheck.set(0, 2, poly2);
		
		ConvCode code = new ConvCode(parityCheck, false);
		// calculate generator to force the broken distance calculating method
		code.generator();
		int freeDist = code.getFreeDist();
		System.out.println("freeDist: " + freeDist);
	}	
	
//	@Test
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
