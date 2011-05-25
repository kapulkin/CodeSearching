package search_procedures;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CEnumerator {
	
	private long n;
	private int k;
	private long[] sequence;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	public CEnumerator(long n, int k)
	{
		if (n < k) {
			throw new InvalidParameterException("There is no combinations with n < k.");
		}

		double combinations = 1; // = n!/k!/(n-k)! = (k+1)*..*n/(n-k)!
		for (long i = k + 1; i <= n; ++i) {
			combinations *= i;
			combinations /= (n - i + 1);
		}
		logger.debug("C(" + n + ", " + k + ") gives " + combinations + " combinations.");
		
		this.n = n;
		this.k = k;			
	}
	
	private void initSequence()
	{
		sequence = new long[k];
		
		for(int i = 0;i < k;i ++)
		{
			sequence[i] = i + 1;
		}
	}
	
	public boolean hasNext()
	{
		return sequence == null || sequence[0] != (n-k+1);
	}
	
	public long[] getNext()
	{
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next combination.");
		}
		
		if(sequence == null)
		{
			initSequence();
			return sequence;
		}
		
		if(sequence[k-1] < n)
		{
			sequence[k-1] ++;
			return sequence;
		}
		
		int indFromEnd = 1;
		
		while(sequence[k-1-indFromEnd] == n-indFromEnd)
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
