package search_procedures;

import java.util.NoSuchElementException;

import search_tools.CEnumerator;

/**
 * Arrangements enumerator
 * @author Stas
 *
 */
public class AEnumerator {
	private long combination[] = null;
	private CEnumerator cenumerator;
	private PEnumerator penumerator;
	
	public AEnumerator(long n, int k) {
		cenumerator = new CEnumerator(n, k);
		penumerator = new PEnumerator(k);
	}
	
	public long getN() {
		return cenumerator.getN();
	}
	
	public int getK() {
		return cenumerator.getK();
	}
	
	public boolean hasNext() {
		return combination == null || cenumerator.hasNext() || penumerator.hasNext();
	}
	
	public long[] next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next arragement.");
		}

		if (combination == null) {
			combination = cenumerator.next();
		}
		
		if (!penumerator.hasNext()) {
			combination = cenumerator.next();
			penumerator = new PEnumerator(getK());
		}

		int i = 0;
		long arragement[] = new long[getK()];
		for (int p : penumerator.next()) {
			arragement[i++] = combination[p];
		}
		
		return arragement;
	}
}
