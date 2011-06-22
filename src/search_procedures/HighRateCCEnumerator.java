package search_procedures;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.Trellis;
import trellises.Trellises;
import math.BitArray;
import math.MinDistance;
import math.Poly;
import math.PolyMatrix;
import codes.ConvCode;

public class HighRateCCEnumerator {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	int delay;
	int freeDist;

	RowsEnumerator lowerRowsEnum;
	RowsEnumerator higherRowsEnum;	
	ArrayList<BitArray> lowerRows;
	ArrayList<BitArray> higherRows;
	
	Random randGen = new Random();
	
	public HighRateCCEnumerator(int delay, int freeDist) {
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
		
		BitArray lowerRowsArray[];
		BitArray higherRowsArray[];
		PolyMatrix checkMatrix;
		while (attempts > 0 && lowerRowsEnum.hasNext() && higherRowsEnum.hasNext()) {
			--attempts;

			lowerRowsArray = lowerRows.toArray(new BitArray[] {});
			higherRowsArray = higherRows.toArray(new BitArray[] {});
			checkMatrix = makeCheckMatrix(lowerRowsArray, higherRowsArray);
			if (!checkLowEstimation(checkMatrix, freeDist)) {
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
				// нашли подходящий код.
				ConvCode code = null;
				try {
					do {
						code = new ConvCode(checkMatrix, false);

						// добавляем следующий вектор к матрице
						if (higherRows.size() == 2) {
							lowerRows.add(lowerRowsEnum.getNext()[0]);
						} else {
							if (randGen.nextBoolean()) {
								lowerRows.add(lowerRowsEnum.getNext()[0]);
							} else {
								higherRows.add(higherRowsEnum.getNext()[0]);
							}
						}
						lowerRowsArray = lowerRows.toArray(new BitArray[] {});
						higherRowsArray = higherRows.toArray(new BitArray[] {});
						checkMatrix = makeCheckMatrix(lowerRowsArray, higherRowsArray);
					} while (checkLowEstimation(checkMatrix, freeDist));
				} catch (NoSuchElementException e) {
					logger.debug("Enumeration is finished.");
				}
				return code;
			}	
		}
				
		logger.debug("An appropriate next code was not found.");
		
		return null;
	}

	/**
	 * Проверяет нижнюю оценку свободного расстояния кода, заданного 
	 * <code>checkMatrix</code>. Возвращает <code>true</code>, если свободное 
	 * расстояние кода больше или равно <code>expectedFreeDist</code>.
	 * @param checkMatrix проверочная матрица, задающая сверточный код
	 * @param expectedFreeDist проверяемая нижняя оценка на свободное расстояние кода
	 * @return <code>true</code>, если свободное расстояние код больше или равно <code>expectedFreeDist</code>.
	 */
	private boolean checkLowEstimation(PolyMatrix checkMatrix, int expectedFreeDist) {
		Trellis trellis = Trellises.trellisFromParityCheckHR(checkMatrix);
		MinDistance.computeDistanceMetrics(trellis);
		
		int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, checkMatrix.getColumnCount() * (delay+1));

		logger.debug("actual free dist = " + freeDist);
		
		return freeDist >= expectedFreeDist; /**/
		
		// TODO: более хитрая проверка: перебираем все слова веса expectedFreeDist-1, длины k+1 и степени v-1
		// TODO: умножаем их на матрицу - если в какой-то момент получается ноль - возвращаем false, иначе true
		// TODO: еще более хитрая проверка: вместо тупого перебора хитрые правила и более тонкий перебор.

/*		WeightedCodeWordsEnumerator wordsEnumerator = new WeightedCodeWordsEnumerator(expectedFreeDist-1, delay, checkMatrix.getColumnCount());
		
		while (wordsEnumerator.hasNext()) {
			Poly words[] = wordsEnumerator.next();
			CEnumerator columnsEnumerator = new CEnumerator(checkMatrix.getColumnCount(), Math.min(expectedFreeDist-1, checkMatrix.getColumnCount()));
			while (columnsEnumerator.hasNext()) {
				// TODO: Все перемножить и сравнить с нулем. И если ноль, выходим.
				Poly sum = new Poly();
				long columns[] = columnsEnumerator.getNext();
				for (int i = 0; i < columns.length; ++i) {
					sum.add(words[i].mul(checkMatrix.get(0, (int)columns[i])));
				}
				
				if (sum.isZero()) {
					return false;
				}
			}
		}
		
		return true;/**/
	}

	private PolyMatrix makeCheckMatrix(BitArray[] lowerRows, BitArray[] higherRows) {
		return (freeDist % 2 == 0) ? 
				FreeDist4CCEnumerator.makeCheckMatrix(lowerRows, higherRows) :
					FreeDist3CCEnumerator.makeCheckMatrix(lowerRows, higherRows);
	}
}
