package search_tools;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combinations enumerator
 * 
 * @author Stas
 *
 */
public class CEnumerator {
	static final private Logger logger = LoggerFactory.getLogger(CEnumerator.class);
	
	private long n;
	private int k;
	private long[] sequence;
	
	
	public CEnumerator(long n, int k)
	{
		if (n < k) {
			throw new IllegalArgumentException("There is no combinations with n < k.");
		}
		
		this.n = n;
		this.k = k;			
	}
	
	public BigInteger count() {
		BigInteger numerator = BigInteger.ONE;
	    BigInteger denumerator = BigInteger.ONE;		
		
		for (long i = n - k + 1;i <= n; ++i) {
			numerator = numerator.multiply(new BigInteger(Long.toString(i)));
			denumerator = denumerator.multiply(new BigInteger(Long.toString(n - i + 1)));			
		}
		
		return numerator.divide(denumerator);
	}
	
	private void initSequence() {
		sequence = new long[k];
		
		for (int i = 0;i < k;i ++) {
			sequence[i] = i;
		}
	}
	
	public int getK() {
		return k;
	}
	
	public long getN() {
		return n;
	}
	
	public boolean hasNext() {
		return sequence == null || (k > 0 && sequence[0] != (n-k));
	}
	
	public long[] current() {
		return sequence;
	}
	
	public long[] shift(int index) {
		if (sequence[index] == n-1-(k-1-index)) {
			return next();
		}
		
		++sequence[index];
		for (int i = index + 1;i < k; ++i) {
			sequence[i] = sequence[index] + (i - index);
		}
		
		return sequence.clone();
	}
	
	public long[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next combination.");
		}
		
		if (sequence == null) {
			initSequence();
			return sequence.clone();
		}
		
		if (sequence[k-1] < n-1) {
			sequence[k-1] ++;
			return sequence.clone();
		}
		
		int indFromEnd = 1;
		
		while(sequence[k-1-indFromEnd] == n-1-indFromEnd)
		{
			indFromEnd++;
		}
		
		sequence[k-1-indFromEnd] ++;
		for(int i = k-indFromEnd;i < k;i ++)
		{
			sequence[i] = sequence[k-1-indFromEnd] + (i - (k-1-indFromEnd));
		}
		
		return sequence.clone();
	}
	
	public long[] getByIndex(BigInteger index) {
		long[] sequence = new long[k];
		BigInteger delta = BigInteger.ZERO;
		long digit = 1;
		
		for (int i = k - 1; i >= 0; --i) {
			while ((delta.add(new CEnumerator(n - digit, i).count())).compareTo(index) <= 0) { 
				delta = delta.add(new CEnumerator(n - digit, i).count());
				++digit;
			}
			
			sequence[k - i - 1] = digit - 1;
			++digit;
		}
		
		return sequence;
	}
	
	private static Random rnd = new Random();
	
	public long[] random() {
		long[] sequence = new long[k];		
		
		for (int i = 0;i < k; ++i) {
			long ballIndex = (((long)rnd.nextInt(Integer.MAX_VALUE) << 31) + rnd.nextInt(Integer.MAX_VALUE)) % (n - i);
			
			for (int j = 0;j < i; ++j) {
				if (sequence[j] <= ballIndex) {
					++ballIndex;
				}
			}
			
			sequence[i] = ballIndex;
		}
		
		for (int i = 0; i < k; ++i) {
			for (int j = k - 1; j > i; --j) {
				if (sequence[j] < sequence[j - 1]) {
					long tmp = sequence[j];
					
					sequence[j] = sequence[j - 1];
					sequence[j - 1] = tmp;
				}
			}
		}
		
		return sequence;
	}

}
