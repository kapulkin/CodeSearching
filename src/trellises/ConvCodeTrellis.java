package trellises;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import math.BitArray;
import math.ConvCodeSpanForm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.TrellisSection.Boundary;

public class ConvCodeTrellis  implements ITrellis {
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
			currentSum = TrellisUtils.getEdgeBits(spanForm.matrix, currentSumRows, 0, spanForm.matrix.getColumnCount());
		}
		
		@Override
		public ITrellisEdge[] getAccessors() {
			// получаем следующие активные ряды: добавляем к текущим начавшийся активный ряд и удаляем завершившийся
			for (Boundary spanHead : sections[layer].spanHeads) {
				currentActiveRows.add(spanHead.row);
			}
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.remove(spanTail.row);
			}

			int nextLayer = (layer + 1) % sections.length;
			
			int fromIndex = (layer == 0) ? 0 : sections[layer].beginColumn();
			int toIndex = (nextLayer == 0) ? c : sections[nextLayer].beginColumn();
			ITrellisEdge edges[] = TrellisUtils.buildAccessorsEdges(spanForm.matrix, vertexIndex, 
					currentSum, currentSumRows, currentActiveRows, 
					sections[layer].spanHeads, fromIndex, toIndex);

			// восстанавливаем значение текущих активных рядов
			for (Boundary spanHead : sections[layer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
			}
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.add(spanTail.row);
			}
			
			return edges;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			int prevLayer = (layer - 1 + sections.length) % sections.length;
			
			SortedSet<Integer> activeRows;
			Set<Integer> sumRows;
			BitArray sum;
			
			if (layer == 0) {
				// предыдущий слой вычисляется на предыдущем блоке матриц, осуществляем сдвиг.
				activeRows = new TreeSet<Integer>();
				for (Integer row : currentActiveRows) {
					activeRows.add(row - b);
				}
				sumRows = new HashSet<Integer>();
				for (Integer row : currentSumRows) {
					sumRows.add(row - b);
				}
				sum = TrellisUtils.getEdgeBits(spanForm.matrix, sumRows, 0, spanForm.matrix.getColumnCount());
			} else {
				activeRows = currentActiveRows;
				sumRows = currentSumRows;
				sum = currentSum;
			}

			// получаем предыдущие активные ряды
			for (Boundary spanTail : sections[prevLayer].spanTails) {
				activeRows.add(spanTail.row);
			}
			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				activeRows.remove(spanHead.row);
			}

			int fromIndex = (prevLayer == 0) ? 0 : sections[prevLayer].beginColumn();
			int toIndex = (layer == 0) ? c : sections[layer].beginColumn();
			ITrellisEdge edges[] = TrellisUtils.buildPredcessorsEdges(spanForm.matrix, vertexIndex, 
					sum, sumRows, activeRows, 
//					sections[prevLayer].spanTails, sections[prevLayer].beginColumn(), sections[layer].beginColumn()); 
					sections[prevLayer].spanTails, fromIndex, toIndex); 

			// восстанавливаем значение текущих активных рядов
			for (Boundary spanTail : sections[prevLayer].spanTails) {
				activeRows.remove(spanTail.row);
			}
			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				activeRows.add(spanHead.row);
			}
			
			for (int i = 0; i < edges.length; ++i) {
				logger.debug("edge " + i + ": " + edges[i]);
			}

			return edges;
		}

		@Override
		public boolean hasBackward() {
			return true;
		}

		@Override
		public boolean hasForward() {
			return true;
		}

		@Override
		public void moveBackward(int edgeIndex) throws NoSuchElementException {
			int prevLayer = (layer - 1 + sections.length) % sections.length;
			
			if (edgeIndex < 0 || edgeIndex >= (1 << sections[prevLayer].spanTails.size())) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

			if (layer == 0) {
				TreeSet<Integer> shiftedActiveRows = new TreeSet<Integer>();
				for (Integer row : currentActiveRows) {
					shiftedActiveRows.add(row - b);
				}
				currentActiveRows = shiftedActiveRows;

				HashSet<Integer> shiftedSumRows = new HashSet<Integer>();
				for (Integer row : currentSumRows) {
					shiftedSumRows.add(row - b);
				}
				currentSumRows = shiftedSumRows;

				currentSum = TrellisUtils.getEdgeBits(spanForm.matrix, currentSumRows, 0, spanForm.matrix.getColumnCount());
			}
			
			for (int i = 0; i < sections[prevLayer].spanTails.size(); i++) {
				Boundary spanTail = sections[prevLayer].spanTails.get(i);

				currentActiveRows.add(spanTail.row);
				if ((edgeIndex & (1 << i)) != 0) {
					currentSumRows.add(spanTail.row);
					currentSum.xor(spanForm.matrix.getRow(spanTail.row));
				}
			}

			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				currentActiveRows.remove(spanHead.row);
				if (currentSumRows.contains(spanHead.row)) {
					currentSum.xor(spanForm.matrix.getRow(spanHead.row));
					currentSumRows.remove(spanHead.row);
				}
			}
			
			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			layer = prevLayer;
		}

		@Override
		public void moveForward(int edgeIndex) throws NoSuchElementException {
			if (edgeIndex < 0 || edgeIndex >= (1 << sections[layer].spanHeads.size())) {
				throw new IndexOutOfBoundsException("There is no edge with such index.");
			}

			for (int i = 0; i < sections[layer].spanHeads.size(); ++i) {
				Boundary spanHead = sections[layer].spanHeads.get(i);
				
				currentActiveRows.add(spanHead.row);
				if ((edgeIndex & (1 << i)) != 0) {
					currentSumRows.add(spanHead.row);
					currentSum.xor(spanForm.matrix.getRow(spanHead.row));
				}
			}
			
			for (Boundary spanTail : sections[layer].spanTails) {
				currentActiveRows.remove(spanTail.row);
				if (currentSumRows.contains(spanTail.row)) {
					currentSum.xor(spanForm.matrix.getRow(spanTail.row));
					currentSumRows.remove(spanTail.row);
				}
			}

			vertexIndex = TrellisUtils.getVertexIndex(currentSumRows, currentActiveRows);

			layer = (layer + 1) % sections.length;
			
			if (layer == 0) {
				TreeSet<Integer> shiftedActiveRows = new TreeSet<Integer>();
				for (Integer row : currentActiveRows) {
					shiftedActiveRows.add(row + b);
				}
				currentActiveRows = shiftedActiveRows;

				HashSet<Integer> shiftedSumRows = new HashSet<Integer>();
				for (Integer row : currentSumRows) {
					shiftedSumRows.add(row + b);
				}
				currentSumRows = shiftedSumRows;

				currentSum = TrellisUtils.getEdgeBits(spanForm.matrix, currentSumRows, 0, spanForm.matrix.getColumnCount());
			}
		}

		@Override
		public int layer() {
			return layer;
		}

		@Override
		public long vertexIndex() {
			return vertexIndex;
		}
		
		@Override
		public Iterator clone() {
			return new Iterator(layer, vertexIndex);
		}		
	}
	
	private ConvCodeSpanForm spanForm;
	private int v;		// overal constraint length
	private int b, c;
	private TrellisSection sections[];

	private Logger logger;
	
	public ConvCodeTrellis(ConvCodeSpanForm spanForm) {
		logger = LoggerFactory.getLogger(this.getClass());

		this.spanForm = spanForm;
		v = 0;
		for (int i = 0; i < v; ++i) {
			v += spanForm.degrees[i];
		}
		
		b = spanForm.getRowCount();
		c = spanForm.matrix.getColumnCount();
		logger.debug("Construction of trellis for " + b + "/" + c + " code");

		ArrayList<TrellisSection> sectionsArray = TrellisUtils.buildSections(spanForm);
		for (TrellisSection section : sectionsArray) {
			for (Boundary spanTail : section.spanTails) {
				spanTail.row += b * spanForm.degrees[spanTail.row];
			}
		}
		
		for (TrellisSection layer2 : sectionsArray) {
			logger.debug(layer2.toString());
		}
		
		sections = sectionsArray.toArray(new TrellisSection[sectionsArray.size()]);
		
		logger.debug("Layers count is " + sections.length);
	}

	@Override
	public ITrellisIterator iterator(int layer, int vertexIndex) {
		return new Iterator(layer, vertexIndex);
	}

	@Override
	public long layerSize(int layer) {
		return 1 << layerComplexity(layer);
	}

	public int layerComplexity(int layer) {
		int column = sections[layer].beginColumn();
		int activeRowsCount = 0;
		for (int row = 0; row < spanForm.matrix.getRowCount(); ++row) {
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
