package search_tools;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

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
	private HammingBallEnumerator columnEnum;
	
	private CEnumerator columnDestribution = null;
	
	public MatrixEnumerator(int rows, int columns) {
		k = rows;
		n = columns;
		
		if (k >= Long.SIZE - 1) {
			throw new IllegalArgumentException("Overflow during shift operation.");
		}
		
		long m = (1L << k);
		columnEnum = new HammingBallEnumerator(m, (int)Math.min(m, n));		
	}
	
	public BigInteger count() {	
		BigInteger cnt = BigInteger.ZERO;
		
		for (int r = 1;r <= columnEnum.getK(); ++r) {
			BigInteger distrCnt = (new CEnumerator(n - 1, r - 1)).count().multiply(new CEnumerator(columnEnum.getN(), r).count());
			
			cnt = cnt.add(distrCnt);
		}
		
		return cnt;
	}
			
	public boolean hasNext() {
		return (columnDestribution == null) || columnDestribution.hasNext() || columnEnum.hasNext();
	}
	
	private long[] redestributeColumns() {
		long[] destribution = new long[n];
		long[] columns = columnEnum.current();
		long[] markers = columnDestribution.next();
		int column = 0;		
		
		for (int i = 0;i < n; ++i) {
			destribution[i] = columns[column];
			if (column < markers.length && i == markers[column]) {
				++column;
				continue;
			}			
		}		
		
		destribution[n - 1] = columns[columns.length - 1];
		return destribution;
	}
	
	public Matrix getNext() {
		if (columnDestribution != null && columnDestribution.hasNext()) {
			return matrixByColumns(redestributeColumns());
		}
		
		columnEnum.next();
		
		int columns = columnEnum.current().length;
		
		columnDestribution = new CEnumerator(n - 1, columns - 1);		
		return matrixByColumns(redestributeColumns());
	}
	
	/*public Matrix getNext(int badColumn) {				
		return matrixByColumns(columnEnum.shift(badColumn));
	}
	
	public Matrix getByIndex(BigInteger index) {
		return matrixByColumns(columnEnum.getByIndex(index));
	} /**/
	
	private static Random rnd = new Random();
	
	public Matrix random() {
		long[] columns = new long[n];
		
		for (int i = 0;i < n; ++i) {
			columns[i] = rnd.nextLong();
		}
		
		// bubble sort
		for (int i = 0;i < n; ++i) {
			for (int j = n - 1;j >= i + 1; --j) {
				if (columns[j] < columns[j - 1]) {
					long tmp = columns[j];
					
					columns[j] = columns[j - 1];
					columns[j - 1] = tmp;
				}
			}
		}
		
		return matrixByColumns(columns);
	}
	
	private Matrix matrixByColumns(long[] columns) {
		Matrix mat = new Matrix(k, n);
		
		for (int i = 0;i < n; ++i) {
			long column = columns[i];
			
			for (int j = 0;j < k; ++j) {
				mat.set(j, i, (column & (1L << j)) != 0);
			}
		}
		
		return mat;
	}/**/
	
}
