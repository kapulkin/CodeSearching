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
	static private Logger logger = LoggerFactory.getLogger(BlockCodeTrellis.class);
	static private Logger iterLogger = LoggerFactory.getLogger(Iterator.class);

	public class Iterator implements ITrellisIterator {
		
		private int layer;
		private long vertexIndex;
		
		/**
		 * Номера рядов, активных в текущем ярусе. Фактически, номера рядов, пересекающих ярус.
		 */
		SortedSet<Integer> currentActiveRows;
		/**
		 * Кодовое слово, полученное, как сумма рядов матрицы, соотвествующих единичным битам номера вершины
		 */
		BitArray currentSum;
		
		Iterator() {
		}
		
		Iterator(int layer, long vertexIndex) {
			this.layer = layer;
			this.vertexIndex = vertexIndex;
		
			currentActiveRows = spanForm.getActiveRowsBefore(sections[layer].beginColumn());
			Set<Integer> sumRows = TrellisUtils.getSumRows(vertexIndex, currentActiveRows);
			currentSum = TrellisUtils.getEdgeBits(spanForm.Matr, sumRows, 0, spanForm.Matr.getColumnCount());
		}

		@Override
		public ITrellisEdge[] getAccessors() {
			if (!hasForward()) {
				return new LongEdge[0];
			}


			int nextLayer = layer + 1;

			ITrellisEdge edges[] = TrellisUtils.buildAccessorsEdges(spanForm.Matr,
					vertexIndex, currentSum, currentActiveRows,
					sections[layer].spanHeads, sections[layer].spanTails, sections[layer].beginColumn(), sections[nextLayer].beginColumn());
			
			for (int i = 0; i < edges.length; ++i) {
				iterLogger.debug("edge " + i + ": " + edges[i]);
			}
			
			return edges;
		}

		@Override
		public ITrellisEdge[] getPredecessors() {
			if (!hasBackward()) {
				return new LongEdge[0];
			}
			
			int prevLayer = layer - 1;
			
			ITrellisEdge edges[] = TrellisUtils.buildPredcessorsEdges(spanForm.Matr,
					vertexIndex, currentSum, currentActiveRows,
					sections[prevLayer].spanHeads, sections[prevLayer].spanTails, sections[prevLayer].beginColumn(), sections[layer].beginColumn());
			
			for (int i = 0; i < edges.length; ++i) {
				iterLogger.debug("edge " + i + ": " + edges[i]);
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
				
				vertexIndex = TrellisUtils.getNextVertexByAdding(vertexIndex, currentActiveRows, spanTail.row, currentSum, spanForm.Matr, (edgeIndex & (1 << i)) != 0);
				currentActiveRows.add(spanTail.row);
			}

			for (Boundary spanHead : sections[prevLayer].spanHeads) {
				int position = currentActiveRows.headSet(spanHead.row).size();
				if ((vertexIndex & (1 << position)) != 0) {
					currentSum.xor(spanForm.Matr.getRow(spanHead.row));
				}
				vertexIndex = TrellisUtils.getNextVertexByRemoving(vertexIndex, currentActiveRows, spanHead.row, currentSum, spanForm.Matr);
				currentActiveRows.remove(spanHead.row);
			}

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
				
				vertexIndex = TrellisUtils.getNextVertexByAdding(vertexIndex, currentActiveRows, spanHead.row, (edgeIndex & (1 << i)) != 0);
				currentActiveRows.add(spanHead.row);
				if ((edgeIndex & (1 << i)) != 0) {
//					currentSumRows.add(spanHead.row);
					currentSum.xor(spanForm.Matr.getRow(spanHead.row));
				}
			}
			
			for (Boundary spanTail : sections[layer].spanTails) {
				vertexIndex = TrellisUtils.getNextVertexByRemoving(vertexIndex, currentActiveRows, spanTail.row, currentSum, spanForm.Matr);
				currentActiveRows.remove(spanTail.row);
			}

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

	public BlockCodeTrellis(SpanForm spanForm) {
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
