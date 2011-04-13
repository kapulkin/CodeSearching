package trellises.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.Common;

import trellises.BlockCodeTrellis;
import trellises.TrellisIterator;
import trellises.Trellises;
import trellises.Trellis.Edge;
import math.BitSet;
import math.Matrix;
import codes.BlockCode;


public class BlockCodeTrellisTest {
	BlockCode code;
	BlockCodeTrellis trellis;

	Logger logger;
	
	public BlockCodeTrellisTest () {
		logger = LoggerFactory.getLogger(this.getClass());
		
		// Генератор задан в минимальной спеновой форме. Секционированная решетка должна иметь 6 ярусов. 
		BitSet row0 = new BitSet(6); row0.set(0); row0.set(1); row0.set(3);
		BitSet row1 = new BitSet(6); row1.set(1); row1.set(4); row1.set(5);
		BitSet row2 = new BitSet(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitSet[] { row0, row1, row2 });

		code = new BlockCode(generator, true);
	}
	
	@Test
	public void layersCountOfSectionalizedTrellisRestriction() {
		trellis = new BlockCodeTrellis(code);

		assertTrue(trellis.layersCount() <= 2 * code.getK() + 1);
		// для данного кода ярусов должно быть 6
		assertEquals(trellis.layersCount(), 6);
	}
	
	@Test
	public void trellisProfileTest() {
		trellis = new BlockCodeTrellis(code);

		int trellisProfile[] = {0, 1, 2, 2, 1, 0};
		
		for (int i = 0; i < trellis.layersCount(); ++i) {
			assertEquals(trellisProfile[i], trellis.layerComplexity(i));
		}
	}
	
	@Test
	public void trellisForwardTraversalShouldGiveCodeWord() {
		Trellises.sortHeads(code.getGeneratorSpanForm());
		trellis = new BlockCodeTrellis(code);

		for (int word = 0; word < 1 << code.getK(); ++word) {
			BitSet wordBitSet = new BitSet(code.getK());
			for (int i = 0; i < code.getK(); ++i) {
				wordBitSet.set(i, (word & (1 << i)) != 0);
			}
			logger.info("word = " + Common.bitsToString(wordBitSet, code.getK()));
			Matrix wordMatrix = new Matrix(new BitSet[] {wordBitSet});
			Matrix codeMatrix = wordMatrix.mul(code.generator());
			
			assertEquals(code.getN(), codeMatrix.getColumnCount());
			logger.info("expected code = " + Common.bitsToString(codeMatrix.getRow(0), code.getN()));
			
			
			BitSet codeBitSet = new BitSet(code.getN());
			int row = 0;
			int bits = 0;
			for (TrellisIterator iter = trellis.iterator(0, 0); iter.hasForward(); ) {
				Edge edges[] = iter.getAccessors();
				if (edges.length == 2) {
					int index = wordBitSet.get(row++) ? 1 : 0;
					for (int i = 0; i < edges[index].Bits.getFixedSize(); ++i) {
						codeBitSet.set(bits++, edges[index].Bits.get(i));
					}
					iter.moveForward(index);
				} else {
					for (int i = 0; i < edges[0].Bits.getFixedSize(); ++i) {
						codeBitSet.set(bits++, edges[0].Bits.get(i));
					}
					iter.moveForward(0);
				}
			}
			
			logger.info("code = " + Common.bitsToString(codeBitSet, bits));
			assertTrue(wordMatrix.equals(new Matrix(new BitSet[] {wordBitSet})));
		}
	}

	@Test
	public void trellisBackwardTraversalShouldGiveCodeWord() {
		Trellises.sortTails(code.getGeneratorSpanForm());
		trellis = new BlockCodeTrellis(code);

		for (int word = 0; word < 1 << code.getK(); ++word) {
			BitSet wordBitSet = new BitSet(code.getK());
			for (int i = 0; i < code.getK(); ++i) {
				wordBitSet.set(i, (word & (1 << i)) != 0);
			}
			logger.info("word = " + Common.bitsToString(wordBitSet, code.getK()));
			Matrix wordMatrix = new Matrix(new BitSet[] {wordBitSet});
			Matrix codeMatrix = wordMatrix.mul(code.generator());
			
			assertEquals(code.getN(), codeMatrix.getColumnCount());
			logger.info("expected code = " + Common.bitsToString(codeMatrix.getRow(0), code.getN()));
			
			
			BitSet codeBitSet = new BitSet(code.getN());
			int row = code.getK() - 1;
			int bits = code.getN() - 1;
			for (TrellisIterator iter = trellis.iterator(trellis.layersCount() - 1, 0); iter.hasBackward(); ) {
				Edge edges[] = iter.getPredecessors();
				if (edges.length == 2) {
					int index = wordBitSet.get(row--) ? 1 : 0;
					for (int i = edges[index].Bits.getFixedSize() - 1; i >= 0; --i) {
						codeBitSet.set(bits--, edges[index].Bits.get(i));
					}
					iter.moveBackward(index);
				} else {
					for (int i = edges[0].Bits.getFixedSize() - 1; i >= 0; --i) {
						codeBitSet.set(bits--, edges[0].Bits.get(i));
					}
					iter.moveBackward(0);
				}
			}
			
			logger.info("code = " + Common.bitsToString(codeBitSet, code.getN()));
			assertTrue(wordMatrix.equals(new Matrix(new BitSet[] {wordBitSet})));
		}
	}
}
