package math;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Спеновая форма порождающей матрицы сверточного кода.
 * Порождающая матрица хранится в виде массива матриц - разложение по степеням.
 * Порождающая матрица должна быть в minimal-base форме.
 * @author stas
 *
 */
public class ConvCodeSpanForm implements ISpanForm {
	public class SpanFormException extends Exception {
		public SpanFormException(String message) {
			super(message);
		}
	}
	
	public class MatrixNotInSpanForm extends SpanFormException {

		public MatrixNotInSpanForm(String message) {
			super(message);
		}

	}

	/**
	 * Разложение по степеням порождающей матрицы сверточного кода, записанное в столбик. 
	 */
	public Matrix matrix;
	/**
	 * Задержка кодера (максимальная степень полинома)
	 */
	public int delay;
	/**
	 * degrees[i] для i = 1..b - это наибольшая степень в i-том ряду при разложении по степеням.
	 * Т.е. matrix.getRow(degrees[i]*b+i) содержит единицы, а matrix.getRow((degrees[i] + k)*b+i), где k > 0, уже нет.  
	 */
	public int degrees[];
	/**
	 * Начала строк в матрице matrices[0] 
	 */
	public int spanHeads[];
	/**
	 * Концы строк в матрицах matrices[degrees[i]] 
	 */
	public int spanTails[];

	public ConvCodeSpanForm(Matrix matrix, int degrees[], int spanHeads[], int spanTails[]) throws SpanFormException {
		this.matrix = matrix;
		this.degrees = degrees;
		this.spanHeads = spanHeads;
		this.spanTails = spanTails;
		
		delay = matrix.getRowCount() / degrees.length - 1;

		checkSpanForm();
	}

	// методы интерфейса ISpanForm
	@Override
	public int getRowCount() {
		return degrees.length;
	}

	@Override
	public int getHead(int row) {
		return spanHeads[row];
	}

	@Override
	public int getTail(int row) {
		return spanTails[row];
	}

	/**
	 * Проверяет, является ли ряд <code>row</code> активным для яруса, 
	 * расположенного <b>перед</b> столбцом <code>column</code>.
	 * 
	 * @param column столбец, активность ряда перед которым проверяется
	 * @param row проверяемый ряд
	 * @return true, если ряд активен.
	 */
	public boolean isRowActiveBefore(int column, int row) {
		int b = getRowCount();
		if (row < b) {
			return spanHeads[row] < column;
		}
		
		int degree = row / b;
		row = row % b;
		if (degrees[row] < degree) {
			return false;
		}
		if (degrees[row] > degree) {
			return true;
		}
		
		return column <= spanTails[row]; 
	}

	/**
	 * Проверяет, является ли ряд <code>row</code> активным для яруса, 
	 * расположенного <b>после</b> столбца <code>column</code>.
	 * 
	 * @param column столбец, активность ряда после которого проверяется
	 * @param row проверяемый ряд
	 * @return true, если ряд активен.
	 */
	public boolean isRowActiveAfter(int column, int row) {
		int b = getRowCount();
		if (row < b) {
			return spanHeads[row] <= column;
		}
		
		int degree = row / b;
		row = row % b;
		if (degrees[row] < degree) {
			return false;
		}
		if (degrees[row] > degree) {
			return true;
		}

		return column < spanTails[row]; 
	}
	
	/**
	 * Проверяет корректность спеновой формы. 
	 * @throws MatrixNotInSpanForm 
	 */
	private void checkSpanForm() throws SpanFormException {
		if ((matrix.getRowCount() % degrees.length) != 0 || 
				degrees.length != spanHeads.length ||
				degrees.length != spanTails.length) {
			throw new MatrixNotInSpanForm("Wrong sizes of input matrices or arrays.");
		}

		int b = getRowCount();
		for (int i = 0; i < b; ++i) {
			if (matrix.getRow(i).nextSetBit(0) != spanHeads[i]) {
				throw new MatrixNotInSpanForm("Span heads don't correspond to matrices.");
			}
			if (matrix.getRow(degrees[i] * b + i).previousSetBit(matrix.getColumnCount() - 1) != spanTails[i]) {
				throw new MatrixNotInSpanForm("Span tails don't correspond to matrices.");
			}
		}
	}

	public SortedSet<Integer> getActiveRowsBefore(int column) {
		SortedSet<Integer> activeRows = new TreeSet<Integer>();

		for (int row = 0; row < matrix.getRowCount(); ++row) {
			if (isRowActiveBefore(column, row)) {
				activeRows.add(row);
			}
		}
		
		return activeRows;
	}
}
