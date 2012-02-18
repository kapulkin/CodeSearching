package search_tools;

import java.math.BigInteger;

public class HammingBallEnumerator {
	private int k;
	private int n;
	private CEnumerator sphereEnum;
	
	public HammingBallEnumerator(int n, int k) {
		this.k = k;
		this.n = n;
		sphereEnum = new CEnumerator(n, 1);
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
	
	public long[] next() {
		if (sphereEnum.hasNext()) {
			return sphereEnum.next();
		}
		
		sphereEnum = new CEnumerator(n, sphereEnum.getK() + 1);
		return sphereEnum.next();
	}
}
