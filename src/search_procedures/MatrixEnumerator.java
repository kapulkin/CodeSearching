package search_procedures;

import math.Matrix;

public class MatrixEnumerator implements Enumerator<Matrix> {

	/**
	 * Количество строк
	 */
	private int k;
	
	/**
	 * Количество столбцов
	 */
	private int n;
	
	/**
	 * Перечислитель сочетаний
	 */
	private CEnumerator cEnum;
	
	public MatrixEnumerator(int rows, int columns)
	{
		k = rows;
		n = columns;
		
		if (k >= Long.SIZE - 1) {
			throw new IllegalArgumentException("Overflow during shift operation.");
		}
		cEnum = new CEnumerator((1L<<k), n);		
	}
	
	public boolean hasNext()
	{
		return cEnum.hasNext();
	}
	
	public Matrix next()
	{
		long[] seq = cEnum.next();
		Matrix mat = new Matrix(k, n);
	    		
		for (int i = 0; i < n; ++i)
		{
			for (int j = 0; j < k; ++j)
			{
				mat.set(j, i, (seq[i] & (1 << j)) != 0);
			}
		}
		
		return mat;
	}
}
