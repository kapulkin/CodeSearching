package search_procedures;

import math.Matrix;
import codes.ConvCode;

public class HighRateCCSearcher {

	public ConvCode search(int stateComplexity, int freeDistance, int infoBits)
	{
		MatrixEnumerator enumerator = new MatrixEnumerator(stateComplexity - 2, infoBits + 1);
		
		while(enumerator.hasNext())
		{
			Matrix candidate = enumerator.getNext();
			
		}
		
		return null;
	}
}
