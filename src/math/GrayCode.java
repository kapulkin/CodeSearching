package math;

/**
 * Позволяет последовательно перебирать коды Грея длины <code>k</code> от 0 до 2^k - 1.
 * @author stas
 *
 */
public class GrayCode {
	/**
	 * Возвращает <code>n</code>-ый элемент последовательности кода Грея. 
	 * Нумерация начинается с нуля.
	 * @param n номер элемента в последовательности
	 * @return <code>n</code>-ый элемент последовательности
	 */
	public static int getWord(int n)
	{
		return n ^ (n/2);
	}
	
	/**
	 * Возвращает номер бита, измененного при переходе к <code>n</code>-ому элементу последовательности.
	 * @param n номер элемента в последовательности
	 * @return номер бита, измененного при переходе к <code>n</code>-ому элементу последовательности
	 */
	public static int getChangedPosition(int n)
	{		
		int pow = Integer.SIZE-1;
		return n == 0?-1:Integer.numberOfTrailingZeros(((1<<pow)-1) ^ (n-1));
	}
	
	/**
	 * Возвращает новое значение бита, изменившегося при переходе к <code>n</code>-ому элементу последовательности. 
	 * @param n номер элемента в последовательности
	 * @return новое значение бита, изменившегося при переходе к <code>n</code>-ому элементу последовательности
	 */
	public static boolean getChangedBit(int n) {
		int pos = getChangedPosition(n);
		if (pos == -1) {
			throw new IllegalArgumentException();
		}
		
		return (getWord(n) & (1 << pos)) != 0;
	}
}
