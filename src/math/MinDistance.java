package math;

import java.util.ArrayList;

import math.ConvCodeSpanForm.SpanFormException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;
import search_tools.CEnumerator;
import trellises.ITrellis;
import trellises.ITrellisIterator;
import trellises.Trellis;
import trellises.algorithms.BeastAlgorithm;
import trellises.algorithms.ViterbiAlgorithm;

public class MinDistance {
	static private Logger logger = LoggerFactory.getLogger(MinDistance.class);
	
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
					int[] oldMetrics = trellis.Layers[i][j].Accessors[k].metrics;
					
					trellis.Layers[i][j].Accessors[k].metrics = new int[oldMetrics.length + 1];
					trellis.Layers[i][j].Accessors[k].metrics[0] = trellis.Layers[i][j].Accessors[k].bits.cardinality(); 
					for(int m = 1; m < oldMetrics.length + 1; m++)
					{
						trellis.Layers[i][j].Accessors[k].metrics[m] = oldMetrics[m - 1];
					}
				}
			}			
		}
	}

	/**
	 * Вычисляет min/free dist решетки блокового или сверточного кода с помощью алгоритма Витерби.
	 * Более строго: вычисляет длину кратчайшего ненулевого пути в решетке. Пути
	 * выходят из нулевой вершины нулевого слоя и проходят по решетке не
	 * более <code>cycles</code> раз. 
	 *
	 * @param trellis решетка блокового или сверточного кода
	 * @param distanceMetric
	 * @param cycles ограничение на кол-во циклов, пройденных по решетке. 
	 * @return вес кратчайшего ненулевого пути в решетке.
	 */
	public static int findMinDistWithViterby(Trellis trellis, int distanceMetric, int cycles, boolean fromZeroVertex)
	{
		int minDist = Integer.MAX_VALUE;		
		int lookForPathsFromCnt = fromZeroVertex ? 1 : trellis.Layers[0].length; 

		for(int v = 0; v < lookForPathsFromCnt; ++v)
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

	public static boolean assureFreeDist(ConvCode code, int freeDist) {
		for (int i = 0; i < code.parityCheck().getColumnCount(); ++i) {
			if (code.parityCheck().get(0, i).isZero()) {
				return freeDist <= 1;
			}
		}
		
		for (int d = 2; d < freeDist; ++d) {
			CEnumerator weightEnum = new CEnumerator(code.getN() * (code.getDelay() + 1), d);
			
			while (weightEnum.hasNext()) {
				long[] pows = weightEnum.next();
				Poly[] polys = new Poly[code.getN()];
				
				for (int i = 0;i < polys.length; ++i) {
					polys[i] = new Poly();
				}
				
				for (int i = 0;i < pows.length; ++i) {
					int polyInd = (int)pows[i] / (code.getDelay() + 1);
					int pow = (int)pows[i] % (code.getDelay() + 1);
					
					polys[polyInd].add(Poly.getUnitPoly().mulPow(pow));
				}
				
				Poly sum = new Poly();
				
				for (int i = 0;i < code.getN(); ++i) {
					sum.add(polys[i].mul(code.parityCheck().get(0, i)));
				}
				
				if (sum.isZero()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Вычисляет минимальное расстояние блокового кода.
	 * @param code блоковый код
	 * @return минимальное расстояние блокового кода
	 * @throws Exception 
	 */
	public static int findMinDist(BlockCode code) throws Exception {
//		code.setMinDist(findMinDistWithBEAST(new BlockCodeTrellis(code.getGeneratorSpanForm()), 0, code.getN()));
//		return code.getMinDist();
		return findMinDistWithBEAST(code.getTrellis(), 0, code.getN());
	}
	
	/**
	 * Вычисляет свободное расстояние сверточного кода.
	 * @param code сверточный код
	 * @return свободное расстояние сверточного кода
	 * @throws SpanFormException 
	 */
	public static int findFreeDist(ConvCode code) throws SpanFormException {
//		code.setFreeDist(findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1)));
//		return code.getFreeDist();
		return findMinDistWithBEAST(code.getTrellis(), 0, code.getN() * (code.getDelay() + 1));
	}
	
	/**
	 * Вычисляет минимальное расстояние усеченного блокового кода.
	 * @param code усеченного блоковый код
	 * @return минимальное расстояние усеченного блокового кода
	 * @throws SpanFormException 
	 */
	public static int findMinDist(TBCode code) throws SpanFormException {
		ITrellis trellis = code.getTrellis();//new BlockCodeTrellis(code.getGeneratorSpanForm());
		
		int minDist = Integer.MAX_VALUE;
		for (int vertexIndex = 0; vertexIndex < trellis.layerSize(0); ++vertexIndex) {
			ITrellisIterator root = trellis.iterator(0, vertexIndex);
			ITrellisIterator toor = trellis.iterator(trellis.layersCount() - 1, vertexIndex);
			
			minDist = Math.min(minDist, BeastAlgorithm.countMinDist(root, toor, 0, code.getN()));
		}

		return minDist;
	}

	/**
	 * Вычисляет метрику на ребрах решетки и вычисляет min/free dist кода.
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
	 * @param distanceMetric номер метрики в решетке для рассчета веса путей
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
	public static int findMinDist(Matrix gen) {
		if (gen.getRowCount() > Integer.SIZE - 2) {
			throw new IllegalArgumentException("Code with input word length more, then " + (Integer.SIZE - 2) + ", is not supported.");
		}
		
		int minDist = Integer.MAX_VALUE;
		BitArray codeWord = new BitArray(gen.getColumnCount());

		for (int w = 1; w < (1<<gen.getRowCount()); w++) {			
			int changedBit = GrayCode.getChangedPosition(w);
			
			codeWord.xor(gen.getRow(changedBit));
			int weight = codeWord.cardinality();
			if (weight < minDist && weight != 0) {
				minDist = weight;
			}
		}
		
		return minDist;
	}
	
	public static int minN(int k, int minDist) {
		int n = 0;
		
		for (int j = 0; j < k; ++j) {
			n += Math.ceil((double)minDist / (1<<j));
		}
		
		return n;
	}
}
