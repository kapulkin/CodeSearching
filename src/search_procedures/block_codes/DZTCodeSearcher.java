package search_procedures.block_codes;

import java.util.SortedSet;
import java.util.TreeSet;

import math.BitArray;
import math.ConvCodeSpanForm;
import math.Matrix;
import math.MinDistance;
import search_procedures.conv_codes.IConvCodeEnumerator;
import search_procedures.conv_codes.RowShiftingCodeEnumerator;
import codes.BlockCode;
import codes.ConvCode;
import codes.ZTCode;

/**
 * Ищет double zer-tail коды (DZT-codes) со скроростью k/n на основе уже известных сверточных кодов. 
 * @author Stas
 *
 */
public class DZTCodeSearcher {
	RowShiftingCodeEnumerator rowCcEnum;
	IConvCodeEnumerator ccEnum;
	int k, n;
	int minDist;
	int scale1, scale2;
	
	private static class CandidatePair {
		public CandidatePair(ZTCode zt1, ZTCode zt2) {
			this.zt1 = zt1;
			this.zt2 = zt2;
		}
		
		ZTCode zt1, zt2;
	}
	
	DZTCodeSearcher(int k, int n, int minDist, IConvCodeEnumerator ccEnum) {
		this.k = k;
		this.n = n;
		this.minDist = minDist;
		this.ccEnum = ccEnum;
	}

	public BlockCode next() {
		CandidatePair candidate;
		
		while ((candidate = getCandidate()) != null) {
			// проверить мин. расстояние zt2
			if (MinDistance.findMinDist(candidate.zt2) < minDist) {
				continue;
			}
			
			// проверить мин. расстояние DZT(zt1, zt2)
			BlockCode dzt = makeDZT(candidate.zt1, candidate.zt2);
			if (MinDistance.findMinDist(dzt) < minDist) {
				continue;
			}
			
			return dzt;
		}
		
		return null;
	}

	private CandidatePair getCandidate() {
		ConvCode rowCc;
		ConvCode cc = null;
		if (rowCcEnum == null || (rowCc = rowCcEnum.next()) == null) {
			if ((cc = ccEnum.next()) == null) {
				return null;
			}
			scale1 = Math.min(k/cc.getK(), n / cc.getN() - cc.getDelay());

			// вычисляем смещение для zt2 кода с тем, чтобы при объединенни кодов получить матрицу в спеновой форме.
			ConvCodeSpanForm spanForm = cc.spanForm();
			SortedSet<Integer> columns = new TreeSet<Integer>();
			for (int i = 0; i < cc.getN(); ++i) {
				columns.add(i);
			}
			for (int i = 0; i < cc.getK(); ++i) {
				columns.remove(spanForm.getHead(i));
			}
			int start = columns.first();
			
			scale2 = k - cc.getK() * scale1;				
			rowCcEnum = new RowShiftingCodeEnumerator(minDist, n - cc.getN() * (scale2 - 1), cc.getN(), start);
			rowCc = rowCcEnum.next();
		}
		
		ZTCode zt1 = new ZTCode(cc, scale1);
		ZTCode zt2 = new ZTCode(rowCc, scale2);

		return new CandidatePair(zt1, zt2);
	}

	BlockCode makeDZT(ZTCode zt1, ZTCode zt2) {
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
}
