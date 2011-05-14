package search_procedures;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.Trellis;
import trellises.Trellises;
import math.BitArray;
import math.PolyMatrix;
import codes.ConvCode;
import codes.MinDistance;

public class HighRateCCSearcher {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	int delay;
	int freeDist;

	RowsEnumerator lowerRowsEnum;
	RowsEnumerator higherRowsEnum;	
	ArrayList<BitArray> lowerRows;
	ArrayList<BitArray> higherRows;
	
	Random randGen = new Random();
	
	public HighRateCCSearcher(int delay, int freeDist) {
		this.delay = delay;
		this.freeDist = freeDist;
		
		if (freeDist <= 4) {
			throw new IllegalArgumentException("This class searches codes with free distance >= 5.");
		}
		
//		int codeLengthRestriction = (freeDist % 2 == 0) ? (1 << delay - 2) + 2 : (1 << delay - 1) + 2;
		// инициализируем начальные ветора в зависимости от четности freeDist
		// отдельно вектора степени v-1
		lowerRowsEnum = new RowsEnumerator(1, freeDist % 2 == 0 ? delay - 2 : delay - 1);
		lowerRows = new ArrayList<BitArray>();
		lowerRows.add(lowerRowsEnum.getNext()[0]);
		// отдельно nv вектора степени v
		higherRowsEnum = new RowsEnumerator(1, freeDist % 2 == 0 ? delay - 2 : delay - 1);
		higherRows = new ArrayList<BitArray>();
		higherRows.add(higherRowsEnum.getNext()[0]);
		
	}
	
	public ConvCode next() {
		int attempts = delay * freeDist;
		
		boolean codeFound = false;
		while (!codeFound && attempts > 0 && lowerRowsEnum.hasNext() && higherRowsEnum.hasNext()) {
			--attempts;

			BitArray lowerRowsArray[] = lowerRows.toArray(new BitArray[] {});
			BitArray higherRowsArray[] = higherRows.toArray(new BitArray[] {});
			if (!checkLowEstimation(makeCheckMatrix(lowerRowsArray, higherRowsArray), freeDist)) {
				// удаляем случайный вектор: или из lowerRows или из higherRows
				int rowIndex = randGen.nextInt(lowerRows.size() + higherRows.size());
				if (rowIndex < lowerRows.size()) {
					lowerRows.remove(rowIndex);
				} else {
					higherRows.remove(rowIndex - lowerRows.size());
				}
				
				// добавляем следующий - если nv == 0, то обязательно из 
				// higherRows, если nv == 2, то обязательно из lowerRows 
				if (higherRows.isEmpty()) {
					higherRows.add(higherRowsEnum.getNext()[0]);
				} else if (higherRows.size() == 2) {
					lowerRows.add(lowerRowsEnum.getNext()[0]);
				} else {
					if (randGen.nextBoolean()) {
						lowerRows.add(lowerRowsEnum.getNext()[0]);
					} else {
						higherRows.add(higherRowsEnum.getNext()[0]);
					}
				}
			} else {
				codeFound = true;
			}	
		}
		
		if (codeFound) {
			// нашли подходящий код.
			BitArray lowerRowsArray[];
			BitArray higherRowsArray[];

			try {
				boolean added = true;
				do {
					// добавляем следующий вектор к матрице
					if (higherRows.size() == 2) {
						lowerRows.add(lowerRowsEnum.getNext()[0]);
					} else {
						if (randGen.nextBoolean()) {
							lowerRows.add(lowerRowsEnum.getNext()[0]);
						} else {
							higherRows.add(higherRowsEnum.getNext()[0]);
							added = false;
						}
					}
					lowerRowsArray = lowerRows.toArray(new BitArray[] {});
					higherRowsArray = higherRows.toArray(new BitArray[] {});
				} while (checkLowEstimation(makeCheckMatrix(lowerRowsArray, higherRowsArray), freeDist));
				
				// удаляем последний добавленный: откатываемся на последний рабочий вариант
				if (added) {
					lowerRows.remove(lowerRows.size() - 1);
				} else {
					higherRows.remove(higherRows.size() - 1);
				}
			} catch (NoSuchElementException e) {
				// do nothing
			}
			lowerRowsArray = lowerRows.toArray(new BitArray[] {});
			higherRowsArray = higherRows.toArray(new BitArray[] {});
			return new ConvCode(makeCheckMatrix(lowerRowsArray, higherRowsArray), false);
		}
		
		return null;
	}

	private boolean checkLowEstimation(PolyMatrix checkMatrix, int expectedFreeDist) {
		Trellis trellis = Trellises.trellisFromParityCheckHR(checkMatrix);
		MinDistance.computeDistanceMetrics(trellis);
		
		int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (delay+1));

		logger.debug("actual free dist = " + freeDist);
		
		return freeDist >= expectedFreeDist;
		
		// TODO: более хитрая проверка: перебираем все слова веса expectedFreeDist-1, длины k+1 и степени v-1
		// TODO: умножаем их на матрицу - если в какой-то момент получается ноль - возвращаем false, иначе true
		
		// TODO: еще более хитрая проверка: вместо тупого перебора хитрые правила и более тонкий перебор.
	}

	private PolyMatrix makeCheckMatrix(BitArray[] lowerRows, BitArray[] higherRows) {
		return (freeDist % 2 == 0) ? 
				FreeDist4CCEnumerator.makeCheckMatrix(lowerRows, higherRows) :
					FreeDist3CCEnumerator.makeCheckMatrix(lowerRows, higherRows);
	}
}
