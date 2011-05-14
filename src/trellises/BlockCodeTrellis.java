package trellises;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.BitArray;
import math.SpanForm;

import trellises.Trellis.Edge;
import trellises.TrellisSection.Boundary;
import codes.BlockCode;

/**
 * Реализует интерфейс ITrellis для блокового кода, передаваемого в конструкторе.
 * При этом решетка строится на лету, в явном виде построения не происходит.
 * Для блокового кода со скроростью k/n время перехода в решекте O(k).   
 * @author stas
 *
 */
public class BlockCodeTrellis implements ITrellis {
	/**
	 * Ярусы решетки блокогого кода расположены между сегментами порождающей 
	 * матрицы. Сегмент включает в себя один или несколько последовательно 
	 * расположенных столбцов. Объекты данного класса представляют собой ярус с 
	 * описанием изменений, которые происходят в <b>следующем</b> за ярусом 
	 * сегменте.
	 * @author stas
	 *
	 */
	static class Layer {
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
	
	public class Iterator implements ITrellisIterator {
		Logger logger;
		
		int layer;
		int vertexIndex;
		
		/**
		 * Номера рядов, активных в текущем ярусе. Фактически, номера рядов, пересекающих ярус.
		 */
		SortedSet<Integer> currentActiveRows;
		/**
		 * Номера рядов, определяющих номер вершины в ярусе. Так же данные ряды складываются при вычислении меток на ребрах. 
		 */
		Set<Integer> currentSumRows;
		
		Iterator(int layer, int vertexIndex) {
			logger = LoggerFactory.getLogger(this.getClass());
			
			this.layer = layer;
			this.vertexIndex = vertexIndex;
		
			currentActiveRows = spanForm.getActiveRowsBefore(layers[layer].beginColumn());
			currentSumRows = TrellisUtils.getSumRows(vertexIndex, currentActiveRows); 
		}

		@Override
		public Edge[] getAccessors() {
			if (!hasForward()) {
				return new Edge[0];
			}

			// получаем следующие активные ряды: добавляем к текущим начавшийся активный ряд и удаляем завершившийся
			SortedSet<Integer> nextActiveRows = new TreeSet<Integer>(currentActiveRows);
			if (layers[layer].spanHead != null) {
				nextActiveRows.add(layers[layer].spanHead.row);
			}
			if (layers[layer].spanTail != null) {
				nextActiveRows.remove(layers[layer].spanTail.row);
			}

			Edge edges[] = new Edge[layers[layer].spanHead == null ? 1 : 2];
			
			// ребро нулевого пути.
			edges[0] = new Edge();
			edges[0].Src = vertexIndex;
			edges[0].Dst = TrellisUtils.getVertexIndex(currentSumRows, nextActiveRows);
			edges[0].Bits = TrellisUtils.getEdgeBits(spanForm.Matr, currentSumRows, 
					layers[layer].beginColumn(), layers[layer + 1].beginColumn());
			edges[0].Metrics = new double[1];
			edges[0].Metrics[0] = edges[0].Bits.cardinality();

			if (layers[layer].spanHead != null) {
				int newRow = layers[layer].spanHead.row;

				// ребро единичного пути.
				edges[1] = new Edge();
				edges[1].Src = vertexIndex;
				currentSumRows.add(newRow);
				edges[1].Dst = TrellisUtils.getVertexIndex(currentSumRows, nextActiveRows);
				currentSumRows.remove(newRow);
				edges[1].Bits = (BitArray) edges[0].Bits.clone();
				edges[1].Bits.xor(spanForm.Matr.getRow(newRow).get(layers[layer].beginColumn(), layers[layer + 1].beginColumn()));
				edges[1].Metrics = new double[1];
				edges[1].Metrics[0] = edges[1].Bits.cardinality();
			}
			
			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}
			
			return edges;
		}

