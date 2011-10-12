package trellises;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import trellises.Trellis.Edge;
import trellises.TrellisSection.Boundary;

import math.BitArray;
import math.GrayCode;
import math.ISpanForm;
import math.Matrix;

public class TrellisUtils {

	/**
	 * Возвращает номера рядов, соотвествующих индексу вершины в ярусе.
	 * Именно эти ряды суммируются, образуя метку на ребре. 
	 * 
	 * @param vertexIndex индекс вершины в ярусе
	 * @param activeRows номера активных рядов в ярусе 
	 * 
	 * @return индексы рядов, учавствующих в сумме.
	 */
	public static Set<Integer> getSumRows(long vertexIndex,
			SortedSet<Integer> activeRows) {
		Set<Integer> sumRows = new HashSet<Integer>();
		
		int i = 0;
		for (int row : activeRows) {
			if ((vertexIndex & (1L << i)) != 0) {
				sumRows.add(row);
			}
			++i;
		}
	
		return sumRows;
	}

	/**
	 * Возвращает индекс вершины в ярусе, соотвествующий рядам 
	 * <code>sumRows</code>.
	 * @param sumRows номера рядов, определяющие индекс вершины
	 * @param activeRows активные ряды в ярусе
	 * @return
	 */
	public static long getVertexIndex(Set<Integer> sumRows,
			SortedSet<Integer> activeRows) {
		long vertexIndex = 0;
		int i = 0;
		for (int row : activeRows) {
			if (sumRows.contains(row)) {
				vertexIndex |= (1L << i);
			}
			++i;
		}
		return vertexIndex;
	}

	/**
	 * Вычисляет кодовые биты на ребре. Для этого суммируются биты строк 
	 * матрицы, участвующие в сумме. Биты берутся из циклического интервала 
	 * [<code>fromIndex</code>, <code>toIndex</code>].
	 * @param matrix матрица кода
	 * @param sumRows индексы рядов матрицы, участвующих в сумме
	 * @param fromIndex начальная граница интервала
	 * @param toIndex конечная граница интервала
	 * @return сумма битов строк матрицы из заданного интервала
	 */
	public static BitArray getEdgeBits(Matrix matrix, Iterable<Integer> sumRows, int fromIndex, int toIndex) {
		BitArray bits = new BitArray(matrix.getColumnCount());
		for (int row : sumRows) {
			bits.xor(matrix.getRow(row));
		}
		return bits.get(fromIndex, toIndex);

//		BitArray bits = new BitArray((toIndex + matrix.getColumnCount() - fromIndex) % matrix.getColumnCount());
//		for (int row : sumRows) {
//			bits.xor(matrix.getRow(row).get(fromIndex, toIndex));
//		}		
//		return bits;
	}

	public static BitArray getEdgeBits(BitArray rows[], Iterable<Integer> sumRows, int fromIndex, int toIndex) {
		return getEdgeBits(new Matrix(rows), sumRows, fromIndex, toIndex);
	}	
	
	/**
	 * Метод строит ребра, исходящие из вершины <code>vertexIndex</code> с 
	 * использованием избыточных данных ради оптимизации.
	 *  
	 * @param matrix матрица, строки которой образуют метки на ребрах
	 * @param vertexIndex индекс вершины в решетке
	 * @param sum сумма активных рядов, соотвествующих <code>vertexIndex</code>
	 * @param sumRows активные ряды, участвующие в сумме, соотвествуют <code>vertexIndex</code>   
	 * @param activeRows активные ряды яруса
	 * @param spanHeads границы начинающихся в секции строк
	 * @param fromIndex начальная граница секции
	 * @param toIndex конечная граница секции
	 * @return ребра, идущие из
	 */
	public static ITrellisEdge[] buildAccessorsEdges(Matrix matrix, long vertexIndex,
			BitArray sum, Set<Integer> sumRows, SortedSet<Integer> activeRows, 
			ArrayList<Boundary> spanHeads, int fromIndex, int toIndex) {
		LongEdge edges[] = new LongEdge[1 << spanHeads.size()];
		BitArray bits;
		
		// ребро нулевого пути.
		bits = sum.get(fromIndex, toIndex);
		edges[0] = new LongEdge(vertexIndex, TrellisUtils.getVertexIndex(sumRows, activeRows), bits);

		for (int e = 1; e < edges.length; ++e) {
			int edgeIndex = GrayCode.getWord(e);
			int bitPos = GrayCode.getChangedPosition(e);
			int newRow = spanHeads.get(bitPos).row;
			
			if (GrayCode.getChangedBit(e)) {
				sumRows.add(newRow);
			} else {
				sumRows.remove(newRow);
			}
			sum.xor(matrix.getRow(newRow));
			
			bits = sum.get(fromIndex, toIndex);
			// ребро единичного пути.
			edges[edgeIndex] = new LongEdge(vertexIndex, TrellisUtils.getVertexIndex(sumRows, activeRows), bits);
		}
		if (edges.length > 1) {
			// восстанавливаем значение текущей суммы и текущих суммируемых рядов
			int lastRow = spanHeads.get(spanHeads.size()-1).row;
			sumRows.remove(lastRow);
			sum.xor(matrix.getRow(lastRow));
		}
		
		return edges;
	}
	

