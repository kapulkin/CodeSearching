package trellises;


/**
 * 
 * @author fedor
 *
 */
public class ViterbiAlgorithm {
	
	/**
	 * Информация о путях до вершины
	 * @author fedor
	 *
	 */
	public class Vertex{
		/**
		 * Лучшие пути до вершины
		 */
		public int[][] Paths;
		/**
		 * Метрики лучших путей до вершины
		 */
		public double[] Metrics;
		/**
		 * Индекс вершины в слое
		 */
		public int Index;
	}
	
	/**
	 * Расчитывает лучшие пути до целевой вершины dstVertex по лучшим путям до предшественников.
	 * Алгоритм - слияние отсортированных массивов. 
	 * @param srcVertices предшественники целевой вершины 
	 * @param edges ребра, соединяющие целевую вершину с предшественниками
	 * @param metric индекс метрики на ребрах
	 * @param maxPaths количество лучших путей
	 * @param dstVertex целевая вершина
	 */ 
	private static void computePaths(Vertex[] srcVertices, Trellis.Edge[] edges, int metric, int maxPaths, Vertex dstVertex)
	{	
		// Минимум из суммарного количества путей до предшественников и maxPaths 
		int pathsCnt = 0;
		// Длина путей
		int pathsLength = srcVertices[0].Paths[0].length;
		// Текущие индексы путей в сливаемых массивах
		int[] pathIds = new int[edges.length];
		
		for(int i = 0; i < edges.length; i++)
		{
			pathsCnt += srcVertices[i].Paths.length;
		}
		
		pathsCnt = Math.min(maxPaths, pathsCnt);
		
		dstVertex.Paths = new int[pathsCnt][pathsLength+1];
		dstVertex.Metrics = new double[pathsCnt];
		for(int i = 0;i < pathsCnt;i ++)
		{
			int bestSrc = 0;
			double bestMetric = Double.POSITIVE_INFINITY;
			
			for(int j = 0;j < edges.length;j ++)
			{						
				if(pathIds[j] >= srcVertices[j].Paths.length)
				{
					continue;
				}
				
				if(srcVertices[j].Metrics[pathIds[j]] + edges[j].Metrics[metric] < bestMetric)
				{
					bestSrc = j;
					bestMetric = srcVertices[j].Metrics[pathIds[j]] + edges[j].Metrics[metric];
				}
			}
			
			// Ставим на i-ю позицию pathIds[bestPredEdge]-й по счету путь предшественника с индексом bestPredEdge
			for(int j = 0;j < pathsLength;j ++)
			{				
				int pathId = pathIds[bestSrc];
				dstVertex.Paths[i][j] = srcVertices[bestSrc].Paths[pathId][j]; 
			}
			
			dstVertex.Paths[i][pathsLength] = dstVertex.Index;
			dstVertex.Metrics[i] = bestMetric;
			
			pathIds[bestSrc] ++;
		}
	}
	
	/**
	 * Ищет optimalPathsCnt лучших путей до слоя с индексом steps (mod кол-во слоев) в решетке trellis  
	 * 
	 * @param trellis решетка
	 * @param steps количество просматриваемых слоев
	 * @param metric индекс метрики на ребрах
	 * @param optimalPathsCnt количество расчитываемых лучших путей
	 * @param initialVertex индекс стартовой вершины в нулевом слое
	 * Если -1, то на первом шаге участвуют все вершины в нулевом слое 
	 * @return информации о путях до вершин последнего слоя 
	 */
	public static Vertex[] findOptimalPaths(Trellis trellis, int steps, int metric, int optimalPathsCnt, int initialVertex)
	{		
		Vertex[] lastLayer = new Vertex[trellis.Layers[0].length];   
		
		for(int v = 0;v < lastLayer.length;v ++)
		{			
			lastLayer[v] = new ViterbiAlgorithm().new Vertex();
			lastLayer[v].Paths = new int[1][1];
			lastLayer[v].Metrics = new double[1];
			lastLayer[v].Index = v;			
			lastLayer[v].Paths[0][0] = v;
			if(v == initialVertex || initialVertex == -1)
			{
				lastLayer[v].Metrics[0] = 0.0;
			}else{
				lastLayer[v].Metrics[0] = Double.MAX_VALUE / 100;
			}
		}
		
		for(int s = 1;s < steps+1;s ++)
		{
			int layerInd = s % trellis.Layers.length;
			Vertex[] currentLayer = new Vertex[trellis.Layers[layerInd].length];
			
			for(int v = 0;v < currentLayer.length;v ++)
			{
				currentLayer[v] = new ViterbiAlgorithm().new Vertex();
				currentLayer[v].Index = v;
				
				Trellis.Edge[] predEdges = trellis.Layers[layerInd][v].Predecessors;
				Vertex[] predVertices = new Vertex[predEdges.length];
				
				for(int e = 0;e < predEdges.length;e ++)
				{
					predVertices[e] = lastLayer[predEdges[e].Src];
				}
				
				computePaths(predVertices, predEdges, metric, optimalPathsCnt, currentLayer[v]);
			}
			
			lastLayer = currentLayer;
		}
		
		return lastLayer;
	}
}
