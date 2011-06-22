package search_procedures;

import java.util.NoSuchElementException;

/**
 * Раскладывает переданное в конструкторе число на заданное количество слагаемых
 * всеми возможными способами с точностью до перестановки слагаемых.
 * @author Stas
 *
 */
public class SumDecomposition {
	private int itemNumber;
	private int number;
//	private int restriction;
	private int items[]; // все слагаемые хранятся в неубывающем порядке.
	private boolean hasNext;
	
	/**
	 * 
	 * @param itemNumber колличество слагаемых
	 * @param number разлагаемое число
//	 * @param restriction ограничение сверху на слагаемое
	 */
	public SumDecomposition(int itemNumber, int number) {
		this.itemNumber = itemNumber;
		this.number = number;

		if (itemNumber <= 0 || number < 0) {
			throw new IllegalArgumentException("Wrong input parameters: " + itemNumber + ", " + number);
		}
		
		items = new int [itemNumber];
		for (int i = 0; i < items.length - 1; ++i) {
			items[i] = 0;
		}
		items[items.length - 1] = number;
		hasNext = true;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	public int [] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next decomposition.");
		}
		int [] itemsCopy = new int[items.length];
		System.arraycopy(items, 0, itemsCopy, 0, items.length);
		
		hasNext = false;
		
		for (int i = items.length - 2; i >= 0; --i) {
			++items[i]; // увеличиваем предпоследний элемент.
			int current_sum = 0;
			for (int j = 0; j <= i; ++j) {
				current_sum += items[j];
			}
			if (number - current_sum >= items[i] * (items.length - 1 - i)) {
				// можем замостить все справа не меньшими слагаемыми 
				for (int j = i + 1; j < items.length - 1; ++j) {
					items[j] = items[i];
					current_sum += items[i];
				}
				items[items.length - 1] = number - current_sum;
				hasNext = true; // нашли следующий элемент
				break;
			}
		}
		
		return itemsCopy;
	}
}
