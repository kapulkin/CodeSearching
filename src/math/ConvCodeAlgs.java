package math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smith_decomposition.SmithDecomposition;
import trellises.Trellis;
import trellises.TrellisSection;
import trellises.TrellisUtils;
import trellises.Trellis.Edge;
import trellises.Trellis.Vertex;
import trellises.TrellisSection.Boundary;

/**
 * Методы данного класса работают с порождающей матрицей сверточного кода. 
 * Часть методов применима к любой порождающей матрице, в то время, как другая 
 * работает корректно только с матрицей в minimal-base форме. 
 * @author stas
 *
 */
public class ConvCodeAlgs {
	private static Logger logger = LoggerFactory.getLogger(ConvCodeAlgs.class);
	/**
	 * Сортирует ряды матрицы и других элементов спеновой формы по возрастанию 
	 * начал строк матрицы <em>G' = {G<0, G1, .., Gm}</em>
	 * @param sf спеновая форма
	 */
	public static void sortHeads(ConvCodeSpanForm sf)
	{
		// сортировка выбором
		for(int i = 0;i < sf.getRowCount();i ++)
		{
			int minHead = sf.getHead(i);
			int minRow = i;
			
			for (int j = i + 1; j < sf.getRowCount(); ++j)
			{
				if(sf.getHead(j) < minHead)
				{
					minHead = sf.getHead(j);
					minRow = j;
				}
			}
			
			if (i != minRow)
			{
				for (int degree = 0; degree < sf.matrices.length; ++degree) {
					BitArray iRow = sf.matrices[degree].getRow(i);
					sf.matrices[degree].setRow(i, sf.matrices[degree].getRow(minRow));
					sf.matrices[degree].setRow(minRow, iRow);
				}

				int idegree = sf.degrees[i];
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.degrees[i] = sf.degrees[minRow];
				sf.degrees[minRow] = idegree;
				
				sf.setHead(i, sf.getHead(minRow));
				sf.setHead(minRow, iHead);
				
				sf.setTail(i, sf.getTail(minRow));
				sf.setTail(minRow, iTail);
			}
		}
	}
	
	public static void sortTails(ConvCodeSpanForm sf)
	{
		// сортировка выбором
		for(int i = 0;i < sf.getRowCount();i ++)
		{
			int minTail = sf.degrees[i] * sf.matrices[0].getColumnCount() + sf.getTail(i);
			int minRow = i;
			
			for (int j = i + 1; j < sf.getRowCount(); ++j)
			{
				if(sf.getTail(j) < minTail)
				{
					minTail = sf.getTail(j);
					minRow = j;
				}
			}
			
			if (i != minRow)
			{
				for (int degree = 0; degree < sf.matrices.length; ++degree) {
					BitArray iRow = sf.matrices[degree].getRow(i);
					sf.matrices[degree].setRow(i, sf.matrices[degree].getRow(minRow));
					sf.matrices[degree].setRow(minRow, iRow);
				}

				int idegree = sf.degrees[i];
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.degrees[i] = sf.degrees[minRow];
				sf.degrees[minRow] = idegree;

				sf.setHead(i, sf.getHead(minRow));
				sf.setHead(minRow, iHead);
				
				sf.setTail(i, sf.getTail(minRow));
				sf.setTail(minRow, iTail);
			}
		}
	}

	/**
	 * Упорядочивает ряды матрицы по возрастанию степеней рядов v_i, i = 1..b.  
	 * @param matrix матрица полиномов.
	 */
	public static int[] sortRowsByDegree(PolyMatrix matrix) {
		int[] degrees = getRowDegrees(matrix);
		
		// сортировка выбором
		for (int i = 0; i < matrix.getRowCount(); ++i) {
			for (int j = i + 1; j < matrix.getRowCount(); ++j) {
				if (degrees[i] > degrees[j]) {
					// нарушен порядок, меняем местами
					int tmp = degrees[i];
					degrees[i] = degrees[j];
					degrees[j] = tmp;
					
					Poly tmpRow[] = matrix.getRow(i);
					matrix.setRow(i, matrix.getRow(j));
					matrix.setRow(j, tmpRow);
				}
			}
		}
		
		return degrees;
	}

