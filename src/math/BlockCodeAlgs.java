package math;

import trellises.BlockCodeTrellis;
import trellises.Trellis;
import trellises.Trellises;
import codes.BlockCode;

public class BlockCodeAlgs {

	/**
	 * Расчет спэновой формы матрицы. Вычисления ведутся над входной матрицей.
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
					BitArray rowToAdd = matr.getRow(currentUniqueRow);
					
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
						BitArray smallerRow = matr.getRow(currentUniqueRow);
						
						matr.getRow(indexToVerify).xor(smallerRow);
												
						spanTails[indexToVerify] = matr.getRow(indexToVerify).previousSetBit(matr.getColumnCount()-1);
					}else{
						BitArray smallerRow = matr.getRow(indexToVerify);
						
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

	public static void sortHeads(SpanForm sf)
	{
		// сортировка выбором
		for(int i = 0;i < sf.getRowCount();i ++)
		{
			int minHead = Integer.MAX_VALUE;
			int minRow = -1;
			
			for(int j = i;j < sf.getRowCount();j ++)
			{
				if(sf.getHead(j) < minHead)
				{
					minHead = sf.getHead(j);
					minRow = j;
				}
			}
			
			if(i != minRow)
			{
				BitArray iRow = sf.Matr.getRow(i);
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.Matr.setRow(i, sf.Matr.getRow(minRow));
				sf.Matr.setRow(minRow, iRow);
				
				sf.setHead(i, sf.getHead(minRow));
				sf.setHead(minRow, iHead);
				
				sf.setTail(i, sf.getTail(minRow));
				sf.setTail(minRow, iTail);
			}
		}
	}

	public static void sortTails(SpanForm sf)
	{
		// сортировка выбором
		for(int i = 0;i < sf.getRowCount();i ++)
		{
			int minTail = Integer.MAX_VALUE;
			int minRow = -1;
			
			for(int j = i;j < sf.getRowCount();j ++)
			{
				if(sf.getTail(j) < minTail)
				{
					minTail = sf.getTail(j);
					minRow = j;
				}
			}
			
			if(i != minRow)
			{
				BitArray iRow = sf.Matr.getRow(i);
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.Matr.setRow(i, sf.Matr.getRow(minRow));
				sf.Matr.setRow(minRow, iRow);
				
				sf.setHead(i, sf.getHead(minRow));
				sf.setHead(minRow, iHead);
				
				sf.setTail(i, sf.getTail(minRow));
				sf.setTail(minRow, iTail);
			}
		}
	}

	public static Trellis buildTrellis(BlockCode code) {
		return Trellises.buildExplicitTrellis(new BlockCodeTrellis(code));
	}
}
