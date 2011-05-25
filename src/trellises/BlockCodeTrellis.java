package trellises;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.BitArray;
import math.SpanForm;

import trellises.TrellisSection.Boundary;

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
	 * описанием изменений, которые происходят в <strong>следующем</strong> за ярусом 
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
				((spanHead != null) ? "h: " + spanHead.row + ", " + spanHead.column : "") +
				(spanHead != null && spanTail != null ? "; " : "") +
				((spanTail != null) ? "t: " + spanTail.row + ", " + spanTail.column : "") + 
				"]";
		}
	}
	
	static class Edge implements ITrellisEdge {
		private long src;
		private long dst;
		private BitArray bits;
		private double metrics[];
		
		/**
		 * Создает ребро, где в качестве метрики берется вес ребра.
		 * @param src индекс вершины, из которой исходит ребро
		 * @param dst индекс вершины, в которую ведет ребро
		 * @param bits кодовые символы на ребре
		 */
		public Edge(long src, long dst, BitArray bits) {
			this.src = src;
			this.dst = dst;
			this.bits = bits;
			metrics = new double[] { bits.cardinality() };
		}
		/**
		 * Создает ребро с заданными метриками
		 * @param src индекс вершины, из которой исходит ребро
		 * @param dst индекс вершины, в которую ведет ребро
		 * @param bits метка из битов на ребре
		 * @param metrics метрики ребра
		 */
		public Edge(long src, long dst, BitArray bits, double metrics[]) {
			this.src = src;
			this.dst = dst;
			this.bits = bits;
			this.metrics = metrics;
		}

		@Override
		public long src() {
			return src;
		}
		@Override
		public long dst() {
			return dst;
		}
		@Override
		public BitArray bits() {
			return bits;
		}
		@Override
		public double[] metrics() {
			return metrics;
		}
		@Override
		public String toString() {
			return src + "‒" + bits + "→" + dst;
		}		
	}

	public class Iterator implements ITrellisIterator {
		Logger logger;
		
		int layer;
		long vertexIndex;
		
		/**
		 * Номера рядов, активных в текущем ярусе. Фактически, номера рядов, пересекающих ярус.
		 */
		SortedSet<Integer> currentActiveRows;
		/**
		 * Номера рядов, определяющих номер вершины в ярусе. Так же данные ряды складываются при вычислении меток на ребрах. 
		 */
		Set<Integer> currentSumRows;
		
		Iterator(int layer, long vertexIndex) {
			logger = LoggerFactory.getLogger(this.getClass());
			
			this.layer = layer;
			this.vertexIndex = vertexIndex;
		
			currentActiveRows = spanForm.getActiveRowsBefore(sections[layer].beginColumn());
			currentSumRows = TrellisUtils.getSumRows(vertexIndex, currentActiveRows); 
		}

		@Override
		public Edge[] getAccessors() {
			if (!hasForward()) {
				return new Edge[0];
			}

			// получаем следующие активные ряды: добавляем к текущим начавшийся активный ряд и удаляем завершившийся
			SortedSet<Integer> nextActiveRows = new TreeSet<Integer>(currentActiveRows);
			for (Boundary spanHead : sections[layer].spanHeads) {
				nextActiveRows.add(spanHead.row);
			}
			for (Boundary spanTail : sections[layer].spanTails) {
				nextActiveRows.remove(spanTail.row);
			}
			int nextLayer = (layer + 1) % sections.length;
			
			Edge edges[] = new Edge[1 << sections[layer].spanHeads.size()];
			BitArray bits;
			
			// ребро нулевого пути.
			bits = TrellisUtils.getEdgeBits(spanForm.Matr, currentSumRows, 
					sections[layer].beginColumn(), sections[nextLayer].beginColumn());
			edges[0] = new Edge(vertexIndex, TrellisUtils.getVertexIndex(currentSumRows, nextActiveRows), bits);

			for (int e = 1; e < edges.length; ++e) {
				// ребро единичного пути.
				bits = (BitArray) edges[0].bits.clone();
				for (int i = 0; i < sections[layer].spanHeads.size(); ++i) {
					if ((e & (1 << i)) != 0) {
						int newRow = sections[layer].spanHeads.get(i).row;
						bits.xor(spanForm.Matr.getRow(newRow).get(
								sections[layer].beginColumn(), sections[nextLayer].beginColumn()));
						currentSumRows.add(newRow);
					}
				}
				edges[1] = new Edge(vertexIndex, TrellisUtils.getVertexIndex(currentSumRows, nextActiveRows), bits);
				for (int i = 0; i < sections[layer].spanHeads.size(); ++i) {
					if ((e & (1 << i)) != 0) {
						int newRow = sections[layer].spanHeads.get(i).row;
						currentSumRows.remove(newRow);
					}
				}
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
			
			int prevLayer = (layer - 1 + sections.length) % sections.length;
			
			// получаем предыдущие активные ряды
			SortedSet<Integer> prevActiveRows = new TreeSet<Integer>(currentActiveRows);
			for (Boundary spanTail : sections[prevLayer].spanTails) {
				prevActiveRows.add(spanTail.row);
			}
			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				prevActiveRows.remove(spanHead.row);
			}
			
			Edge edges[] = new Edge[1 << sections[prevLayer].spanTails.size()];
			BitArray bits;

			// ребро нулевого пути.
			bits = TrellisUtils.getEdgeBits(spanForm.Matr, currentSumRows, 
					sections[prevLayer].beginColumn(), sections[layer].beginColumn());
			edges[0] = new Edge(TrellisUtils.getVertexIndex(currentSumRows, prevActiveRows), vertexIndex, bits);

			for (int e = 1; e < edges.length; ++e) {
				// ребро единичного пути.
				bits = (BitArray) edges[0].bits.clone();
				for (int i = 0; i < sections[prevLayer].spanTails.size(); ++i) {
					if ((e & (1 << i)) != 0) {
						int delRow = sections[prevLayer].spanTails.get(i).row;
						bits.xor(spanForm.Matr.getRow(delRow).get(sections[prevLayer].beginColumn(), sections[layer].beginColumn()));
						currentSumRows.add(delRow);
					}
				}
				edges[e] = new Edge(TrellisUtils.getVertexIndex(currentSumRows, prevActiveRows), vertexIndex, bits);
				for (int i = 0; i < sections[prevLayer].spanTails.size(); ++i) {
					if ((e & (1 << i)) != 0) {
						int delRow = sections[prevLayer].spanTails.get(i).row;
						currentSumRows.remove(delRow);
					}
				}
			}
			
			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}

			return edges;
		}

		@Override
		public boolean hasBackward() {
			return spanForm.IsTailbiting || layer > 0;
		}

		@Override
		public boolean hasForward() {
			return spanForm.IsTailbiting || layer < sections.length - 1;
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

			int prevLayer = (layer - 1 + sections.length) % sections.length;
			
			if (edgeIndex < 0 || edgeIndex >= (1 << sections[prevLayer].spanTails.size())) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

			for (Boundary spanTail : sections[prevLayer].spanTails) {
				currentActiveRows.add(spanTail.row);
				if (edgeIndex == 1) {
					currentSumRows.add(spanTail.row);
				}
			}

			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
				currentSumRows.remove(spanHead.row);
			}
			
			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			layer = prevLayer;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (!hasForward()) {
				throw new NoSuchElementException();
			}

			if (edgeIndex < 0 || edgeIndex >= (1 << sections[layer].spanHeads.size())) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

			for (Boundary spanHead : sections[layer].spanHeads) {
				currentActiveRows.add(spanHead.row);
				if (edgeIndex == 1) {
					currentSumRows.add(spanHead.row);
				}
			}
			
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.remove(spanTail.row);
				currentSumRows.remove(spanTail.row);
			}

			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			layer = (layer + 1) % sections.length;
		}

		@Override
		public long vertexIndex() {
			return vertexIndex;
		}
		
		@Override
		public ITrellisIterator clone() {
			return new Iterator(layer, vertexIndex);
		}
	}
	
	private SpanForm spanForm;
	private TrellisSection sections[];

	private Logger logger;
	
	public BlockCodeTrellis(SpanForm spanForm) {
		logger = LoggerFactory.getLogger(this.getClass());
		
		this.spanForm = spanForm;
		
		int k = spanForm.Matr.getRowCount(), n = spanForm.Matr.getColumnCount();
		logger.debug("Construction of trellis for " + k + "/" + n + " code");
		
		ArrayList<TrellisSection> sectionsArray = TrellisUtils.buildSections(spanForm);
		if (!spanForm.IsTailbiting) {
			TrellisSection lastSection = new TrellisSection();
			Boundary dummyBoundary = new Boundary(-1, n);
			lastSection.spanHeads.add(dummyBoundary);
			sectionsArray.add(lastSection); // добавляем последний слой с фиктивной границей
		}

		for (TrellisSection layer2 : sectionsArray) {
			logger.debug(layer2.toString());
		}
		
		sections = sectionsArray.toArray(new TrellisSection[sectionsArray.size()]);
		
		logger.debug("Layers count is " + sections.length);
	}
	
	@Override
	public ITrellisIterator iterator(int layer, int vertexIndex) {
		if (layer >= layersCount() || vertexIndex >= layerSize(layer)) {
			throw new IndexOutOfBoundsException(layer + ", " + vertexIndex);
		}
		
		return new Iterator(layer, vertexIndex);
	}

	@Override
	public long layerSize(int layer) {
		return 1 << layerComplexity(layer);
	}
	
	public int layerComplexity(int layer) {
		int column = sections[layer].beginColumn();
		int activeRowsCount = 0;
		for (int row = 0; row < spanForm.Matr.getRowCount(); ++row) {
			if (spanForm.isRowActiveBefore(column, row)) {
				++activeRowsCount;
			}
		}
		
		return activeRowsCount;
	}

	@Override
	public int layersCount() {
		return sections.length;
	}
}