	/** Вычисляет максимальные степени полиномов в каждом ряду матрицы 
	 * @param matrix матрица полиномов.
	 * @return степени рядов в матрице.
	 */
	public static int[] getRowDegrees(PolyMatrix matrix) {
		int degrees[] = new int[matrix.getRowCount()];
		for (int i = 0; i < matrix.getRowCount(); ++i) {
			degrees[i] = Integer.MIN_VALUE;
			for (int j = 0; j < matrix.getColumnCount(); ++j) {
				if (degrees[i] < matrix.get(i, j).getDegree()) {
					degrees[i] = matrix.get(i, j).getDegree();
				}
			}
		}
		return degrees;
	}

	public static int getHigherDegree(PolyMatrix matrix) {
		int degree = Integer.MIN_VALUE;
		for (int i = 0; i < matrix.getRowCount(); ++i) {
			for (int j = 0; j < matrix.getColumnCount(); ++j) {
				if (degree < matrix.get(i, j).getDegree()) {
					degree = matrix.get(i, j).getDegree();
				}
			}
		}
		
		return degree;
	}
	
	/**
	 * Строит представление матрицы полиномов G в виде {G_0, G_1, .., G_m}, где
	 * G = G_0 + G_1*D + ... + G_m*D^m, m - наибольшая степень полиномов G.
	 * 
	 * @param matrix матрица полиномов.
	 * @return разложение входной матрицы по степпеням.
	 */
	public static Matrix[] buildPowerDecomposition(PolyMatrix matrix) {
		int maxDegree = Integer.MIN_VALUE;

		for (int i = 0; i < matrix.getRowCount(); ++i) {
			for (int j = 0; j < matrix.getColumnCount(); ++j) {
				if (maxDegree < matrix.get(i, j).getDegree()) {
					maxDegree = matrix.get(i, j).getDegree();
				}
			}
		}

		logger.debug("maxDegree = " + maxDegree);
		
		Matrix [] powerDecomposition = new Matrix[maxDegree + 1];
		for (int i = 0; i < powerDecomposition.length; ++i) {
			powerDecomposition[i] = new Matrix(matrix.getRowCount(), matrix.getColumnCount());
		}
		
		for (int i = 0; i < matrix.getRowCount(); ++i) {
			for (int j = 0; j < matrix.getColumnCount(); ++j) {
				for (int pow = 0; pow <= matrix.get(i, j).getDegree(); ++pow) {
					powerDecomposition[pow].set(i, j, matrix.get(i, j).getCoeff(pow));
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Power decomposition:");
			for (int i = 0; i < matrix.getRowCount(); ++i) {
				String str = new String();
				for (Matrix matrix2 : powerDecomposition) {
					str += matrix2.getRow(i) + " ";
				}
				logger.debug(str);
			}
		}
		
		return powerDecomposition;
	}

	/**
	 * Строит полиномиальную матрицу по ее разложению по степеням.
	 * Можно сказать, что выполняет действие, обратное разложению по степеням.
	 * 
	 * @param polyDecomposition разложение матрицы полиномов по степеням
	 * @return матрица полиномов
	 */
	public static PolyMatrix buildPolyComposition(Matrix [] polyDecomposition) {
		PolyMatrix matrix = new PolyMatrix(polyDecomposition[0].getRowCount(), polyDecomposition[0].getColumnCount());
		for (int row = 0; row < matrix.getRowCount(); ++row) {
			for (int column = 0; column < matrix.getColumnCount(); ++column) {
				for (int power = 0; power < polyDecomposition.length; ++power) {
					matrix.get(row, column).setCoeff(power, polyDecomposition[power].get(row, column));
				}
			}
		}
		
		return matrix;
	}
	
	/**
	 * Строит спеновую форму порождающей матрицы сверточного кода в minimal-base форме.
	 * Спеновая форма включает в себя массив матриц - разложение полиномиальной по степеням, а так же индексы начал и концов строк.
	 * @param matrix порождающая матрица сверточного кода в minimal-base форме.
	 * @return спеновую форму порождающей матрицы сверточного кода
	 */
	public static ConvCodeSpanForm buildSpanForm(PolyMatrix matrix) {
		int degrees[] = sortRowsByDegree(matrix);
		Matrix matrices[] = buildPowerDecomposition(matrix);

		int spanHeads[] = new int [matrix.getRowCount()];
		Set<Integer> processedHeadRows = new TreeSet<Integer>();
		
		for (int col = 0; col < matrices[0].getColumnCount(); ++col) {
			for (int row = 0; row < matrices[0].getRowCount(); ++row) {
				if (processedHeadRows.contains(row)) {
					continue;
				}
				
				if (matrices[0].get(row, col)) {
					for (int lowerRow = row + 1; lowerRow < matrix.getRowCount(); ++lowerRow) {
						if (!processedHeadRows.contains(lowerRow) && matrices[0].get(lowerRow, col)) {
							addLongRow(matrices, lowerRow, row, degrees[row]);
						}
					}
					spanHeads[row] = col;
					processedHeadRows.add(row);
					break;
				}
			}
		}

		int spanTails[] = new int [matrix.getRowCount()];
		Set<Integer> processedTailRows = new TreeSet<Integer>();
		
		// выправляем хвосты только между рядами, имеющими одинаковую степень.
		Map<Integer, ArrayList<Integer>> degreeMap = new TreeMap<Integer, ArrayList<Integer>>();
		for (int row = matrix.getRowCount() - 1; row >= 0; --row) {
			if (!degreeMap.containsKey(degrees[row])) {
				degreeMap.put(degrees[row], new ArrayList<Integer>());
			}
			degreeMap.get(degrees[row]).add(row);
		}

		for (int degree : degreeMap.keySet()) {
			ArrayList<Integer> rows = degreeMap.get(degree);
			
			if (rows.size() == 1) {
				int row = rows.get(0);
				spanTails[row] = matrices[degree].getRow(row).previousSetBit(matrix.getColumnCount() - 1);
				processedTailRows.add(row);
				continue;
			}

			for (int col = matrices[0].getColumnCount() - 1; col >= 0; --col) {
				ArrayList<Integer> conflictedRows = new ArrayList<Integer>();
				// находим ряды, конфликтующие в данном столбце: т.е. ряды еще не использованы, и в них в данном столбце единица;
				// прибавляем ряд с наибольшим spanHeads[row] ко всем остальным.
				for (int row : rows) {
					if (processedTailRows.contains(row)) {
						continue;
					}
					
					if (matrices[degree].get(row, col)) {
						conflictedRows.add(row);
					}
				}
				int maxSpanHead = Integer.MIN_VALUE, maxSpanHeadRow = -1;
				for (int row : conflictedRows) {
					if (spanHeads[row] > maxSpanHead) {
						maxSpanHead = spanHeads[row];
						maxSpanHeadRow = row;
					}
				}
				
				for (int row : conflictedRows) {
					if (row != maxSpanHeadRow) {
						addLongRow(matrices, row, maxSpanHeadRow, degree);
					}
				}
				if (maxSpanHeadRow != -1) {
					spanTails[maxSpanHeadRow] = col;
					processedTailRows.add(maxSpanHeadRow);
				}
			}
		}		

		if (logger.isDebugEnabled()) {
			logger.debug("Span form:");
			for (int i = 0; i < matrix.getRowCount(); ++i) {
				String str = new String();
				for (Matrix matrix2 : matrices) {
					str += matrix2.getRow(i) + " ";
				}
				logger.debug(str);
			}
		}

		return new ConvCodeSpanForm(matrices, degrees, spanHeads, spanTails);
	}

	public static Trellis buildTrellis(ConvCodeSpanForm spanForm) {
		int b = spanForm.matrices[0].getRowCount();
		int c = spanForm.matrices[0].getColumnCount();
		int v = 0;	// overal constraint length
		for (int degree : spanForm.degrees) {
			v += degree;
		}
		
		logger.debug("Here is a (" + b + ", " + c + ", " + v + ") code.");
		
		// Формируем ряды, по которым дальше будем строить решетку. Фактически 
		// мы берем матрицы G0..Gm и выстраиваем их вертикально, исключая при 
		// этом нулевые ряды.
		BitArray rows[] = new BitArray[v + b];
		// Номера рядов в rows, в которых находятся ряды из Gi со старшими степенями.
		Map<Integer, Integer> tailsRows = new TreeMap<Integer, Integer>();

		int rowIndex = 0;
		for (int degree = 0; degree < spanForm.matrices.length; ++degree) {
			for (int row = 0; row < b; ++row) {
				if (spanForm.degrees[row] >= degree) {
					if (spanForm.degrees[row] == degree) {
						tailsRows.put(row, rowIndex);
					}
					rows[rowIndex++] = spanForm.matrices[degree].getRow(row);
				}
			}
		}
		

		TrellisSection[] sections = TrellisUtils.buildSections(spanForm).toArray(new TrellisSection[0]);

		for (TrellisSection section : sections) {
			logger.debug(section.toString());
		}
		
		// строим решетку для всех ярусов.
		Trellis trellis = new Trellis();
		trellis.Layers = new Trellis.Vertex[sections.length][];
		// сначала активны ряды всех матриц положительной степени.
		SortedSet<Integer> activeRows = new TreeSet<Integer>();
		for (int row = b; row < v + b; ++row) {
			activeRows.add(row);
		}
		logger.debug("active rows: " + activeRows);
		for (int layer = 0; layer < trellis.Layers.length; ++layer) {
			trellis.Layers[layer] = new Trellis.Vertex[1 << activeRows.size()];

			SortedSet<Integer> nextActiveRows = new TreeSet<Integer>(activeRows);
			for (Boundary head : sections[layer].spanHeads) {
				nextActiveRows.add(head.row);
			}
			for (Boundary tail : sections[layer].spanTails) {
				nextActiveRows.remove(tailsRows.get(tail.row));
			}

			logger.debug("next active rows: " + nextActiveRows);

			// [from, to) - полинтервал, соответствующий текущему сегменту.
			int from = sections[layer].beginColumn();
			int to = (layer == sections.length - 1) ? c : sections[layer + 1].beginColumn();
			BitArray sum = new BitArray(to - from);

			Integer activeRowsArray[] = activeRows.toArray(new Integer[activeRows.size()]);
			Set<Integer> sumRows = new HashSet<Integer>();
			for (int i = 0; i < trellis.Layers[layer].length; ++i) {
				int vertexIndex = GrayCode.getWord(i);
				
				trellis.Layers[layer][vertexIndex] = new Vertex();

				if (i > 0) {
					int row = activeRowsArray[GrayCode.getChangedPosition(i)]; // ряд, который добавился или удалился
					if (GrayCode.getChangedBit(i)) {
						sumRows.add(row); // ряд добавился
					} else {
						sumRows.remove(row); // ряд удалился
					}
					sum.xor(rows[row].get(from, to)); // прибавляем/вычитаем ряд
				}
				
				Edge edges[] = new Edge[1 << sections[layer].spanHeads.size()];
				for (int j = 0; j < edges.length; ++j) {
					edges[j] = new Edge();
					edges[j].Src = vertexIndex;
					edges[j].Bits = (BitArray) sum.clone();
					for (int bit = 0; bit < sections[layer].spanHeads.size(); ++bit) {
						if ((j & (1 << bit)) != 0) {
							int row = sections[layer].spanHeads.get(bit).row;
							edges[j].Bits.xor(rows[row].get(from, to));
							sumRows.add(row);
						}
					}
					long dstLong = TrellisUtils.getVertexIndex(sumRows, nextActiveRows);
					if (dstLong < 0 || dstLong > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Dst index is not in [0, " + Integer.MAX_VALUE + "]: " +
								layer + ", " + vertexIndex + ", " + j);
					}
					edges[j].Dst = (int)dstLong;
					for (int bit = 0; bit < sections[layer].spanHeads.size(); ++bit) {
						if ((j & (1 << bit)) != 0) {
							int row = sections[layer].spanHeads.get(bit).row;
							sumRows.remove(row);
						}
					}
				}
				trellis.Layers[layer][vertexIndex].Accessors = edges;
			}

			activeRows = nextActiveRows;
		}
		
		// строим обратные ребра.
		TrellisUtils.buildPredcessors(trellis);

		return trellis;
	}

	private static void addLongRow(Matrix[] matrices, int dest, int source,
			int degreeRestriction) {
		for (int i = 0; i <= degreeRestriction; ++i) {
			for (int j = 0; j < matrices[i].getColumnCount(); ++j) {
				boolean sum = matrices[i].get(dest, j) ^ matrices[i].get(source, j);
				matrices[i].set(dest, j, sum);
			}
		}
	}

	/**
	 * Приводит порождающую матрицу сверточного кода к минимальной форме
	 * @param matr порождающая матрица сверточного кода
	 */
	public static void toMinimalForm(PolyMatrix matr)
	{
		while(decreaseConstraint(matr)) {}
	}

	/**
	 * Строит порождающую матрицу сверточного кода в minimal base форме по декомпозиции Смита  
	 * @param decomposition декомпозиция Смита
	 * @return порождающая матрица сверточного кода в minimal base форме
	 */
	public static PolyMatrix getMinimalBaseGenerator(SmithDecomposition decomposition) {
		PolyMatrix D = decomposition.getD();
		
		PolyMatrix minBaseG = new PolyMatrix(D.getRowCount(), D.getColumnCount());
		for (int i = 0; i < minBaseG.getRowCount(); ++i) {
			for (int j = 0; j < minBaseG.getColumnCount(); ++j) {
				minBaseG.set(i, j, decomposition.getB().get(i, j));
			}
		}
		
		toMinimalForm(minBaseG);
		
		return minBaseG;
	}

	public static PolyMatrix getMinimalBaseGenerator(PolyMatrix G) {
		return getMinimalBaseGenerator(new SmithDecomposition(G));
	}

	/**
	 * Строит ортогональную матрицу в minimal base форме по декомпозиции Смита  
	 * @param decomposition декомпозиция Смита
	 * @return ортогональная матрица в minimal base форме
	 */
	public static PolyMatrix getOrthogonalMatrix(SmithDecomposition decomp)
	{
		PolyMatrix ort = new PolyMatrix(decomp.getD().getColumnCount() - decomp.getD().getRowCount(), decomp.getD().getColumnCount());
		
		for(int i = 0;i < ort.getRowCount();i ++)
		{
			for(int j = 0;j < ort.getColumnCount();j ++)
			{
				ort.set(i, j, decomp.getInvB().get(j, i + decomp.getD().getRowCount()));
			}
		}
		
		return ort;
	}

	static private boolean decreaseConstraint(PolyMatrix matr)
	{
		Matrix highestDegreesMatr = new Matrix(matr.getRowCount(), matr.getColumnCount());
		int[] highestDegrees = new int[matr.getRowCount()];
		
		for(int i = 0;i < matr.getRowCount();i ++)
		{			
			int maxDeg = Integer.MIN_VALUE;
			
			for(int j = 0;j < matr.getColumnCount();j ++)
			{
				int deg = matr.get(i, j).getDegree();
				
				if(deg > maxDeg)
				{
					maxDeg = deg;
				}
			}
			
			highestDegrees[i] = maxDeg;
			
			for(int j = 0;j < matr.getColumnCount();j ++)
			{
				int deg = matr.get(i, j).getDegree();
				
				if(deg == maxDeg)
				{
					highestDegreesMatr.set(i, j, true);
				}else{
					highestDegreesMatr.set(i, j, false);
				}
			}
		}
		
		int[] dependentRows = MathAlgs.findDependentRows(highestDegreesMatr);
		
		if(dependentRows.length < 2)
		{
			return false;
		}
		
		int maxDegInd = -1;
		int maxDeg = Integer.MIN_VALUE;
		
		for(int i = 0;i < dependentRows.length;i ++)
		{
			if(highestDegrees[dependentRows[i]] > maxDeg)
			{
				maxDeg = highestDegrees[dependentRows[i]];
				maxDegInd = i;
			}
		}
		
		for(int i = 0;i < dependentRows.length;i ++)
		{
			if(i == maxDegInd)
			{
				continue;
			}
			
			for(int j = 0;j < matr.getColumnCount();j ++)
			{
				Poly summand = matr.get(i, j).mulPow(maxDeg - highestDegrees[dependentRows[i]]);
				matr.set(maxDegInd, j, matr.get(maxDegInd, j).sum(summand));
			}
		}
		
		return true;
	}
}