	/**
	 * Метод строит ребра, приходящие в вершину <code>vertexIndex</code> с 
	 * использованием избыточных данных ради оптимизации.
	 *  
	 * @param matrix матрица, строки которой образуют метки на ребрах
	 * @param vertexIndex индекс вершины в решетке
	 * @param sum сумма активных рядов, соотвествующих <code>vertexIndex</code>
	 * @param sumRows активные ряды, участвующие в сумме, соотвествуют <code>vertexIndex</code>   
	 * @param activeRows активные ряды яруса
	 * @param spanHeads границы начинающихся в секции строк
	 * @param fromIndex начальная граница секции
	 * @param toIndex конечная граница секции
	 * @return ребра, идущие из
	 */
	public static ITrellisEdge[] buildPredcessorsEdges(Matrix matrix, long vertexIndex,
			BitArray sum, Set<Integer> sumRows, SortedSet<Integer> activeRows, 
			ArrayList<Boundary> spanTails, int fromIndex, int toIndex) {
		LongEdge edges[] = new LongEdge[1 << spanTails.size()];
		BitArray bits;

		// ребро нулевого пути.
		bits = sum.get(fromIndex, toIndex);
		edges[0] = new LongEdge(TrellisUtils.getVertexIndex(sumRows, activeRows), vertexIndex, bits);

		for (int e = 1; e < edges.length; ++e) {
			int edgeIndex = GrayCode.getWord(e);
			int bitPos = GrayCode.getChangedPosition(e);
			int delRow = spanTails.get(bitPos).row;

			if (GrayCode.getChangedBit(e)) {
				sumRows.add(delRow);
			} else {
				sumRows.remove(delRow);
			}
			sum.xor(matrix.getRow(delRow));
			
			// ребро единичного пути.
			bits = sum.get(fromIndex, toIndex);
			edges[edgeIndex] = new LongEdge(TrellisUtils.getVertexIndex(sumRows, activeRows), vertexIndex, bits);
		}
		if (edges.length > 1) {
			// восстанавливаем значение текущей суммы и текущих суммируемых рядов
			int lastRow = spanTails.get(spanTails.size()-1).row;
			sumRows.remove(lastRow);
			sum.xor(matrix.getRow(lastRow));
		}
		
		return edges;
	}	
	
	public static ArrayList<TrellisSection> buildSections(ISpanForm spanForm) {
		// вычисляем секции будующей решетки
		SortedMap<Integer, TrellisSection> sectionsMap = new TreeMap<Integer, TrellisSection>();
		for (int row = 0; row < spanForm.getRowCount(); ++row) {
			int column = spanForm.getHead(row);
			if (!sectionsMap.containsKey(column)) {
				sectionsMap.put(column, new TrellisSection());
			}
			sectionsMap.get(column).spanHeads.add(new TrellisSection.Boundary(row, column));
		}
		
		for (int row = 0; row < spanForm.getRowCount(); ++row) {
			int column = spanForm.getTail(row);
			if (!sectionsMap.containsKey(column)) {
				sectionsMap.put(column, new TrellisSection());
			}
			sectionsMap.get(column).spanTails.add(new TrellisSection.Boundary(row, column));
		}
		
		ArrayList<TrellisSection> sectionsArray = new ArrayList<TrellisSection>();
		Iterator<TrellisSection> iter = sectionsMap.values().iterator();
		TrellisSection section = iter.next();
		while (section != null) {
			TrellisSection nextSection = iter.hasNext() ? iter.next() : null;
	
			if (section.spanTails.isEmpty() && nextSection != null && 
					nextSection.spanHeads.isEmpty() && !nextSection.spanTails.isEmpty()) {
				// объединяем две секции
				section.spanTails.addAll(nextSection.spanTails);
				nextSection = iter.hasNext() ? iter.next() : null; 
			}
	
			sectionsArray.add(section);
	
			section = nextSection;
		}
	
		return sectionsArray;
	}

	/**
	 * Для решетки <code>trellis</code> с построенными прямыми ребрами строит обратные.
	 * @param trellis решетка только с прямыми ребрами.
	 */
	public static void buildPredcessors(Trellis trellis) {
		for (int layer = 0; layer < trellis.Layers.length; ++layer) {
			for (int vertexIndex = 0; vertexIndex < trellis.Layers[layer].length; ++vertexIndex) {
				// строим обратные ребра. Если на последнем ярусе нет прямых ребер, то нет и обратных. 
				for (Edge edge : trellis.Layers[layer][vertexIndex].Accessors) {
					int vertex = edge.Dst;
					int nextLayer = (layer + 1) % trellis.Layers.length;
					
					Edge predcessors[] = trellis.Layers[nextLayer][vertex].Predecessors;
					Edge newPredecessors[];
					if (predcessors == null) {
						newPredecessors = new Edge[] {edge};
					} else {
						newPredecessors = new Edge[predcessors.length + 1];
						System.arraycopy(predcessors, 0, newPredecessors, 0, predcessors.length);
						newPredecessors[predcessors.length] = edge;
					}
					trellis.Layers[nextLayer][vertex].Predecessors = newPredecessors;
				}
			}
		}
	
		for (int vertexIndex = 0; vertexIndex < trellis.Layers[0].length; ++vertexIndex) {
			if (trellis.Layers[0][vertexIndex].Predecessors == null) {
				trellis.Layers[0][0].Predecessors = new Edge[0];
			}
		}
	}
	
	public static int stateComplexity(ITrellis trellis) {
		int max_s = 0;
		
		for(int l = 0;l < trellis.layersCount();++ l){
			int s = (int)Long.numberOfTrailingZeros(trellis.layerSize(l));
			
			if(s > max_s){
				max_s = s;
			}
		}
		
		return max_s;
	}
}
