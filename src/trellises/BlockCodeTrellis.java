package trellises;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.BitSet;
import math.SpanForm;

import trellises.Trellis.Edge;
import codes.BlockCode;

public class BlockCodeTrellis implements ITrellis {
	/**
	 * Граница активного ряда в спеновой форме. Может быть началом или концом 
	 * ряда. 
	 * @author stas
	 *
	 */
	class Boundary {
		int row;
		int column;
		
		Boundary(int column, int row) {
			this.column = column;
			this.row = row;
		}
	}
	
	/**
	 * Ярусы решетки блокогого кода расположены между сегментами порождающей 
	 * матрицы. Сегмент включает в себя один или несколько последовательно 
	 * расположенных столбцов. Объекты данного класса представляют собой ярус с 
	 * описанием изменений, которые происходят в <b>следующем</b> за ярусом 
	 * сегменте.
	 * @author stas
	 *
	 */
	class Layer {
		Boundary spanHead = null;
		Boundary spanTail = null;

		public Layer() {
		}
		
		public Layer(Boundary spanHead, Boundary spanTail) {
			this.spanHead = spanHead;
			this.spanTail = spanTail;
		}
					
		int beginColumn() {
			return spanHead != null ? spanHead.column : spanTail.column;
		}
		
		@Override
		public String toString() {
			return "[" + 
				((spanHead != null) ? "h:" + spanHead.column : "") +
				(spanHead != null && spanTail != null ? ", " : "") +
				((spanTail != null) ? "t:" + spanTail.column : "") + 
				"]";
		}
	}
	
	public class Iterator implements TrellisIterator {
		Logger logger;
		
		int layer;
		int vertexIndex;
		
		SortedSet<Integer> currentActiveRows;
		Set<Integer> currentSumRows;
		
		Iterator(int layer, int vertexIndex) {
			logger = LoggerFactory.getLogger(this.getClass());
			
			this.layer = layer;
			this.vertexIndex = vertexIndex;
		
			currentActiveRows = spanForm.getActiveRowsBefore(layers[layer].beginColumn());
			currentSumRows = getSumRows(currentActiveRows, vertexIndex); 
		}

		@Override
		public Edge[] getAccessors() {
			if (!hasForward()) {
				return new Edge[0];
			}

			Edge edges[];

			// получаем следующие активные ряды: добавляем к текущим начавшийся активный ряд и удаляем завершившийся
			SortedSet<Integer> nextActiveRows = new TreeSet<Integer>(currentActiveRows);
			if (layers[layer].spanHead != null) {
				nextActiveRows.add(layers[layer].spanHead.row);
			}
			if (layers[layer].spanTail != null) {
				nextActiveRows.remove(layers[layer].spanTail.row);
			}

			// ребро нулевого пути.
			Edge edge = new Edge();
			edge.Src = vertexIndex;
			edge.Dst = getVertexIndex(currentSumRows, nextActiveRows);
			edge.Bits = getEdgeBits(currentSumRows, layers[layer].beginColumn(), layers[layer + 1].beginColumn());

			if (layers[layer].spanHead != null) {
				int newRow = layers[layer].spanHead.row;
				Set<Integer> nextSumRows = new HashSet<Integer>(currentSumRows);
				nextSumRows.add(newRow);
				edges = new Edge[2];

				// ребро единичного пути.
				edges[1] = new Edge();
				edges[1].Src = vertexIndex;
				edges[1].Dst = getVertexIndex(nextSumRows, nextActiveRows);
				edges[1].Bits = (BitSet) edge.Bits.clone();
				edges[1].Bits.xor(spanForm.Matr.getRow(newRow).get(layers[layer].beginColumn(), layers[layer + 1].beginColumn()));
			} else {
				edges = new Edge[1];
			}
			edges[0] = edge;
			
			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}
			
			return edges;
		}

