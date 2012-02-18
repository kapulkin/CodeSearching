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

import codes.ConvCode;
import codes.TBCode;
import codes.TruncatedCode;
import codes.ZTCode;

import trellises.IntEdge;
import trellises.Trellis;
import trellises.TrellisSection;
import trellises.TrellisUtils;
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
		int b = sf.getRowCount();
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
				for (int degree = 0, row = i, mrow = minRow; degree <= sf.delay; ++degree, row += b, mrow += b) {
					BitArray iRow = sf.matrix.getRow(row);
					sf.matrix.setRow(row, sf.matrix.getRow(mrow));
					sf.matrix.setRow(mrow, iRow);
				}

				int idegree = sf.degrees[i];
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.degrees[i] = sf.degrees[minRow];
				sf.degrees[minRow] = idegree;
				
				sf.spanHeads[i] = sf.getHead(minRow);
				sf.spanHeads[minRow] = iHead;
				
				sf.spanTails[i] = sf.getTail(minRow);
				sf.spanTails[minRow] = iTail;
			}
		}
	}
	
	public static void sortTails(ConvCodeSpanForm sf)
	{
		int b = sf.getRowCount();

		// сортировка выбором
		for(int i = 0;i < sf.getRowCount();i ++)
		{
			int minTail = sf.degrees[i] * sf.matrix.getColumnCount() + sf.getTail(i);
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
				for (int degree = 0, row = i, mrow = minRow; degree <= sf.delay; ++degree, row += b, mrow += b) {
					BitArray iRow = sf.matrix.getRow(row);
					sf.matrix.setRow(row, sf.matrix.getRow(mrow));
					sf.matrix.setRow(mrow, iRow);
				}

				int idegree = sf.degrees[i];
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.degrees[i] = sf.degrees[minRow];
				sf.degrees[minRow] = idegree;

				sf.spanHeads[i] = sf.getHead(minRow);
				sf.spanHeads[minRow] = iHead;
				
				sf.spanTails[i] = sf.getTail(minRow);
				sf.spanTails[minRow] = iTail;
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

	public static int getHighestDegree(PolyMatrix matrix) {
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
		int maxDegree = getHighestDegree(matrix);

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
		Map<Integer, SortedSet<Integer>> degreeMap = new TreeMap<Integer, SortedSet<Integer>>();
		for (int row = matrix.getRowCount() - 1; row >= 0; --row) {
			if (!degreeMap.containsKey(degrees[row])) {
				degreeMap.put(degrees[row], new TreeSet<Integer>());
			}
			degreeMap.get(degrees[row]).add(row);
		}

		for (Integer degree : degreeMap.keySet()) {
			SortedSet<Integer> rows = degreeMap.get(degree);
			
			if (rows.size() == 1) {
				Integer row = rows.first();
				spanTails[row] = matrices[degree].getRow(row).previousSetBit(matrix.getColumnCount() - 1);
				processedTailRows.add(row);
				continue;
			}

			for (int col = matrices[0].getColumnCount() - 1; col >= 0; --col) {
				// находим конфликтующие строки, прибавляем строку с наибольшим spanHead ко всем остальным
				ArrayList<Integer> conflicted = new ArrayList<Integer>();
				int maxSpanHeadRow = -1;
				for (Integer row : rows) {
					if (processedTailRows.contains(row)) {
						continue;
					}
					
					if (matrices[degree].get(row, col)) {
						conflicted.add(row);
						processedTailRows.add(row);
						if (maxSpanHeadRow == -1) {
							maxSpanHeadRow = row;
						} else if (spanHeads[row] > spanHeads[maxSpanHeadRow]){
							maxSpanHeadRow = row;
						}
					}
				}
				for (Integer row : conflicted) {
					if (row == maxSpanHeadRow) {
						continue;
					}
					addLongRow(matrices, row, maxSpanHeadRow, degree);
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

		BitArray rows[] = new BitArray[matrices[0].getRowCount() * matrices.length];
		for (int degree = 0, row = 0; degree < matrices.length; ++degree) {
			for (int i = 0; i < matrices[0].getRowCount(); ++i, ++row) {
				rows[row] = matrices[degree].getRow(i);
			}
		}
		
		return new ConvCodeSpanForm(new Matrix(rows), degrees, spanHeads, spanTails);
	}

	/**
	 * Строит секционированную решетку сверточного кода в явном виде.
	 * 
	 * @param spanForm спеновая форма для сверточного кода
	 * @return секционированная решетка сверточного кода
	 */
	public static Trellis buildTrellis(ConvCodeSpanForm spanForm) {
		int b = spanForm.getRowCount();
		int c = spanForm.matrix.getColumnCount();
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
		for (int degree = 0; degree <= spanForm.delay; ++degree) {
			for (int row = 0; row < b; ++row) {
				if (degree <= spanForm.degrees[row]) {
					if (spanForm.degrees[row] == degree) {
						tailsRows.put(row, rowIndex);
					}
					rows[rowIndex++] = spanForm.matrix.getRow(degree * b + row);
				}
			}
		}
		

		TrellisSection[] sections = TrellisUtils.buildSections(spanForm).toArray(new TrellisSection[0]);

		logger.debug("sections:");
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

			// [from, to) - полуинтервал, соответствующий текущему сегменту.
			int from = (layer == 0) ? 0 : sections[layer].beginColumn();
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
				
				IntEdge edges[] = new IntEdge[1 << sections[layer].spanHeads.size()];
				for (int j = 0; j < edges.length; ++j) {
					edges[j] = new IntEdge();
					edges[j].metrics = new int[0];

					edges[j].src = vertexIndex;
					edges[j].bits = sum.clone();
					for (int bit = 0; bit < sections[layer].spanHeads.size(); ++bit) {
						if ((j & (1 << bit)) != 0) {
							int row = sections[layer].spanHeads.get(bit).row;
							edges[j].bits.xor(rows[row].get(from, to));
							sumRows.add(row);
						}
					}
					long dstLong = TrellisUtils.getVertexIndex(sumRows, nextActiveRows);
					if (dstLong < 0 || dstLong > Integer.MAX_VALUE) {
						throw new IllegalArgumentException("Dst index is not in [0, " + Integer.MAX_VALUE + "]: " +
								layer + ", " + vertexIndex + ", " + j);
					}
					edges[j].dst = (int)dstLong;
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
	public static PolyMatrix getOrthogonalMatrix(SmithDecomposition decomp) {
		if (!decomp.isBasic()) {
			throw new IllegalArgumentException("Impossible to find orthogonal matrix because matrix is not basic!");
		}
		
		int k = decomp.getD().getRowCount();
		int n = decomp.getD().getColumnCount();
		int r = n - k;
		PolyMatrix ort = new PolyMatrix(r, n);
		PolyMatrix invB = decomp.getInvB();
		PolyMatrix gamma = decomp.getD();
		
		for (int i = 0; i < n;i ++) {
			for (int j = 0; j < r;j ++) {
				Poly sum = new Poly();
				
				for (int l = 0; l < k; ++l) {
					sum.add(invB.get(i, l).mul(gamma.get(l, j)));
				}
				
				sum.add(invB.get(i, k + j));
				ort.set(j, i, sum);
			}			
		}
		
		return ort;
	}
	
	public static TruncatedCode truncate(int k, int n, ConvCode convCode) {
		int L, L0;
		
		if ((n % convCode.getN() != 0) ||  (k % convCode.getK() != 0)) {
			throw new IllegalArgumentException("cann't truncate conv code in such way");
		}
		
		L = n / convCode.getN();
		L0 = L - k / convCode.getK();
		
		if (L0 < 0) {
			throw new IllegalArgumentException("cann't truncate conv code in such way");
		}
		
		if (L0 == 0) {
			return new TBCode(convCode, L - (convCode.getDelay() + 1));
		}
		
		if (L0 == convCode.getDelay()) {
			return new ZTCode(convCode, L - (convCode.getDelay() + 1));
		}		
		
		return new TruncatedCode(convCode, L0, L - (convCode.getDelay() + 1));
	}

	static private boolean decreaseConstraint(PolyMatrix matr) {
		Matrix highestDegreesMatr = new Matrix(matr.getRowCount(), matr.getColumnCount());
		int[] highestDegrees = new int[matr.getRowCount()];
		
		for (int i = 0;i < matr.getRowCount(); ++i) {			
			int maxDeg = Integer.MIN_VALUE;
			
			for (int j = 0;j < matr.getColumnCount(); ++j) {
				int deg = matr.get(i, j).getDegree();
				
				if (deg > maxDeg) {
					maxDeg = deg;
				}
			}
			
			highestDegrees[i] = maxDeg;
			
			for (int j = 0;j < matr.getColumnCount(); ++j) {
				int deg = matr.get(i, j).isZero() ? -1 : matr.get(i, j).getDegree();
				
				if (deg == maxDeg) {
					highestDegreesMatr.set(i, j, true);
				}else{
					highestDegreesMatr.set(i, j, false);
				}
			}
		}
		
		int[] dependentRows = MathAlgs.findDependentRows(highestDegreesMatr);
		
		if(dependentRows.length < 2) {
			return false;
		}
		
		int maxDegInd = -1;
		int maxDeg = Integer.MIN_VALUE;
		
		for (int i = 0;i < dependentRows.length; ++i) {
			if (highestDegrees[dependentRows[i]] > maxDeg) {
				maxDeg = highestDegrees[dependentRows[i]];
				maxDegInd = i;
			}
		}
		
		for (int i = 0;i < dependentRows.length; ++i) {
			if (i == maxDegInd) {
				continue;
			}
			
			for (int j = 0;j < matr.getColumnCount(); ++j) {
				Poly summand = matr.get(i, j).mulPow(maxDeg - highestDegrees[dependentRows[i]]);
				matr.set(maxDegInd, j, matr.get(maxDegInd, j).sum(summand));
			}
		}
		
		return true;
	}
	 
}
