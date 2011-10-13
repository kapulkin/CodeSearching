package search_tools;

import math.BitArray;

public class RowsEnumerator {
	private CEnumerator cEnum;
	private int rowsNumber;
	private int rowsLength; 
	
	public RowsEnumerator(int rowsNumber, int rowsLength) {
		this.rowsNumber = rowsNumber;
		this.rowsLength = rowsLength;
		
		if (rowsLength >= Long.SIZE - 1) {
			throw new IllegalArgumentException("Overflow during shift operation.");
		}
		
		cEnum = new CEnumerator(1L << rowsLength, rowsNumber);
	}
	
	public boolean hasNext()
	{
		return cEnum.hasNext();
	}
	
	public BitArray[] getNext()
	{
		long combination[] = cEnum.next();

		BitArray rows[] = new BitArray[rowsNumber];
		for (int i  = 0; i < rowsNumber; ++i) {
			rows[i] = new BitArray(rowsLength);
		}
		
		for(int i = 0; i < rowsNumber; ++i)
		{
			for(int j = 0; j < rowsLength; ++j)
			{
				rows[i].set(j, (combination[i] & (1 << j)) != 0);
			}
		}

		return rows;
	}
}
