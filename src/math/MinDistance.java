package math;

import codes.BlockCode;
import codes.ConvCode;
import trellises.BeastAlgorithm;
import trellises.BlockCodeTrellis;
import trellises.ITrellis;
import trellises.ITrellisIterator;
import trellises.Trellis;
import trellises.ViterbiAlgorithm;

public class MinDistance {
	/**
	 * Рассчитывает метрику решетки, где весовой функцией выступает вес кодовых слов на ребрах.
	 * Метрика помещается в нулевую ячейку метрик на ребрах, ранее рассчитанные метрики смещаются на одну позицию вправо.
	 * @param trellis
	 */
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

	/**
	 * Вычисляет min/free dist решетки блокового или сверточного кода с помощью алгоритма Витерби.
	 * Более строго: вычисляет длину кратчайшего ненулевого пути в решетке. Пути
	 * выходят из нулевой вершины нулевого слоя и имеют проходят по решетке не
	 * более <code>cycles</code> раз. 
	 *
	 * @param trellis решетка блокового или сверточного кода
	 * @param distanceMetric
	 * @param cycles ограничение на кол-во циклов, пройденных по решетке. 
	 * @return вес кратчайшего ненулевого пути в решетке.
	 */
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

	/**
	 * Вычисляет минимальное расстояние блокового кода.
	 * @param code блоковый код
	 * @return
	 */
	public static int findMinDist(BlockCode code) {
		return findMinDistWithBEAST(new BlockCodeTrellis(code.getGeneratorSpanForm()), 0, code.getN());
	}
	
	public static int findFreeDist(ConvCode code) {
		PolyMatrix minBaseGen = ConvCodeAlgs.getMinimalBaseGenerator(code.generator());
		Trellis trellis = ConvCodeAlgs.buildTrellis(ConvCodeAlgs.buildSpanForm(minBaseGen));
		computeDistanceMetrics(trellis);
		return findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
	}
	
	/**
	 * Вычисляет метрику на ребрах решекти и вычисляет min/free dist кода.
	 * @param trellis
	 * @param upperBound
	 * @return
	 */
	public static int findMinDistWithBEAST(Trellis trellis, int upperBound) {
		computeDistanceMetrics(trellis);
		return findMinDistWithBEAST(trellis, 0, upperBound);
	}
	
	/**
	 * Вычисляет min/free dist решетки блокового или сверточного кода с помощью алгоритма BEAST.
	 * Более строго: вычисляет длину кратчайшего ненулевого пути в решетке.
	 * 
	 * Верхняя оценка upperBound используется как страховочный механизм для защиты от 
	 * бесконечной работы алгоритма в случае ошибки в нем или входной решетке.
	 *  
	 * Для работы с блоковым кодом: <code>upperBound</code> достаточно взять равным n.
	 * Для работы со сверточным кодом: <code>upperBound</code> достаточно взять равным n*(delay+1)  
	 * 
	 * @param trellis решетка блокового или сверточного кода 
	 * @param distanceMetric номер метрики в решетке для рассчета веса путей.
	 * @param upperBound верхняя оценка min/free dist 
	 * @return минимальное расстояния кода
	 */
	public static int findMinDistWithBEAST(ITrellis trellis, int distanceMetric, int upperBound) {
		ITrellisIterator root = trellis.iterator(0, 0);
		ITrellisIterator toor = trellis.iterator(trellis.layersCount() - 1, 0);
		
		return BeastAlgorithm.countMinDist(root, toor, distanceMetric, upperBound);
	}
	
	/**
	 * Находим мнимальное расстояние блокового кода, перебирая все слова по порождающей матрице.
	 * @param gen порождающая матрица блокового кода
	 * @return минимальное расстояние блокового кода
	 */
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
