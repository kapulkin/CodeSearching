package search_tools;

import java.util.NoSuchElementException;

import math.Poly;

/**
 * Класс перебирает все кодовые слова указанной длины с заданными весом и степенью.
 * 
 * В возвращаемом слове полиномы отсортированы по убыванию их веса.
 *   
 * @author Stas
 *
 */
public class WeightedCodeWordsEnumerator {
	private int sumCount;
	private int degree;
	private int length;
	private SumDecomposition decomposition;
	private int summands[];	// распределяет вес по полиномам
	private CEnumerator enumerators[];	// расставляет вес по коэффициентам полинома
	private Poly polies[];	// полиномы
	
	/**
	 * 
	 * @param weight вес кодового слова
	 * @param degree степень кодового слова
	 * @param length длина кодового слова
	 */
	public WeightedCodeWordsEnumerator(int weight, int degree, int length) {
		sumCount = Math.min(weight, length);
		this.degree = degree;
		this.length = length;
		
		decomposition = new SumDecomposition(weight, sumCount, degree + 1, 0);
	}

	private void initPolies() {
		polies = new Poly[length];

		summands = decomposition.next();
		enumerators = new CEnumerator[sumCount];
		for (int i = 0; i < sumCount; ++i) {
			int coeffsNumber = Math.min(degree + 1, summands[i]);
			enumerators[i] = new CEnumerator(degree + 1, coeffsNumber);
			polies[i] = makePoly(enumerators[i].next());
		}
		
		for (int i = sumCount; i < length; ++i) {
			polies[i] = new Poly();
		}
	}

	private Poly makePoly(long powers[]) {
		Poly poly = new Poly();
		for (int i = 0; i < powers.length; ++i) {
			poly.setCoeff((int)powers[i], true);
		}
		return poly;
	}
	
	public boolean hasNext() {
		if (polies == null) {
			return true;
		}
		
		if (decomposition.hasNext()) {
			return true;
		}
		
		for (int i = 0; i < sumCount; ++i) {
			if (enumerators[i].hasNext()) {
				return true;
			}
		}
		
		return false;
	}
	
	public Poly[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		if (polies == null) {
			initPolies();
			return polies;
		}
		
		for (int i = 0; i < sumCount; ++i) {
			if (enumerators[i].hasNext()) {
				polies[i] = makePoly(enumerators[i].next());
				for (int j = i - 1; j >= 0; --j) {
					enumerators[j] = new CEnumerator(degree + 1, summands[j]);
					polies[j] = makePoly(enumerators[j].next());
				}
				return polies;
			}
		}
		
		summands = decomposition.next();
		for (int i = 0; i < sumCount; ++i) {
			enumerators[i] = new CEnumerator(degree + 1, summands[i]);
			polies[i] = makePoly(enumerators[i].next());
		}

		return polies;
	}
}
