package trellises.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.BitArray;
import math.BlockCodeAlgs;
import math.Matrix;
import math.Poly;
import math.PolyMatrix;

import trellises.BlockCodeTrellis;
import trellises.ITrellis;
import trellises.ITrellisEdge;
import trellises.Trellis;
import trellises.ITrellisIterator;
import trellises.Trellises;
import codes.BlockCode;
import codes.Code;

public class TrellisesTest {
	private static Logger logger = LoggerFactory.getLogger(TrellisesTest.class);

	BlockCode code;
	
	public TrellisesTest() {
		// Генератор задан в минимальной спеновой форме. Секционированная решетка должна иметь 6 ярусов. 
		BitArray row0 = new BitArray(6); row0.set(0); row0.set(1); row0.set(3);
		BitArray row1 = new BitArray(6); row1.set(1); row1.set(4); row1.set(5);
		BitArray row2 = new BitArray(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitArray[] { row0, row1, row2 });

		code = new BlockCode(generator, true);
	}
	
	@Test
	public void explicitTrellisConstructionFromBlockCodeTrellisTest() {

		BlockCodeAlgs.sortHeads(code.getGeneratorSpanForm());
		Trellis trellis1 = Trellises.buildExplicitTrellis(new BlockCodeTrellis(code.getGeneratorSpanForm()));
		trellisForwardTraversalShouldGiveCodeWord(code, trellis1, 1);

		BlockCodeAlgs.sortTails(code.getGeneratorSpanForm());
		Trellis trellis2 = Trellises.buildExplicitTrellis(new BlockCodeTrellis(code.getGeneratorSpanForm()));
		trellisBackwardTraversalShouldGiveCodeWord(code, trellis2);
	}
	
	@Test
	public void trellisFromParityCheckHRTest() {
		PolyMatrix parityCheck = new PolyMatrix(1, 4);
		parityCheck.set(0, 0, Poly.getUnitPoly());
		parityCheck.set(0, 1, new Poly(new int[]{0, 1}));
		parityCheck.set(0, 2, new Poly(new int[]{0, 2}));
		parityCheck.set(0, 3, new Poly(new int[]{0, 1, 2}));
		
		Trellis trellis = Trellises.trellisFromParityCheckHR(parityCheck);
		try {
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
		// TODO: отсортировать строки проверочной матрицы аналогично sortHeads и sortTails 
		//trellisForwardTraversalShouldGiveCodeWord(new ConvCode(parityCheck, false), trellis, 1);
	}
	
	
	/**
	 * Метод тестирует решетку <code>trellis</code>, построенную для кода
	 * <code>code</code>. Метод перебирает все информационные последовательности
	 * длиной <code>wordsNumber</code> слов и проверяет корректность построенной
	 * решеткой кодовой последовательнотью.
	 * 
	 * Стоит заметить, что стандартная решетка <strong>блокового</strong> кода позволяет 
	 * построить кодовую последовательность, состоящую только из одного кодового
	 * слова. Т.о. для нее корректная работа метода гарантируется только при
	 * <code>wordsNumber</code> равном 1.
	 * 
	 * Данный метод не работает для tailbiting-кода. 
	 * 
	 * @param code код
	 * @param trellis проверяемая решетка
	 * @param wordsNumber длина информационной последовательности в информационных словах
	 */
	public static void trellisForwardTraversalShouldGiveCodeWord(Code code, ITrellis trellis, int wordsNumber) {
		int infLength = code.getK() * wordsNumber;
		int codeLength = code.getN() * wordsNumber;
		
		if (infLength > Integer.SIZE - 1) {
			throw new IllegalArgumentException("The information sequence length more, then " + (Integer.SIZE-1) + " is not supported.");
		}
		
		for (int word = 0; word < (1 << infLength); ++word) {
			BitArray infSeq = new BitArray(infLength);
			for (int i = 0; i < infLength; ++i) {
				infSeq.set(i, (word & (1 << i)) != 0);
			}
			logger.info("inf seq = " + infSeq);
			BitArray codeSeq = code.encodeSeq(infSeq);
			
			assertEquals(codeLength, codeSeq.getFixedSize());
			logger.info("expected code seq = " + codeSeq);
			
			
			BitArray codeBitSet = new BitArray(codeLength);
			int row = 0;
			int bits = 0;
			for (ITrellisIterator iter = trellis.iterator(0, 0); iter.hasForward() && bits < codeSeq.getFixedSize(); ) {
				ITrellisEdge edges[] = iter.getAccessors();
								
				if (edges.length == 2) {
					int index = infSeq.get(row++) ? 1 : 0;
					for (int i = 0; i < edges[index].bits().getFixedSize(); ++i) {
						codeBitSet.set(bits++, edges[index].bits().get(i));
					}
					iter.moveForward(index);
				} else {
					for (int i = 0; i < edges[0].bits().getFixedSize(); ++i) {
						codeBitSet.set(bits++, edges[0].bits().get(i));
					}
					iter.moveForward(0);
				}
			}
			
			logger.info("code seq = " + codeBitSet);
			assertTrue(codeSeq.equals(codeBitSet));
		}
	}

	/**
	 * Метод тестирует решетку <code>trellis</code>, построенную для кода
	 * <code>code</code>. Метод перебирает все информационные последовательности
	 * длиной <code>wordsNumber</code> слов и проверяет корректность построенной
	 * решеткой кодовой последовательноти.
	 * @param code код
	 * @param trellis проверяемая решетка
	 */
	public static void trellisBackwardTraversalShouldGiveCodeWord(Code code, ITrellis trellis) {
		for (int word = 0; word < 1 << code.getK(); ++word) {
			BitArray infWord = new BitArray(code.getK());
			for (int i = 0; i < code.getK(); ++i) {
				infWord.set(i, (word & (1 << i)) != 0);
			}
			logger.info("inf word = " + infWord);
			BitArray codeWord = code.encodeSeq(infWord);
			
			assertEquals(code.getN(), codeWord.getFixedSize());
			logger.info("expected code = " + codeWord);
			
			
			BitArray codeBitSet = new BitArray(code.getN());
			int row = code.getK() - 1;
			int bits = code.getN() - 1;
			for (ITrellisIterator iter = trellis.iterator(trellis.layersCount() - 1, 0); iter.hasBackward() && bits >= 0; ) {
				ITrellisEdge edges[] = iter.getPredecessors();
				
				if (edges.length == 2) {
					int index = infWord.get(row--) ? 1 : 0;
					for (int i = edges[index].bits().getFixedSize() - 1; i >= 0; --i) {
						codeBitSet.set(bits--, edges[index].bits().get(i));
					}
					iter.moveBackward(index);
				} else {
					for (int i = edges[0].bits().getFixedSize() - 1; i >= 0; --i) {
						codeBitSet.set(bits--, edges[0].bits().get(i));
					}
					iter.moveBackward(0);
				}
			}
			
			logger.info("code word = " + codeBitSet);
			assertTrue(codeWord.equals(codeBitSet));
		}
	}
}
