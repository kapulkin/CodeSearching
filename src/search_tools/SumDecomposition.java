package search_tools;

import java.util.NoSuchElementException;

/**
 * Перечисляет все возможные разложения числа на заданное колличество слагаемых
 * с точностью до перестановки. Дополнительно может быть задано ограничение на
 * максимальное значение слагаемых.
 * 
 * @author Stas
 *
 */
public class SumDecomposition {
	private int number;

	private int upperBound;
	private int lowerBound;
	private int nextItems[]; // все слагаемые хранятся в невозррастающем порядке.
	/**
	 * Индекс последнего ненулевого слагаемого в текущем разложении.
	 */
	private int last;
	private boolean hasNext;
	
	/**
	 * @param number разлагаемое число
	 * @param sumCount колличество слагаемых
	 * @param upperBound ограничение сверху на слагаемое
	 * @param lowerBound ограничение снизу на слагаемое
	 */
	public SumDecomposition(int number, int sumCount, int upperBound, int lowerBound) {
		if (sumCount <= 0 || number < 0 || upperBound <= 0) {
			throw new IllegalArgumentException("Wrong input parameters: " + sumCount + ", " + number + ", " + upperBound);
		}
		
		int summand = number / sumCount;
		int reminder = number % sumCount;
		if (summand > upperBound - (reminder == 0 ? 0 : 1)) {
			throw new IllegalArgumentException("Cannot decompose " + number + " to summands not greater then " + upperBound);
		}
		
		if (summand < lowerBound) {
			throw new IllegalArgumentException("Cannot decompose " + number + " to summands not less then " + lowerBound);
		}
		
//		if (number / upperBound + (number % upperBound == 0 ? 0 : 1) > sumCount) {
//			throw new IllegalArgumentException("Cannot decompose " + number + " to summands not greater then " + upperBound);
//		}

		this.number = number;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		
		nextItems = new int[sumCount];
		for (int i = 0; i < sumCount; ++i) {
			nextItems[i] = lowerBound;
		}
		int rest = number - sumCount * lowerBound;
		for (last = 0; last < sumCount; ++last) {
			int addition = Math.min(rest, upperBound - lowerBound);
			nextItems[last] += addition;
			rest -= addition;
			if (rest == 0) {
				break;
			}
		}

		hasNext = true;
	}

	public SumDecomposition(int number, int sumCount) {
		this(number, sumCount, number, 0);
	}
	
	public int getSumCount() {
		return nextItems.length;
	}
	
	public int getNumber() {
		return number;
	}
	
	public int getUpperBound() {
		return upperBound;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	public int [] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next decomposition.");
		}
		
		int items[] = nextItems;

		int threshold = items[items.length - 1];
		int rest = 0;
		while (last >= 0 && items[last] - 1 < threshold + 1) {
			rest += items[last];
			--last;
		}
		
		if (last < 0) {
			hasNext = false;
			return items;
		}

		nextItems = new int[items.length];
		System.arraycopy(items, 0, nextItems, 0, last);
		nextItems[last] = items[last] - 1;
		++rest;
		
		for (int i = last + 1; i < nextItems.length; ++i) {
			nextItems[i] = lowerBound;
		}
		rest -= (getSumCount() - last - 1) * lowerBound;
		for (int i = last + 1; i < nextItems.length; ++i) {
			int addition = Math.min(rest, nextItems[last] - lowerBound);
			nextItems[i] += addition;
			rest -= addition;
			if (rest == 0) {
				last = i;
				break;
			}
		}
						
		return items;
	}
	
	@Override
	public String toString() {
		if (!hasNext()) {
			return "end";
		}
		return nextItems.toString();
	}
}
