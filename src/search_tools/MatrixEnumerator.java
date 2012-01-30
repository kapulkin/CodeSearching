package search_tools;

import java.math.BigInteger;
import java.util.ArrayList;

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
	 * Перечислитель столбцов
	 */
	private CEnumerator columnEnum;			
	
	public MatrixEnumerator(int rows, int columns) {
		k = rows;
		n = columns;
		
		if (k >= Long.SIZE - 1) {
			throw new IllegalArgumentException("Overflow during shift operation.");
		}
		
		columnEnum = new CEnumerator((1L<<k) * n, n);		
	}
	
	public BigInteger count() {
		/*BigInteger cnt = new BigInteger("0");
		
		for (int distCol = 1; distCol <= Math.min(n, (1L<<k)); ++distCol) {
			cnt = cnt.add(new CEnumerator((1L<<k), distCol).count()).multiply((new CEnumerator(n - 1, distCol - 1).count()));
		}/**/
		
		return columnEnum.count();
	}
	
	/*public long[] getDistinctColumns() {
		long[] distinctColumns = new long[n];

		for (int i = 0; i < n)
		
		return distinctColumns.toArray(new Long[0]);
	}/**/
	
//	public long[] getCounts() {
	//	return counts;
	//}
	
	public boolean hasNext() {
		return columnEnum.hasNext();//(distinctColumnsCnt < Math.min(1L<<k, n)) || cEnumDistinct.hasNext() || cEnumFill.hasNext();
	}
	
	public Matrix getNext() {
		/*if (!cEnumFill.hasNext()) {
			if (!cEnumDistinct.hasNext()) {
				cEnumDistinct = new CEnumerator((1L<<k), ++distinctColumnsCnt);			
			}
			
			distinctColumns = cEnumDistinct.next();
			cEnumFill = new CEnumerator(n - 1, distinctColumnsCnt - 1);
		}
		
		counts = positionsToCounts(cEnumFill.next());
		
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
		
		return mat;/**/
		
		return matrixByColumns(columnEnum.next());
	}
	
	public Matrix getByIndex(BigInteger index) {
		return matrixByColumns(columnEnum.getByIndex(index));
	}
	
	private Matrix matrixByColumns(long[] columns) {
		Matrix mat = new Matrix(k, n);
		
		for (int i = 0;i < n; ++i) {
			long column = columns[i] / n;
			
			for (int j = 0;j < k; ++j) {
				mat.set(j, i, (column & (1L << j)) != 0);
			}
		}
		
		return mat;
	}
	
	/*private long[] positionsToCounts(long[] positions) {
		long[] counts = new long[distinctColumnsCnt];
		
		if (distinctColumnsCnt == 1) {
			counts[0] = n;
			return counts;
		}
		int remainder = n;
		
		for (int i = 0;i < distinctColumnsCnt - 1;++i) {
			counts[i] = positions[i] - (i > 0 ? positions[i-1] : -1);
			remainder -= counts[i];
		}
		counts[distinctColumnsCnt - 1] = remainder;
		return counts;
	}/**/
}
