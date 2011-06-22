package math;

import java.util.ArrayList;



public class MathAlgs {
	
	/**
	 * Ищет матрицу, порождающую ортогональное дополнение к пространству, порожденному матрицей mat.
	 * В ходе работы алгоритма необходимы преобразования матрицы mat. В зависимости от значения флага 
	 * allowModifications эти преобразования будут выполняться над mat или над ее копией. 
	 * @param mat матрица с линейно независимыми строками
	 * @param allowModifications разрешить модифицировать mat
	 * @return ортогональная матрица
	 */
	public static Matrix findOrthogonalMatrix(Matrix mat, boolean allowModifications)
	{
		Matrix workingMat;
		Matrix ortMat = new Matrix(mat.getColumnCount() - mat.getRowCount(), mat.getColumnCount());
		//Позиции единичек в строчках перестановочной подматрицы 
		int[] permutationRows = new int[mat.getRowCount()];
		//Позиции единичек в столбцах перестановочной подматрицы и -1 в остальных столбцах  
		int[] permutationColumns = new int[mat.getColumnCount()];
		
		for(int i = 0;i < mat.getColumnCount();i ++)
		{
			permutationColumns[i] = -1;
		}
		
		if(allowModifications)
		{
			workingMat = mat;
		}else{
			workingMat = (Matrix)mat.clone();
		}
		
		for(int i = 0;i < workingMat.getRowCount();i ++)
		{
			int uniqueInd = workingMat.getRow(i).nextSetBit(0);
			for(int j = 0;j < workingMat.getRowCount();j ++)
			{
				if(i == j) continue;
				
				if(workingMat.get(j, uniqueInd) == true)
				{
					workingMat.getRow(j).xor(workingMat.getRow(i));
				}
			}
			permutationColumns[uniqueInd] = i; 			
		}		
		
		int permColumnsViwed = 0;
		
		for(int i = 0;i < workingMat.getColumnCount();i ++)
		{
			if(permutationColumns[i] != -1)
			{
				permutationRows[permutationColumns[i]] = permColumnsViwed;
				permColumnsViwed ++;
			}
		}
		
		permColumnsViwed = 0;
		for(int i = 0;i < ortMat.getColumnCount();i ++)
		{
			if(permutationColumns[i] == -1)
			{
				// заполнение строки единичной подматрицы
				for(int j = 0;j < ortMat.getRowCount();j ++)
				{
					// если диагональный элемент
					if(j == i - permColumnsViwed)
					{
						ortMat.set(j, i, true);
					}else{
						ortMat.set(j, i, false);
					}
				}
			}else{
				int j = 0;
				// Расчет произведений permColumnsViwed-й строки транспонированной перестановочной матрицы
				// на столбцы неперестановочной подматрицы исходной матрицы. Эти произведения по сути 
				// представляют собой строчку исходной матрицы с номером permutationRows[permColumnsViwed]. 
				for(int k = 0;k < workingMat.getColumnCount();k ++)
				{
					if(permutationColumns[k] == -1)
					{
						ortMat.set(j, i, workingMat.get(permutationRows[permColumnsViwed], k));
						j++;
					}
				}
				permColumnsViwed ++;
			}
		}		
		
		return ortMat;
	}		
	
	/**
	 * Ищет линейнозависимые строки в матрице "методом Гаусса": 
	 * последовательно вычитает (с учетом бинарности логики прибавляет) первую 
	 * строчку, содержащую единицу в данном столбце из всех остальных.
	 * 
	 * Вычисления ведутся над входной матрицей.
	 * 
	 * Если в результате определенная строчка оказывается нулевой, возвращается 
	 * массив, содержащий индексы строчек, вычитавшихся из данной и ее 
	 * собственный индекс. Эти индексы и являются индексами линейнозависимых 
	 * строчек. 
	 * 
	 * Если в матрице нет ЛЗ строчек, возвращается пустой массив.
	 * 
	 * @param matr - матрица, ЛЗ строчки в которой ищутся.
	 * @return массив индексов ЛЗ строчек в матрице.
	 */
	public static int[] findDependentRows(Matrix matr)
	{
		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0;i < matr.getRowCount();i ++)
		{
			combinations.add(new ArrayList<Integer>());
		}
		
		for(int i = 0;i < matr.getRowCount();i ++)
		{
			int bitPos = matr.getRow(i).nextSetBit(0);
			
			if(bitPos == -1)
			{
				int[] dependentRows = new int[combinations.get(i).size()+1];
				
				for(int j = 0;j < combinations.get(i).size();j ++)
				{
					dependentRows[j] = combinations.get(i).get(j);
				}
				
				dependentRows[dependentRows.length - 1] = i;
				return dependentRows;
			}
			
			for(int j = i + 1;j < matr.getRowCount();j ++)
			{
				if(matr.get(j, bitPos) == true)
				{
					matr.getRow(j).xor(matr.getRow(i));
					combinations.get(j).add(i);
				}
			}
		}
		
		return new int[0];
	}
	
}
