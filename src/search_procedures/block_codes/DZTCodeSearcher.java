package search_procedures.block_codes;

import java.util.SortedSet;
import java.util.TreeSet;

import math.BitArray;
import math.ConvCodeSpanForm;
import math.Matrix;
import search_procedures.conv_codes.ICodeEnumerator;
import search_procedures.conv_codes.RowShiftingCodeEnumerator;
import codes.BlockCode;
import codes.ConvCode;
import codes.ZTCode;

/**
 * Ищет double zero-tail коды (DZT-codes) со скоростью k/n на основе уже известных сверточных кодов. 
 * @author Stas
 *
 */
public class DZTCodeSearcher extends BasicBlockCodesSearcher<BlockCode> {
	ICodeEnumerator<ConvCode> ccEnum;
	ConvCode ccCode;
	ZTCode zt1;
	RowShiftingCodeEnumerator rowCcEnum;
	RowShiftingZTCodeSearcher rowZTSearcher;
	int k, n;
	int minDist;
	int scale1, scale2;

	/**
	 * Constructs a searcher for (<code>k</code>, <code>n</code>) DZT code with minimal distance <code>minDist</code>.
	 * It's expected, that <code>ccEnum</code> enumerates convolutional codes with free distance <code>freeDist >= minDist</code>.
	 * @param k
	 * @param n
	 * @param minDist
	 * @param ccEnum convolutional code enumerator
	 */
	DZTCodeSearcher(int k, int n, int minDist, ICodeEnumerator<ConvCode> ccEnum) {
		super(minDist, Integer.MAX_VALUE);
		this.k = k;
		this.n = n;
		this.minDist = minDist;
		this.ccEnum = ccEnum;	

		ccCode = ccEnum.next();
		if (ccCode == null) {
			throw new IllegalArgumentException("Convolutional code enumerator has not even one code.");
		}
		scale1 = Math.min(k/ccCode.getK(), n / ccCode.getN() - ccCode.getDelay());
		zt1 = new ZTCode(ccCode, scale1);
		rowZTSearcher = new RowShiftingZTCodeSearcher(minDist, Integer.MAX_VALUE, ccCode, k, n);
		
		this.candidateEnum = new DZTCandidateEnumerator();
	}
	
	public static int getScale1(ConvCode cc, int k, int n) {
		return Math.min(k/cc.getK(), n / cc.getN() - cc.getDelay());
	}
	
	public static int getScale2(ConvCode cc, int scale1, int k, int n) {
		return k - cc.getK() * scale1;				
	}

	public static int getStart(ConvCode cc) {
		// вычисляем начальное смещение для zt2 кода с тем, чтобы при объединенни кодов получить матрицу в спеновой форме.
		ConvCodeSpanForm spanForm = cc.spanForm();
		SortedSet<Integer> columns = new TreeSet<Integer>();
		for (int i = 0; i < cc.getN(); ++i) {
			columns.add(i);
		}
		for (int i = 0; i < cc.getK(); ++i) {
			columns.remove(spanForm.getHead(i));
		}
		return columns.first();
	}
	
	public static BlockCode makeDZT(BlockCode zt1, BlockCode zt2) {
		if (zt1.getN() != zt2.getN()) {
			throw new IllegalArgumentException("Codes have different code word lengths!");
		}
		int n = zt1.getN();
		int k = zt1.getK() + zt2.getK();
		
		Matrix generator = new Matrix(k, n);

		int i;
		for (i = 0; i < zt1.generator().getRowCount(); ++i) {
			generator.setRow(i, new BitArray(n));
			generator.getRow(i).getBitSet().or(zt1.generator().getRow(i).getBitSet());
		}

		for (int j = 0; i < k; ++i, ++j) {
			generator.setRow(i, new BitArray(n));
			generator.getRow(i).getBitSet().or(zt2.generator().getRow(j).getBitSet());
		}
		
		return new BlockCode(generator, true);
	}

	private class DZTCandidateEnumerator implements ICandidateEnumerator<BlockCode> {
		@Override
		public BlockCode next() {
			ZTCode zt2;
			if ((zt2 = (ZTCode) rowZTSearcher.findNext()) == null) {
				do {
					ConvCode cc = ccEnum.next();
					if (cc == null) {
						return null;
					}
					rowZTSearcher = new RowShiftingZTCodeSearcher(minDist, Integer.MAX_VALUE, cc, k, n);
					zt2 = (ZTCode) rowZTSearcher.findNext();
				} while (zt2 == null);
				scale1 = getScale1(ccCode, k, n);
				zt1 = new ZTCode(ccCode, scale1);
			}
			
			return makeDZT(zt1, zt2);
		}
	}
}
