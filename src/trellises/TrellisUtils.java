package trellises;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import trellises.Trellis.Edge;

import math.BitArray;
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
	public static Set<Integer> getSumRows(int vertexIndex,
			SortedSet<Integer> activeRows) {
		Set<Integer> sumRows = new HashSet<Integer>();
		
		int i = 0;
		for (int row : activeRows) {
			if ((vertexIndex & (1 << i)) != 0) {
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
	public static int getVertexIndex(Set<Integer> sumRows,
			SortedSet<Integer> activeRows) {
		int vertexIndex = 0;
		int i = 0;
		for (int row : activeRows) {
			if (sumRows.contains(row)) {
				vertexIndex |= (1 << i);
			}
			++i;
		}
		return vertexIndex;
	}

	public static BitArray getEdgeBits(Matrix matrix, Iterable<Integer> sumRows, int fromIndex, int toIndex) {
		BitArray bits = new BitArray(toIndex - fromIndex);

		for (int row : sumRows) {
			bits.xor(matrix.getRow(row).get(fromIndex, toIndex));
		}			
		
		return bits;
	}

	public static BitArray getEdgeBits(BitArray rows[], Iterable<Integer> sumRows, int fromIndex, int toIndex) {
		return getEdgeBits(new Matrix(rows), sumRows, fromIndex, toIndex);
	}	
	
	public static TrellisSection[] buildSections(ISpanForm spanForm) {
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
	
		TrellisSection sections[] = new TrellisSection[sectionsArray.size()];
		sectionsArray.toArray(sections);
		return sections;
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
}
