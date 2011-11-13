package trellises;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

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
		/**
		 * Кодовое слово, полученное, как сумма рядов из currentSumRows
		 */
		BitArray currentSum;
		
		Iterator(int layer, long vertexIndex) {
			logger = LoggerFactory.getLogger(this.getClass());
			
			this.layer = layer;
			this.vertexIndex = vertexIndex;
		
			currentActiveRows = spanForm.getActiveRowsBefore(sections[layer].beginColumn());
			currentSumRows = TrellisUtils.getSumRows(vertexIndex, currentActiveRows);
			currentSum = TrellisUtils.getEdgeBits(spanForm.Matr, currentSumRows, 0, spanForm.Matr.getColumnCount());
		}

		@Override
		public ITrellisEdge[] getAccessors() {
			if (!hasForward()) {
				return new LongEdge[0];
			}

			// получаем следующие активные ряды: добавляем к текущим начавшийся активный ряд и удаляем завершившийся
			for (Boundary spanHead : sections[layer].spanHeads) {
				currentActiveRows.add(spanHead.row);
			}
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.remove(spanTail.row);
			}

			int nextLayer = (layer + 1) % sections.length;
			
			ITrellisEdge edges[] = TrellisUtils.buildAccessorsEdges(spanForm.Matr, vertexIndex, 
					currentSum, currentSumRows, currentActiveRows,
					sections[layer].spanHeads, sections[layer].beginColumn(), sections[nextLayer].beginColumn());
			
			// восстанавливаем значение текущих активных рядов
			for (Boundary spanHead : sections[layer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
			}
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.add(spanTail.row);
			}

			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}
			
			return edges;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			if (!hasBackward()) {
				return new LongEdge[0];
			}
			
			int prevLayer = (layer - 1 + sections.length) % sections.length;
			
			// получаем предыдущие активные ряды
			for (Boundary spanTail : sections[prevLayer].spanTails) {
				currentActiveRows.add(spanTail.row);
			}
			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
			}
			
			ITrellisEdge edges[] = TrellisUtils.buildPredcessorsEdges(spanForm.Matr, vertexIndex, 
					currentSum, currentSumRows, currentActiveRows, 
					sections[prevLayer].spanTails, sections[prevLayer].beginColumn(), sections[layer].beginColumn());
			
			// восстанавливаем значение текущих активных рядов
			for (Boundary spanTail : sections[prevLayer].spanTails) {
				currentActiveRows.remove(spanTail.row);
			}
			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				currentActiveRows.add(spanHead.row);
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
			return layer < sections.length - 1;
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

			for (int i = 0; i < sections[prevLayer].spanTails.size(); ++i) {
				Boundary spanTail = sections[prevLayer].spanTails.get(i);
				
				currentActiveRows.add(spanTail.row);
				if ((edgeIndex & (1 << i)) != 0) {
					currentSumRows.add(spanTail.row);
					currentSum.xor(spanForm.Matr.getRow(spanTail.row));
				}
			}

			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
				if (currentSumRows.contains(spanHead.row)) {
					currentSum.xor(spanForm.Matr.getRow(spanHead.row));
					currentSumRows.remove(spanHead.row);
				}
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

			for (int i = 0; i < sections[layer].spanHeads.size(); ++i) {
				Boundary spanHead = sections[layer].spanHeads.get(i);
				
				currentActiveRows.add(spanHead.row);
				if ((edgeIndex & (1 << i)) != 0) {
					currentSumRows.add(spanHead.row);
					currentSum.xor(spanForm.Matr.getRow(spanHead.row));
				}
			}
			
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.remove(spanTail.row);
				if (currentSumRows.contains(spanTail.row)) {
					currentSum.xor(spanForm.Matr.getRow(spanTail.row));
					currentSumRows.remove(spanTail.row);
				}
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

		TrellisSection lastSection = new TrellisSection();
		Boundary dummyBoundary = new Boundary(-1, n);
		lastSection.spanHeads.add(dummyBoundary);
		sectionsArray.add(lastSection); // добавляем последний слой с фиктивной границей

		for (TrellisSection layer2 : sectionsArray) {
			logger.debug(layer2.toString());
		}
		
		sections = sectionsArray.toArray(new TrellisSection[sectionsArray.size()]);
		
		logger.debug("Layers count is " + sections.length);
	}
	
	@Override
	public ITrellisIterator iterator(int layer, long vertexIndex) {
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
