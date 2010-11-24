package search_procedures;

import trellises.BeastAlgorithm;
import trellises.Trellis;
import trellises.Trellises;
import math.BlockMatrix;
import math.MathAlgs;
import math.Matrix;
import math.Poly;
import math.PolyMatrix;
import math.SmithDecomposition;
import codes.ConvCode;
import codes.MinDistance;

public class HighRateCCSearcher {

	public ConvCode search(int delay, int freeDistance, int infoBits)
	{
		MatrixEnumerator enumerator = new MatrixEnumerator(delay - 1, infoBits + 1);
		
		while(enumerator.hasNext())
		{			
			Matrix candidate = enumerator.getNext();
			PolyMatrix checkMatrix = new PolyMatrix(1, infoBits + 1);
			
			for(int i = 0;i < infoBits + 1;i ++)
			{
				Boolean[] polyCoeffs = new Boolean[delay+1];
				
				polyCoeffs[0] = true;
				for(int j = 1;j < delay;j ++)
				{
					polyCoeffs[j] = candidate.get(j-1, i);
				}
				
				if(i >= infoBits - 1)
				{
					polyCoeffs[delay] = true;
				}else{
					polyCoeffs[delay] = false;
				}
				
				checkMatrix.set(0, i, new Poly(polyCoeffs));
			}
			
			SmithDecomposition decomp = new SmithDecomposition(checkMatrix);
			
			if(decomp.getD().get(0, 0).isZero())
			{
				continue;
			}
					
			Trellis trellis = Trellises.trellisFromParityCheckHR(checkMatrix);
			
			MinDistance.computeDistanceMetrics(trellis);
			
			int minDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (delay+1));			
						
			if(minDist >= freeDistance)
			{
				PolyMatrix genMatrix = MathAlgs.findOrthogonalMatrix(decomp);
				ConvCode code = new ConvCode(new BlockMatrix(genMatrix, delay + 1), true);
				
				return code;
			}
		}
		
		return null;
	}	

}
