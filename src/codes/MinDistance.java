package codes;

import math.BitArray;
import math.GrayCode;
import math.Matrix;
import trellises.BeastAlgorithm;
import trellises.ITrellisIterator;
import trellises.Trellis;
import trellises.ViterbiAlgorithm;

public class MinDistance {

	public static void computeDistanceMetrics(Trellis trellis)
	{
		for(int i = 0; i < trellis.Layers.length; i++)
		{
			for(int j = 0; j < trellis.Layers[i].length; j++)
			{
				for(int k = 0; k < trellis.Layers[i][j].Accessors.length; k++)
				{
					double[] oldMetrics = trellis.Layers[i][j].Accessors[k].Metrics;
					
					trellis.Layers[i][j].Accessors[k].Metrics = new double[oldMetrics.length + 1];
					trellis.Layers[i][j].Accessors[k].Metrics[0] = trellis.Layers[i][j].Accessors[k].Bits.cardinality(); 
					for(int m = 1; m < oldMetrics.length + 1; m++)
					{
						trellis.Layers[i][j].Accessors[k].Metrics[m] = oldMetrics[m - 1];
					}
				}
			}			
		}
	}
	
	public static int findMinDist(Trellis trellis, int distanceMetric, int cycles)
	{
		int minDist = Integer.MAX_VALUE;		
		
		for(int v = 0;v < trellis.Layers[0].length;v ++)
		{
			ViterbiAlgorithm.Vertex[] lastLayer;
			
			if(cycles == 0)
			{
				lastLayer = ViterbiAlgorithm.findOptimalPaths(trellis, trellis.Layers.length - 1, distanceMetric, 2, v);
			}else{
				lastLayer = ViterbiAlgorithm.findOptimalPaths(trellis, trellis.Layers.length * cycles, distanceMetric, 2, v);
			}
			
			int pathWeight = lastLayer[v].Metrics[0] == 0.0 ? (int)lastLayer[v].Metrics[1] : (int)lastLayer[v].Metrics[0];
			
			if(pathWeight < minDist)
			{
				minDist = pathWeight;
			}
		}
		
		return minDist;
	}

	public static int findMinDistWithBEAST(Trellis trellis, int distanceMetric, int cycles) {
		int minDist = Integer.MAX_VALUE;
				
		int length = Math.max(1, cycles) * trellis.Layers.length;
		double weights[] = new double[length];
		for (int i = 0; i < length; ++i) {
			weights[i] = 1;
		}
		
		ITrellisIterator root = trellis.iterator(0, 0);
		ITrellisIterator toor = trellis.iterator(trellis.Layers.length - 1, 0);
		BeastAlgorithm.Path paths[] = BeastAlgorithm.findOptimalPaths(root, toor, distanceMetric, weights);
		
		for (int i = 0; i < paths.length; ++i) {
			minDist = Math.min(minDist, (int)paths[i].weight());
		}
		
		return minDist;
	}
	
	public static int findMinDist(Matrix gen)
	{
		int minDist = Integer.MAX_VALUE;
		BitArray codeWord = new BitArray(gen.getColumnCount());

		for(int w = 1;w < (1<<(gen.getRowCount()));w ++)
		{			
			int changedBit = GrayCode.getChangedPosition(w);
			int weight;
			
			codeWord.xor(gen.getRow(changedBit));
			weight = codeWord.cardinality();
			if(weight < minDist && weight != 0)
			{
				minDist = weight;
			}
		}
		
		return minDist;
	}
		
}
