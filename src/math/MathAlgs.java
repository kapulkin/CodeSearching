package math;

import in_out_interfaces.IOMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MathAlgs {
	
	public static Integer[] getPermutationSubmatrix(Matrix mat) {
		HashMap<Integer, Integer> submatrix = new HashMap<Integer, Integer>();		
		
		for (int i = 0;i < mat.getColumnCount(); ++i) {
			int firstSetBit = 0;
			int secondSetBit = mat.getRowCount() - 1;
			
			for (int j = 0;j < mat.getRowCount(); ++j) {
				if (mat.get(j, i)) {
					firstSetBit = j;
					break;
				}
			}
			
			for (int j = mat.getRowCount() - 1;j >= 0; --j) {
				if (mat.get(j, i)) {
					secondSetBit = j;
					break;
				}
			}
			
			if (firstSetBit == secondSetBit) {
				submatrix.put(firstSetBit, i);
			}
		}
		
		return submatrix.values().toArray(new Integer[0]);
	}
	
	public static HashMap<Integer, Integer> extractPermutationSubmatrix(Matrix mat) {				
		HashMap<Integer, Integer> submatrix = new HashMap<Integer, Integer>();		
		
		for(int i = 0;i < mat.getRowCount();i ++) {
			int uniqueInd = mat.getRow(i).nextSetBit(0);
			
			for(int j = 0;j < mat.getRowCount();j ++) {
				if(i == j) continue;
				
				if(mat.get(j, uniqueInd) == true) {
					mat.getRow(j).xor(mat.getRow(i));
				}
			}
						
			submatrix.put(uniqueInd, i);
		}		
		
		return submatrix;	
	}
	
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
		int[] invPermutationRows = new int[mat.getRowCount()];
		
		if (allowModifications) {
			workingMat = mat;
		} else {
			workingMat = (Matrix)mat.clone();
		}
		
		HashMap<Integer, Integer> permSubmatrix = extractPermutationSubmatrix(workingMat);
		
		int permColumnsViwed = 0;	
		
		for(int i = 0;i < workingMat.getColumnCount();i ++) {
			if(permSubmatrix.containsKey(i)) {
				permutationRows[permSubmatrix.get(i)] = permColumnsViwed;
				invPermutationRows[permColumnsViwed] = permSubmatrix.get(i);
				permColumnsViwed ++;
			}
		}
		
		permColumnsViwed = 0;
		for (int i = 0;i < ortMat.getColumnCount();i ++) {
			if (!permSubmatrix.containsKey(i)) {			
				// заполнение строки единичной подматрицы
				for (int j = 0;j < ortMat.getRowCount();j ++) {
					// если диагональный элемент
					if (j == i - permColumnsViwed) {
						ortMat.set(j, i, true);
					} else {
						ortMat.set(j, i, false);
					}
				}				
			} else {
				int j = 0;
				// Расчет произведений permColumnsViwed-й строки транспонированной перестановочной матрицы
				// на столбцы неперестановочной подматрицы исходной матрицы. Эти произведения по сути 
				// представляют собой строчку исходной матрицы с номером permutationRows[permColumnsViwed]. 
				for (int k = 0;k < workingMat.getColumnCount();k ++) {
					if (!permSubmatrix.containsKey(k)) {
						ortMat.set(j, i, workingMat.get(invPermutationRows[permColumnsViwed], k));
						++ j;
					}
				}
				++ permColumnsViwed;
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
					
					for (int k : combinations.get(i)) {
						if (combinations.get(j).contains(k)) {
							combinations.get(j).remove(k);
						} else {
							combinations.get(j).add(k);
						}
					}
				}
			}
		}
		
		return new int[0];
	}
		
}