		@Override
		public Edge[] getPredecessors() {
			if (!hasBackward()) {
				return new Edge[0];
			}
			
			// получаем предыдущие активные ряды
			SortedSet<Integer> prevActiveRows = new TreeSet<Integer>(currentActiveRows);
			if (layers[layer - 1].spanTail != null) {
				prevActiveRows.add(layers[layer - 1].spanTail.row);
			}
			if (layers[layer - 1].spanHead != null) {
				prevActiveRows.remove(layers[layer - 1].spanHead.row);
			}
			
			Edge edges[] = new Edge[layers[layer - 1].spanTail == null ? 1 : 2];

			// ребро нулевого пути.
			edges[0] = new Edge();
			edges[0].Src = TrellisUtils.getVertexIndex(currentSumRows, prevActiveRows);
			edges[0].Dst = vertexIndex;
			edges[0].Bits = TrellisUtils.getEdgeBits(spanForm.Matr, currentSumRows, 
					layers[layer - 1].beginColumn(), layers[layer].beginColumn());
			edges[0].Metrics = new double[1];
			edges[0].Metrics[0] = edges[0].Bits.cardinality();

			if (layers[layer - 1].spanTail != null) {
				int delRow = layers[layer - 1].spanTail.row;
				
				// ребро единичного пути.
				edges[1] = new Edge();
				currentSumRows.add(delRow);
				edges[1].Src = TrellisUtils.getVertexIndex(currentSumRows, prevActiveRows);
				currentSumRows.remove(delRow);
				edges[1].Dst = vertexIndex;
				edges[1].Bits = (BitArray) edges[0].Bits.clone();
				edges[1].Bits.xor(spanForm.Matr.getRow(delRow).get(layers[layer - 1].beginColumn(), layers[layer].beginColumn()));
				edges[1].Metrics = new double[1];
				edges[1].Metrics[0] = edges[1].Bits.cardinality();
			}
			
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

			if (edgeIndex < 0 ||
				layers[layer - 1].spanTail == null && edgeIndex > 0) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

			if (layers[layer - 1].spanTail != null) {
				currentActiveRows.add(layers[layer - 1].spanTail.row);
				if (edgeIndex == 1) {
					currentSumRows.add(layers[layer - 1].spanTail.row);
				}
			}
			if (layers[layer - 1].spanHead != null) {
				currentActiveRows.remove(layers[layer - 1].spanHead.row);
				currentSumRows.remove(layers[layer - 1].spanHead.row);
			}
			
			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			--layer;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!hasForward()) {
				throw new NoSuchElementException();
			}

			if (edgeIndex < 0 ||
				layers[layer].spanHead == null && edgeIndex > 0) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

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

			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			++layer;
		}

		@Override
		public int vertexIndex() {
			return vertexIndex;
		}
		
		@Override
		public ITrellisIterator clone() {
			return new Iterator(layer, vertexIndex);
		}
	}
	
	private SpanForm spanForm;
	private Layer layers[];

	private Logger logger;
	
	public BlockCodeTrellis(BlockCode code) {
		logger = LoggerFactory.getLogger(this.getClass());
		
		this.spanForm = code.getGeneratorSpanForm();
		
		logger.debug("Construction of trellis for " + code.getK() + "/" + code.getN() + " code");
		
		// Создаем упорядоченный список всех границ. 
		SortedMap<Integer, Layer> layersMap = new TreeMap<Integer, Layer>();
		for (int row = 0; row < spanForm.getRowCount(); ++row) {
			int column = spanForm.getHead(row);
			if (!layersMap.containsKey(column)) {
				layersMap.put(column, new Layer());
			}
			layersMap.get(column).spanHead = new Boundary(row, column);
		}
		logger.debug("Span head layers count is " + layersMap.size());

		for (int row = 0; row < spanForm.getRowCount(); ++row) {
			int column = spanForm.getTail(row);
			if (!layersMap.containsKey(column)) {
				layersMap.put(column, new Layer());
			}
			layersMap.get(column).spanTail = new Boundary(row, column);
		}
		logger.debug("Total span layers count is " + layersMap.size());

		ArrayList<Layer> layersArray = new ArrayList<Layer>();
		// We hope that layersMap has at least one element. In other case the input code is illegal.
		java.util.Iterator<Layer> iter = layersMap.values().iterator();
		Layer layer = iter.next();
		while (layer != null) {
			Layer nextLayer = iter.hasNext() ? iter.next() : null;
			
			if (layer.spanTail == null && nextLayer != null && nextLayer.spanTail != null) {
				// объединяем ярусы.
				layer.spanTail = nextLayer.spanTail;
				nextLayer = iter.hasNext() ? iter.next() : null;
			}
			
			layersArray.add(layer);

			layer = nextLayer;
		}
		Boundary dummyBoundary = new Boundary(-1, code.getN());
		Layer lastLayer = new Layer(dummyBoundary, null);
		layersArray.add(lastLayer); // добавляем последний слой с фиктивной границей

		for (Layer layer2 : layersArray) {
			logger.debug(layer2.toString());
		}
		
		layers = new Layer[layersArray.size()];
		layersArray.toArray(layers); // эта строчка в общем-то необязательна. Можно вполне работать и с ArrayList
		
		logger.debug("Layers count is " + layers.length);
	}
	
	@Override
	public ITrellisIterator iterator(int layer, int vertexIndex) {
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
