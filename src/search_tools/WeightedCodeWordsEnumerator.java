package search_tools;

import java.util.NoSuchElementException;

import math.Poly;

/**
 * Класс перебирает все кодовые слова указанной длины с ограничением степени с заданным весом.  
 * @author Stas
 *
 */
public class WeightedCodeWordsEnumerator {
	private int itemNumber;
	private int degree;
	private SumDecomposition decomposition;
	private int items[];	// распределяет вес по полиномам
	private CEnumerator enumerators[];	// расставляет вес по коэффициентам полинома
	private Poly polies[];	// полиномы
	
	public WeightedCodeWordsEnumerator(int weight, int degree, int length) {
		itemNumber = Math.min(weight, length);
		this.degree = degree;
		
		decomposition = new SumDecomposition(itemNumber, weight);
	}

	private void initPolies() {
		enumerators = new CEnumerator[itemNumber];
		polies = new Poly[itemNumber];

		items = decomposition.next();
		for (int i = 0; i < itemNumber; ++i) {
			int coeffsNumber = Math.min(degree, items[i]);
			enumerators[i] = new CEnumerator(degree, coeffsNumber);
			polies[i] = makePoly(enumerators[i].getNext());
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
		
		for (int i = 0; i < itemNumber; ++i) {
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
		
		for (int i = 0; i < itemNumber; ++i) {
			if (enumerators[i].hasNext()) {
				polies[i] = makePoly(enumerators[i].getNext());
				for (int j = i - 1; j >= 0; --j) {
					enumerators[j] = new CEnumerator(degree, items[j]);
					polies[j] = makePoly(enumerators[j].getNext());
				}
				return polies;
			}
		}
		
		items = decomposition.next();
		for (int i = 0; i < itemNumber; ++i) {
			enumerators[i] = new CEnumerator(degree, items[i]);
			polies[i] = makePoly(enumerators[i].getNext());
		}

		return polies;
	}
}
