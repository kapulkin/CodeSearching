package search_tools;

import math.Matrix;

public class MatrixEnumerator {

	/**
	 * Количество строк
	 */
	private int k;
	
	/**
	 * Количество столбцов
	 */
	private int n;
	
	/**
	 * Перечислитель различных столбцов
	 */
	private CEnumerator cEnumDistinct;
	
	/**
	 * Перечислитель размещений столбцов
	 */
	private CEnumerator cEnumFill;
	
	/** 
	 * Количество различных столбцов
	 * */
	private int distinctColumnsCnt = 1;
	
	/** 
	 * Различные столбцы
	 * */
	private long[] distinctColumns;
	
	public MatrixEnumerator(int rows, int columns) {
		k = rows;
		n = columns;
		
		if (k >= Long.SIZE - 1) {
			throw new IllegalArgumentException("Overflow during shift operation.");
		}
		
		cEnumDistinct = new CEnumerator((1L<<k), distinctColumnsCnt);
		cEnumFill = new CEnumerator(n + distinctColumnsCnt - 1, distinctColumnsCnt - 1);
		
		distinctColumns = cEnumDistinct.getNext();
	}
	
	public boolean hasNext() {
		return (distinctColumnsCnt < Math.min(1L<<k, n)) || cEnumDistinct.hasNext();
	}
	
	public Matrix getNext() {
		if(!cEnumFill.hasNext()) {
			if(!cEnumDistinct.hasNext()) {
				cEnumDistinct = new CEnumerator((1L<<k), ++distinctColumnsCnt);			
			}
			
			distinctColumns= cEnumDistinct.getNext();
			cEnumFill = new CEnumerator(n - 1, distinctColumnsCnt - 1);
		}
		
		long[] counts = positionsToCounts(cEnumFill.getNext());
		int columnsFilled = 0;
		Matrix mat = new Matrix(k, n);
	    		
		for (int l = 0;l < distinctColumnsCnt; ++l) {
			for (int c = 0;c < counts[l]; ++c) {
				for (int i = 0;i < k; ++i) {
					mat.set(i, c + columnsFilled, (distinctColumns[l] & (1L << i)) != 0);
				}
			}
			columnsFilled += counts[l];
		}
		
		return mat;
	}
	
	private long[] positionsToCounts(long[] positions) {
		long[] counts = new long[distinctColumnsCnt];
		
		if (distinctColumnsCnt == 1) {
			counts[0] = n;
			return counts;
		}
		int remainder = n;
		
		for(int i = 0;i < distinctColumnsCnt - 1;++i) {
			counts[i] = positions[i] - (i > 0 ? positions[i-1] : 0) + 1;
			remainder -= counts[i];
		}
		counts[distinctColumnsCnt - 1] = remainder;
		return counts;
	}
}
