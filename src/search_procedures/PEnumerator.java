package search_procedures;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Permutations enumerator
 * 
 * Рекурсивная реализация, не очень эффективна по памяти и времени.
 * 
 * @author Stas
 *
 */
public class PEnumerator {
	private int n;
	/**
	 * Инверсия перестановки
	 */
	private int invertion[];
	private int permutation[];
	private List<Integer> numbers;
	private boolean hasNext;
	
	public PEnumerator(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("The length of permutation can't be negative: " + n);
		}
		
		this.n = n;
		invertion = new int[n];
		permutation = new int[n];
		hasNext = true;

		numbers = new LinkedList<Integer>();
		for (int i = 0; i < n; ++i) {
			numbers.add(i);
		}
	}
	
	private void makePermutation() {
		List<Integer> numbers = new LinkedList<Integer>(this.numbers);
		
		for (int i = 0; i < n; ++i) {
			permutation[i] = numbers.remove(invertion[i]);
		}
	}

	public int getN() {
		return n;
	}
	
	public boolean hasNext() {
		return hasNext;
	}

	public int[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next permutation.");
		}

		makePermutation();
		
		// получаем следующую инверсию инкрементированием текущей как числа в переменной системе счисления
		int i = n - 2;
		while (i >= 0 && invertion[i] == n - 1 - i) {
			invertion[i] = 0;
			--i;
		}
		
		if (i >= 0) {
			++invertion[i];
		} else {
			hasNext = false;
		}
		
		return permutation;
	}
	
/*	private int index;
	private PEnumerator reduction = null;
	
	private void initPermutation() {
		permutation = new int[n];
		index = n - 1;
		if (n > 1) {
			reduction = new PEnumerator(n - 1);
			System.arraycopy(reduction.next(), 0, permutation, 0, n - 1);
			permutation[index] = n - 1;
		}
	}
	
//	private void reset() {
//		index = n - 1;
//		if (n > 1) {
//			reduction.reset();
//			System.arraycopy(reduction.next(), 0, permutation, 0, n - 1);
//			permutation[index] = n - 1;
//		}
//	}
		
	public boolean hasNext() {
		return permutation == null || (n > 1 && (index > 0 || reduction.hasNext()));
	}
	
	public int[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next permutation.");
		}
		
		if (permutation == null) {
			initPermutation();
			return permutation;
		}
		
		if (!reduction.hasNext()) {
			--index;
			reduction = new PEnumerator(n - 1);
		}
		int reductedPermutaion[] = reduction.next();
		System.arraycopy(reductedPermutaion, 0, permutation, 0, index);
		permutation[index] = n - 1;
		System.arraycopy(reductedPermutaion, index, permutation, index + 1, n - index - 1);
		
		return permutation;
	}/**/
}