		private BitSet getEdgeBits(Iterable<Integer> sumRows, int fromIndex, int toIndex) {
			BitSet bits = new BitSet(toIndex - fromIndex);

			for (int row : sumRows) {
				bits.xor(spanForm.Matr.getRow(row).get(fromIndex, toIndex));
			}			
			
			return bits;
		}

		/**
		 * Возвращает индекс вершины в ярусе, соотвествующий рядам 
		 * <code>sumRows</code>.
		 * @param sumRows ряды, которые участвуют в сложении
		 * @param activeRows активные ряды в ярусе
		 * @return
		 */
		private int getVertexIndex(Set<Integer> sumRows,
				Iterable<Integer> activeRows) {
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

		/**
		 * Возвращает ряды, которые будут складываться для вычисления метки на ребре.
		 * 
		 * @param activeRows активные ряды, сложение которых может повлиять на метку. 
		 * @param vertexIndex индекс вершины в ярусе, указывающие на ряды, учавствующие в сумме
		 * @return индексы рядов, учавствующих в сумме.
		 */
		private Set<Integer> getSumRows(Iterable<Integer> activeRows,
				int vertexIndex) {
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

		@Override
		public Edge[] getPredecessors() {
			if (!hasBackward()) {
				return new Edge[0];
			}
			
			Edge edges[];

			// получаем предыдущие активные ряды
			SortedSet<Integer> prevActiveRows = new TreeSet<Integer>(currentActiveRows);
			if (layers[layer - 1].spanHead != null) {
				prevActiveRows.remove(layers[layer - 1].spanHead.row);
			}
			if (layers[layer - 1].spanTail != null) {
				prevActiveRows.add(layers[layer - 1].spanTail.row);
			}
			// ребро нулевого пути.
			Edge edge = new Edge();
			edge.Src = getVertexIndex(currentSumRows, prevActiveRows);
			edge.Dst = vertexIndex;
			edge.Bits = getEdgeBits(currentSumRows, layers[layer - 1].beginColumn(), layers[layer].beginColumn());

			if (layers[layer - 1].spanTail != null) {
				int delRow = layers[layer - 1].spanTail.row;
				Set<Integer> prevSumRows = new HashSet<Integer>(currentSumRows);
				prevSumRows.add(delRow);
				
				edges = new Edge[2];
				// ребро единичного пути.
				edges[1] = new Edge();
				edges[1].Src = getVertexIndex(prevSumRows, prevActiveRows);
				edges[1].Dst = vertexIndex;
				edges[1].Bits = (BitSet) edge.Bits.clone();
				edges[1].Bits.xor(spanForm.Matr.getRow(delRow).get(layers[layer - 1].beginColumn(), layers[layer].beginColumn()));
			} else {
				edges = new Edge[1];
			}
			edges[0] = edge;
			
			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}

			return edges;
		}

		@Override
		public boolean hasBackward() {
			return layer > 0;
		}

		@Override
		public boolean hasForward() {
			return layer < layers.length - 1;
		}

		@Override
		public int layer() {
			return layer;
		}

		@Override
		public void moveBackward(int edgeIndex) throws NoSuchElementException {
			if (!hasBackward()) {
				throw new NoSuchElementException();
			}
			vertexIndex = getPredecessors()[edgeIndex].Src;

			if (layers[layer].spanTail != null) {
				currentActiveRows.add(layers[layer].spanTail.row);
				if (edgeIndex == 1) {
					currentSumRows.add(layers[layer].spanTail.row);
				}
			}
			if (layers[layer].spanHead != null) {
				currentActiveRows.remove(layers[layer].spanHead.row);
				currentSumRows.remove(layers[layer].spanHead.row);
			}

			--layer;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!hasForward()) {
				throw new NoSuchElementException();
			}
			vertexIndex = getAccessors()[edgeIndex].Dst;
			
			if (layers[layer].spanHead != null) {
				currentActiveRows.add(layers[layer].spanHead.row);
				if (edgeIndex == 1) {
					currentSumRows.add(layers[layer].spanHead.row);
				}
			}
			if (layers[layer].spanTail != null) {
				currentActiveRows.remove(layers[layer].spanTail.row);
				currentSumRows.remove(layers[layer].spanTail.row);
			}
			
			++layer;
		}

