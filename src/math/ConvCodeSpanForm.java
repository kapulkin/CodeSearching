package math;

/**
 * Спеновая форма порождающей матрицы сверточного кода.
 * Порождающая матрица хранится в виде массива матриц - разложение по степеням.
 * Порождающая матрица должна быть в minimal-base форме.
 * @author stas
 *
 */
public class ConvCodeSpanForm implements ISpanForm {
	/**
	 * Разложение по степеням порождающей матрицы сверточного кода. 
	 */
	public Matrix matrices[];
	/**
	 * degrees[i] для i = 1..b - это наибольшая степень в i-том ряду при разложении по степеням.
	 * Т.е. matrices[degrees[i]].getRow(i) содержит единицы, а matrices[degrees[i] + k].getRow(i), где k > 0, уже нет.  
	 */
	int degrees[];
	/**
	 * Начала строк в матрице matrices[0] 
	 */
	private int spanHeads[];
	/**
	 * Концы строк в матрицах matrices[degrees[i]] 
	 */
	private int spanTails[];

	public ConvCodeSpanForm(Matrix matrices[], int degrees[], int spanHeads[], int spanTails[]) {
		this.matrices = matrices;
		this.degrees = degrees;
		this.spanHeads = spanHeads;
		this.spanTails = spanTails;

		checkSpanForm();
	}

	@Override
	public int getRowCount() {
		return matrices[0].getRowCount();
	}

	@Override
	public int getHead(int row) {
		return spanHeads[row];
	}

	@Override
	public int getTail(int row) {
		return spanTails[row];
	}

	@Override
	public void setHead(int row, int column) {
		spanHeads[row] = column;
	}

	@Override
	public void setTail(int row, int column) {
		spanTails[row] = column;
	}

	/**
	 * Проверяет корректность спеновой формы. 
	 */
	private void checkSpanForm() {
		if (matrices[0].getRowCount() != degrees.length || 
				matrices[0].getRowCount() != spanHeads.length ||
				matrices[0].getRowCount() != spanTails.length) {
			throw new IllegalArgumentException("Wrong sizes of input matrices or arrays.");
		}

		for (int i = 0; i < matrices[0].getRowCount(); ++i) {
			if (matrices[0].getRow(i).nextSetBit(0) != spanHeads[i]) {
				throw new IllegalArgumentException("Span heads don't correspond to matrices.");
			}
			if (matrices[degrees[i]].getRow(i).previousSetBit(matrices[0].getColumnCount() - 1) != spanTails[i]) {
				throw new IllegalArgumentException("Span tails don't correspond to matrices.");
			}
		}
	}
}
