package math;

public class GrayCode {

	public static int getWord(int n)
	{
		return n ^ (n/2);
	}
	
	public static int getChangedBit(int n)
	{		
		int pow = Integer.SIZE-1;
		return n == 0?-1:Integer.numberOfTrailingZeros(((1<<pow)-1) ^ (n-1));
	}
}
