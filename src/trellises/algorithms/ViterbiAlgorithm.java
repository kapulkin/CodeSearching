package trellises.algorithms;

import java.util.Map;
import java.util.TreeMap;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;
import trellises.IntEdge;
import trellises.Trellis;

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
	public static class Vertex{
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
	private static void computePaths(Vertex[] srcVertices, IntEdge[] edges, int metric, int maxPaths, Vertex dstVertex)
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
				
				if(srcVertices[j].Metrics[pathIds[j]] + edges[j].metrics[metric] < bestMetric)
				{
					bestSrc = j;
					bestMetric = srcVertices[j].Metrics[pathIds[j]] + edges[j].metrics[metric];
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
			lastLayer[v] = new ViterbiAlgorithm.Vertex();
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
				currentLayer[v] = new ViterbiAlgorithm.Vertex();
				currentLayer[v].Index = v;
				
				IntEdge[] predEdges = trellis.Layers[layerInd][v].Predecessors;
				Vertex[] predVertices = new Vertex[predEdges.length];
				
				for(int e = 0;e < predEdges.length;e ++)
				{
					predVertices[e] = lastLayer[predEdges[e].src];
				}
				
				computePaths(predVertices, predEdges, metric, optimalPathsCnt, currentLayer[v]);
			}
			
			lastLayer = currentLayer;
		}
		
		return lastLayer;
	}

	/**
	 * Ищет вес кратчайшего ненулевого пути из вершины root до toor.
	 * 
	 * @param root
	 * @param toor
	 * @param metric номер метрики в решетке для рассчета веса путей
	 * @param stepCount ограничение на максимальное колличество шагов алгоритма. Не используется, если меньше 0 
	 * @return вес кратчайшего ненулевого пути из вершины root до toor
	 */
	public static int findMinDist(ITrellisIterator root, ITrellisIterator toor, int metric, int stepCount) {
		class WeightedIterator {
			public WeightedIterator(ITrellisIterator iterator, double weight) {
				this.iterator = iterator;
				this.weight = weight;
			}

			ITrellisIterator iterator;
			double weight;
		}
		
		if (root.vertexIndex() == toor.vertexIndex() &&  root.layer() == toor.layer()) {
			return 0;
		}
		
		Map<Long, WeightedIterator> layer = new TreeMap<Long, WeightedIterator>();
		ITrellisEdge edges[] = root.getAccessors();
		// выполняем переход по всем ребрам, кроме ребра нулевого пути
		for (int i = (root.vertexIndex() == 0 ? 1 : 0); i < edges.length; ++i) {
			ITrellisIterator iterator = root.clone();
			iterator.moveForward(i);
			WeightedIterator next = new WeightedIterator(iterator, edges[i].metric(metric));
			
			long vertexIndex = next.iterator.vertexIndex();
			if (!layer.containsKey(next.iterator.vertexIndex()) || layer.get(vertexIndex).weight > next.weight) {
				layer.put(vertexIndex, next);
			}
		}
		
		int step = 1;
		while (!(layer.containsKey(toor.vertexIndex()) 
				&& layer.get(toor.vertexIndex()).iterator.layer() == toor.layer())
				&& (step < stepCount || stepCount < 0)) {
			Map<Long, WeightedIterator> nextLayer = new TreeMap<Long, WeightedIterator>();
			
			for (WeightedIterator current : layer.values()) {
				ITrellisEdge edges2[] = current.iterator.getAccessors();
				for (int i = 1; i < edges2.length; ++i) {
					ITrellisIterator iterator = current.iterator.clone();
					iterator.moveForward(i);
					WeightedIterator next = new WeightedIterator(iterator, current.weight + edges2[i].metrics()[metric]);
					
					long vertexIndex = next.iterator.vertexIndex();
					if (!nextLayer.containsKey(next.iterator.vertexIndex()) || nextLayer.get(vertexIndex).weight > next.weight) {
						nextLayer.put(vertexIndex, next);
					}
				}
			}
			layer = nextLayer;
			++step;
		}
		
		if (!(layer.containsKey(toor.vertexIndex()) 
				&& layer.get(toor.vertexIndex()).iterator.layer() == toor.layer())) {
			// вышли по ограничению на количество шагов.
			return -1;
		}
		
		return (int)layer.get(toor.vertexIndex()).weight;
	}
}
