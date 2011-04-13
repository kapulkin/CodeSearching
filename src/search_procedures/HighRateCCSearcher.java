package search_procedures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import in_out_interfaces.IOPolyMatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	volatile boolean searchRunning = false;
	ArrayList<ConvCodeListener> listeners = new ArrayList<ConvCodeListener>();
	
	public void addCodeListener(ConvCodeListener listener) {
		listeners.add(listener);
	}
	
	public void removeCodeListener(ConvCodeListener listener) {
		listeners.remove(listener);
	}
	
	/*
	 * Правильный итерфейс поисковика:
	 * начни искать коды с расстоянием больше заданного (удовлетворяющего 
	 * условиям) и с заданными парметрами. Если нашел код - выдай его листенеру.
	 * startSearch(freeDistance, ...)
	 * 
	 * Всегда можно остановить поиск:
	 * stopSearch();
	 * 
	 * Если перебор закончился, то остановиться и кинуть листенеру информацию 
	 * об этом. 
	 */
	public void startSearch(int delay, int freeDistance, int infoBits) throws IOException
	{
		searchRunning = true;
		
		Logger logger = LoggerFactory.getLogger(this.getClass());
		
		MatrixEnumerator enumerator = new MatrixEnumerator(delay - 1, infoBits + 1);
		
		while (searchRunning && enumerator.hasNext())
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
			
			if(decomp.getD().get(0, 0).isZero()) {
				continue;
			}
					
			Trellis trellis = Trellises.trellisFromParityCheckHR(checkMatrix);
			
			MinDistance.computeDistanceMetrics(trellis);
			
			int minDist = MinDistance.findFreeDistWithBEAST(trellis, 0, 2 * (delay+1));
						
			if(minDist >= freeDistance)
			{
				logger.debug("free dist: " + minDist);
				if (logger.isDebugEnabled()) {
					StringWriter writer = new StringWriter();
					IOPolyMatrix.writeMatrix(checkMatrix, new BufferedWriter(writer));
					logger.debug("\n" + writer.getBuffer().substring(0));
				}

				PolyMatrix genMatrix = MathAlgs.findOrthogonalMatrix(decomp);
				ConvCode code = new ConvCode(new BlockMatrix(genMatrix, delay + 1), true);
				
				for (ConvCodeListener listener : listeners) {
					listener.codeFound(code);
				}
			}
		}
		
		for (ConvCodeListener listener : listeners) {
			listener.searchFinished();
		}
	}	
	
	public void stopSearch() {
		searchRunning = false;
	}

}
