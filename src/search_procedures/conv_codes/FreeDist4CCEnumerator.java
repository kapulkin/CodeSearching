package search_procedures.conv_codes;

import java.util.NoSuchElementException;

import math.BitArray;
import math.PolyMatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_tools.RowsEnumerator;

import codes.ConvCode;
import codes.tests.ConvCodeTest;

/**
 * Данный класс перебирает k/(k+1) скоростные коды со свободным расстоянием >= 4
 * и малой сложностью решетки.
 * 
 * @author stas
 *
 */
public class FreeDist4CCEnumerator {
	static final private Logger logger = LoggerFactory.getLogger(FreeDist4CCEnumerator.class);
	
	int delay;
	int infBitsNumber;
	
	RowsEnumerator lowerRowsEnum;
	RowsEnumerator higherRowsEnum;	
	int nv;
	
	BitArray lowerRows[];

	public FreeDist4CCEnumerator(int delay, int infBitsNumber) {
		this.delay = delay;
		this.infBitsNumber = infBitsNumber;
		
		if (delay < 3) {
			throw new IllegalArgumentException("Delay should be more than 1");
		}
		
		int codeLengthRestriction = (1 << delay - 2) + 2;
		if (infBitsNumber + 1 > codeLengthRestriction) {
			throw new IllegalArgumentException("Cannot create code for such delay and code length: " + delay + ", " + (infBitsNumber + 1));
		}

		nv = (infBitsNumber + 1 < codeLengthRestriction) ? 1 : 2;
		lowerRowsEnum = new RowsEnumerator(infBitsNumber + 1 - nv, delay - 2);
		lowerRows = lowerRowsEnum.getNext();
		higherRowsEnum = new RowsEnumerator(nv, delay - 2);
	}
	
	public boolean hasNext() {
		return !(nv == 2 && !lowerRowsEnum.hasNext() && !higherRowsEnum.hasNext()); 
	}
	
	public ConvCode next() {
		if (!hasNext()) {
			throw new NoSuchElementException(); 
		}
		
		if (!higherRowsEnum.hasNext()) {
			if (!lowerRowsEnum.hasNext()) {
				++nv;
				lowerRowsEnum = new RowsEnumerator(infBitsNumber + 1 - nv, delay - 2);
			}
			lowerRows = lowerRowsEnum.getNext();
			higherRowsEnum = new RowsEnumerator(nv, delay - 2);
		}
		BitArray higherRows[] = higherRowsEnum.getNext();

		PolyMatrix checkMatrix = makeCheckMatrix(lowerRows, higherRows);

		return new ConvCode(checkMatrix, false);
	}

	/**
	 * Создает проверочную матрицу сверточного кода со свободным 
	 * расстоянием >= 4 на основе входных битовых строк.
	 * 
	 * Входные строки <code>lowerRows</code> должны быть различны. Используются 
	 * как коэффициенты в первых полиномах проверочной матрицы при степенях 1..delay-2.
	 * Коэффициент при степени 0 полагается равным 1. Коэффициент при степени 
	 * delay-1 ,берется таким, чтобы общее число единиц в полиноме было нечетным.
	 * 
	 * Входные строки <code>higherRows</code> должны быть различны. Используются 
	 * как коэффициенты в последних полиномах проверочной матрицы при степенях 1..delay-2.
	 * Коэффициент при степени 0 и степени delay полагается равным 1. Коэффициент
	 * при степени delay-1 ,берется таким, чтобы общее число единиц в полиноме было нечетным.
	 *
	 * @param lowerRows коэффициенты первых полиномов матрицы 
	 * @param higherRows коэффициенты последних полиномов матрицы
	 * @return проверочная матрица сверточного кода со свободным расстоянием >= 4
	 */
	public static  PolyMatrix makeCheckMatrix(BitArray[] lowerRows, BitArray[] higherRows) {
		int codeLength = lowerRows.length + higherRows.length;
		int delay = higherRows[0].getFixedSize() + 2;
		
		PolyMatrix checkMatrix = new PolyMatrix(1, codeLength);
		for (int i = 0; i < lowerRows.length; ++i) {
			checkMatrix.get(0, i).setCoeff(0, true);
			for (int j = 0; j < delay - 2; ++j) {
				checkMatrix.get(0, i).setCoeff(j + 1, lowerRows[i].get(j));
			}
			checkMatrix.get(0, i).setCoeff(delay - 1, lowerRows[i].cardinality() % 2 == 0 ? false : true);
		}
		for (int i = 0 ; i < higherRows.length; ++i) {
			checkMatrix.get(0, lowerRows.length + i).setCoeff(0, true);
			for (int j = 0; j < delay - 2; ++j) {
				checkMatrix.get(0, lowerRows.length + i).setCoeff(j + 1, higherRows[i].get(j));
			}
			checkMatrix.get(0, lowerRows.length + i).setCoeff(delay - 1, higherRows[i].cardinality() % 2 == 0 ? true : false);
			checkMatrix.get(0, lowerRows.length + i).setCoeff(delay, true);
		}
		return checkMatrix;
	}

}
