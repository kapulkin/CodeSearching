package search_tools;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combinations enumerator
 * 
 * @author Stas
 *
 */
public class CEnumerator {
	
	private long n;
	private int k;
	private long[] sequence;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public CEnumerator(long n, int k)
	{
		if (n < k) {
			throw new IllegalArgumentException("There is no combinations with n < k.");
		}

		if (logger.isDebugEnabled()) {
			double combinations = 1; // = n!/k!/(n-k)! = (k+1)*..*n/(n-k)!
			for (long i = k + 1; i <= n; ++i) {
				combinations *= i;
				combinations /= (n - i + 1);
			}
		}
		
		this.n = n;
		this.k = k;			
	}
	
	private void initSequence()
	{
		sequence = new long[k];
		
		for(int i = 0;i < k;i ++)
		{
			sequence[i] = i;
		}
	}
	
	public int getK() {
		return k;
	}
	
	public long getN() {
		return n;
	}
	
	public boolean hasNext()
	{
		return sequence == null || (k > 0 && sequence[0] != (n-k));
	}
	
	public long[] next()
	{
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next combination.");
		}
		
		if(sequence == null)
		{
			initSequence();
			return sequence;
		}
		
		if(sequence[k-1] < n-1)
		{
			sequence[k-1] ++;
			return sequence;
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
		
		return sequence;
	}

}