		@Override
		public int vertexIndex() {
			return vertexIndex;
		}
		
		@Override
		public TrellisIterator clone() {
			return new Iterator(layer, vertexIndex);
		}
	}
	
	private BlockCode code;
	private SpanForm spanForm;
	private Layer layers[];

	private Logger logger;
	
	public BlockCodeTrellis(BlockCode code) {
		logger = LoggerFactory.getLogger(this.getClass());
		
		this.code = code;
		this.spanForm = code.getGeneratorSpanForm();
		
		logger.debug("Construction of trellis for " + code.getK() + "/" + code.getN() + " code");
		
		// Создаем упорядоченный список всех границ. 
		SortedMap<Integer, Layer> layersMap = new TreeMap<Integer, Layer>();
		for (int row = 0; row < spanForm.spanHeads.length; ++row) {
			int column = spanForm.spanHeads[row];
			if (!layersMap.containsKey(column)) {
				layersMap.put(column, new Layer());
			}
			layersMap.get(column).spanHead = new Boundary(column, row);
		}
		logger.debug("Span head layers count is " + layersMap.size());

		for (int row = 0; row < spanForm.spanTails.length; ++row) {
			int column = spanForm.spanTails[row];
			if (!layersMap.containsKey(column)) {
				layersMap.put(column, new Layer());
			}
			layersMap.get(column).spanTail = new Boundary(column, row);
		}
		logger.debug("Total span layers count is " + layersMap.size());

		ArrayList<Layer> layersArray = new ArrayList<Layer>();
		// We hope that layersMap has at least one element. In other case the input code is illegal.
		java.util.Iterator<Layer> iter = layersMap.values().iterator();
		Layer layer = iter.next();
		while (true) {
			boolean layersAreMerged = false;
			Layer nextLayer = null;
			
			if (layer.spanHead != null && layer.spanTail == null) {
				// если в текущем ярусе только одна гарница - начало ряда, ...
				if (iter.hasNext()) {
					nextLayer = iter.next();
					if (nextLayer.spanHead == null && nextLayer.spanTail != null) {
						// а в следующующем ярусе только одна граница - конец ряда, то объединяем ярусы.
						layer.spanTail = nextLayer.spanTail;
						layersAreMerged = true;
					}
				}
			}
			
			layersArray.add(layer);

			if (nextLayer != null) {
				if (!layersAreMerged) {
					layer = nextLayer;
					continue;
				}
			}
			
			if (iter.hasNext()) {
				layer = iter.next();
			} else {
				break;
			}
		}
		Boundary dummyBoundary = new Boundary(code.getN(), -1);
		Layer lastLayer = new Layer(dummyBoundary, null);
		layersArray.add(lastLayer); // добавляем последний слой с финктивной границей

		for (Layer layer2 : layersArray) {
			logger.debug(layer2.toString());
		}
		
		layers = new Layer[layersArray.size()];
		layersArray.toArray(layers); // эта строчка в общем-то необязательна. Можно вполне работать и с ArrayList
		
		logger.debug("Layers count is " + layers.length);
	}
	
	@Override
	public TrellisIterator iterator(int layer, int vertexIndex) {
		return new Iterator(layer, vertexIndex);
	}

	@Override
	public int layerSize(int layer) {
		return 1 << layerComplexity(layer);
	}
	
	public int layerComplexity(int layer) {
		if (layer == layers.length - 1) {
			return 0;
		}
		
		int activeRowsCount = 0;

		int column = layers[layer].beginColumn();
		for (int row = 0; row < spanForm.Matr.getRowCount(); ++row) {
			if (spanForm.isRowActiveBefore(column, row)) {
				++activeRowsCount;
			}
		}
		
		return activeRowsCount;
	}

	@Override
	public int layersCount() {
		return layers.length;
	}

}
