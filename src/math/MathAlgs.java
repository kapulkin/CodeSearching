package math;

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
	 * Расчет спэновой формы матрицы
	 * @param matr
	 * @return
	 */
	public static SpanForm toSpanForm(Matrix matr)
	{
		int[] spanHeads = new int[matr.getRowCount()];
		int[] spanTails = new int[matr.getRowCount()];
		int[] headUniqueIndices = new int[matr.getColumnCount()];
		int[] tailUniqueIndices = new int[matr.getColumnCount()];
		
		for(int i = 0;i < matr.getColumnCount();i ++)
		{
			headUniqueIndices[i] = -1;
			tailUniqueIndices[i] = -1;
		}
		
		// упорядочение голов
		for(int i = 0;i < matr.getRowCount();i ++)
		{
			spanTails[i] = matr.getRow(i).previousSetBit(matr.getColumnCount() - 1);
			spanHeads[i] = matr.getRow(i).nextSetBit(0);			
						
			while(true)
			{
				int currentHead = spanHeads[i];
				int currentUniqueRow = headUniqueIndices[currentHead];
				
				if(currentUniqueRow != -1)
				{					
					BitSet rowToAdd = matr.getRow(currentUniqueRow);
					
					matr.getRow(i).xor(rowToAdd);
					
					if(spanTails[currentUniqueRow] == spanTails[i])
					{
						spanTails[i] = matr.getRow(i).previousSetBit(matr.getColumnCount() - 1);
					}else if(spanTails[currentUniqueRow] > spanTails[i])
					{
						spanTails[i] = spanTails[currentUniqueRow];
					}
					spanHeads[i] = matr.getRow(i).nextSetBit(0);					
				}else{
					headUniqueIndices[currentHead] = i;
					break;
				}
			}
		}

		// упорядочение хвостов
		for(int i = 0;i < matr.getRowCount();i ++)
		{						
			int indexToVerify = i;
			while(true)
			{
				int currentTail = spanTails[indexToVerify];
				int currentUniqueRow = tailUniqueIndices[currentTail]; 
				if(currentUniqueRow != -1)
				{
					if(spanHeads[currentUniqueRow] > spanHeads[indexToVerify])
					{						
						BitSet smallerRow = matr.getRow(currentUniqueRow);
						
						matr.getRow(indexToVerify).xor(smallerRow);
												
						spanTails[indexToVerify] = matr.getRow(indexToVerify).previousSetBit(matr.getColumnCount()-1);
					}else{
						BitSet smallerRow = matr.getRow(indexToVerify);
						
						tailUniqueIndices[currentTail] = indexToVerify;
						
						matr.getRow(currentUniqueRow).xor(smallerRow);						
						spanTails[currentUniqueRow] = matr.getRow(currentUniqueRow).previousSetBit(matr.getColumnCount()-1);
						
						indexToVerify = currentUniqueRow;						
					}
				}else{
					tailUniqueIndices[currentTail] = indexToVerify;
					break;
				}
			}
		}
		
		return new SpanForm(matr, spanHeads, spanTails);
	}
}
