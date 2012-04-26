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
		return columnEnum.count();
	}
	
		
	public boolean hasNext() {
		return columnEnum.hasNext();
	}
	
	public Matrix getNext() {
		return matrixByColumns(columnEnum.next());
	}
	
	public Matrix getNext(int badColumn) {				
		return matrixByColumns(columnEnum.shift(badColumn));
	}
	
	public Matrix getByIndex(BigInteger index) {
		return matrixByColumns(columnEnum.getByIndex(index));
	}
	
	public Matrix random() {
		return matrixByColumns(columnEnum.random());
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
	
}
