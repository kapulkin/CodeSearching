package search_procedures;

import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

public class CEnumerator {
	
	private int n;
	private int k;
	private int[] sequence;
	
	public CEnumerator(int n, int k)
	{
		if (n < k) {
			throw new InvalidParameterException("There is no combinations with n < k.");
		}
		
		this.n = n;
		this.k = k;			
	}
	
	private void initSequence()
	{
		sequence = new int[k];
		
		for(int i = 0;i < k;i ++)
		{
			sequence[i] = i + 1;
		}
	}
	
	public boolean hasNext()
	{
		return sequence == null || sequence[0] != (n-k+1);
	}
	
	public int[] getNext()
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
