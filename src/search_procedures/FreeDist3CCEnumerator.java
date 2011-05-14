package search_procedures;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import math.BitArray;
import math.PolyMatrix;

/**
 * Данный класс перебирает k/(k+1) скоростные коды со свободным расстоянием >= 3
 * и малой сложностью решетки.
 * 
 * @author stas
 *
 */
public class FreeDist3CCEnumerator {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	int delay;
	int infBitsNumber;
	
	RowsEnumerator lowerRowsEnum;
	RowsEnumerator higherRowsEnum;	
	int nv;
	
	BitArray lowerRows[];

	public FreeDist3CCEnumerator(int delay, int infBitsNumber) {
		this.delay = delay;
		this.infBitsNumber = infBitsNumber;
		
		if (delay < 2) {
			throw new IllegalArgumentException("Delay should be more than 1");
		}
		
		if (infBitsNumber + 1 > (1 << delay - 1) + 2) {
			throw new IllegalArgumentException("Cannot create code for such delay and code length: " + delay + ", " + (infBitsNumber + 1));
		}

		nv = (infBitsNumber + 1 < (1 << delay - 1) + 2) ? 1 : 2;
		lowerRowsEnum = new RowsEnumerator(infBitsNumber + 1 - nv, delay - 1);
		lowerRows = lowerRowsEnum.getNext();
		higherRowsEnum = new RowsEnumerator(nv, delay - 1);
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
				lowerRowsEnum = new RowsEnumerator(infBitsNumber + 1 - nv, delay - 1);
			}
			lowerRows = lowerRowsEnum.getNext();
			higherRowsEnum = new RowsEnumerator(nv, delay - 1);
		}
		BitArray higherRows[] = higherRowsEnum.getNext();

		PolyMatrix checkMatrix = makeCheckMatrix(lowerRows, higherRows);

		return new ConvCode(checkMatrix, false);
	}

	/**
	 * Создает проверочную матрицу сверточного кода со свободным 
	 * расстоянием >= 3 на основе входных битовых строк.
	 * 
	 * Входные строки <code>lowerRows</code> должны быть различны. Используются 
	 * как коэффициенты в первых полиномах проверочной матрицы при степенях 1..delay-1.
	 * Коэффициент при степени 0 полагается равным 1.
	 *  
	 * Входные строки <code>higherRows</code> должны быть различны. Используются 
	 * как коэффициенты в последних полиномах проверочной матрицы при степенях 1..delay-1.
	 * Коэффициент при степени 0 и степени delay полагается равным 1.
	 *
	 * @param lowerRows коэффициенты первых полиномов матрицы 
	 * @param higherRows коэффициенты последних полиномов матрицы
	 * @return проверочная матрица сверточного кода со свободным расстоянием >= 3
	 */
	public static PolyMatrix makeCheckMatrix(BitArray[] lowerRows, BitArray[] higherRows) {
		int codeLength = lowerRows.length + higherRows.length;
		int delay = higherRows[0].getFixedSize() + 1;

		PolyMatrix checkMatrix = new PolyMatrix(1, codeLength);
		for (int i = 0; i < lowerRows.length; ++i) {
			checkMatrix.get(0, i).setCoeff(0, true);
			for (int j = 0; j < delay - 1; ++j) {
				checkMatrix.get(0, i).setCoeff(j + 1, lowerRows[i].get(j));
			}
		}
		for (int i = 0 ; i < higherRows.length; ++i) {
			checkMatrix.get(0, lowerRows.length + i).setCoeff(0, true);
			for (int j = 0; j < higherRows[i].getFixedSize(); ++j) {
				checkMatrix.get(0, lowerRows.length + i).setCoeff(j + 1, higherRows[i].get(j));
			}
			checkMatrix.get(0, lowerRows.length + i).setCoeff(delay, true);
		}
		return checkMatrix;
	}
}
