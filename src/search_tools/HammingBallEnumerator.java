package search_tools;

import java.math.BigInteger;
import java.util.Random;

public class HammingBallEnumerator {
	private int k;
	private long n;
	private CEnumerator sphereEnum;
	
	public HammingBallEnumerator(long n, int k) {
		this.k = k;
		this.n = n;
		sphereEnum = new CEnumerator(n, 1);
	}
	
	public int getK() {
		return k;
	}
	
	public long getN() {
		return n;
	}
	
	public BigInteger count() {
		BigInteger cnt = BigInteger.ZERO;
		
		for (int r = 1;r <= k; ++r) {
			cnt = cnt.add(sphereEnum.count());
		}
				
		return cnt;
	}
	
	public boolean hasNext() {
		return sphereEnum.getK() < k || sphereEnum.hasNext();
	}
	
	public long[] current() {
		return sphereEnum.current();
	}
	
	public long[] next() {
		if (sphereEnum.hasNext()) {
			return sphereEnum.next();
		}
		
		sphereEnum = new CEnumerator(n, sphereEnum.getK() + 1);
		return sphereEnum.next();
	}
	
}
