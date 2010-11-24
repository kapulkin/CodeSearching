package search_procedures;

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
	 * Перечислитель сочетаний
	 */
	private CEnumerator cEnum;
	
	public MatrixEnumerator(int rows, int columns)
	{
		k = rows;
		n = columns;
		cEnum = new CEnumerator((1<<k), n);		
	}
	
	public boolean hasNext()
	{
		return cEnum.hasNext();
	}
	
	public Matrix getNext()
	{
		int[] seq = cEnum.getNext();
		Matrix mat = new Matrix(k, n);
	    
		mat = new Matrix(k, n);
		
		for(int i = 0;i < n;i ++)
		{
			for(int j = 0;j < k;j ++)
			{
				mat.set(j, i, (seq[i] & (1 << j)) != 0);
			}
		}
		
		return mat;
	}
}
