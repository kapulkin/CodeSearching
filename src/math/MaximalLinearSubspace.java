package math;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;

import search_tools.CEnumerator;

public abstract class MaximalLinearSubspace {
	protected int dim;
	
	protected class BitVectorComp implements Comparator<BitArray> {
		
		@Override
		public int compare(BitArray v1, BitArray v2) {
			if (v1.getFixedSize() != v2.getFixedSize()) {
				return v1.getFixedSize() - v2.getFixedSize();
			}
			
			for (int i = v1.getFixedSize() - 1;i >= 0; --i) {
				if (v1.get(i) != v2.get(i)) {
					return v1.get(i) ? 1 : -1;
				}
			}
			
			return 0;
		}
		
	}
	
	protected boolean checkCompleteness(BitArray[] qSubset, BitArray[] vectorSet) {
		BitArray combination = new BitArray(dim);
		int q = qSubset.length;
		
		for (int n = 1;n < (1 << q); ++n) {
			int bit = GrayCode.getChangedPosition(n);
			
			combination.xor(qSubset[bit]);			
			if (Arrays.binarySearch(vectorSet, combination, new BitVectorComp()) < 0) {
				return false;
			}
		}
		
		return true;
	}
	
	private BitArray[] findTwo(BitArray[] vectorSet) {				
		for (int i = 0;i < vectorSet.length; ++i) {						
			for (int j = i + 1;j < vectorSet.length; ++j) {								
				BitArray[] basis = new BitArray[] { vectorSet[i], vectorSet[j] };
				
				if (checkCompleteness(basis, vectorSet)) {
					return basis;
				}
			}
		}
		
		return null;
	}
	
	private BitArray[] findByBruteForce(BitArray[] vectorSet, int q) {
		CEnumerator qEnum = new CEnumerator(vectorSet.length, q);
		
		while (qEnum.hasNext()) {
			long[] qInds = qEnum.next();
			BitArray[] qTuple = new BitArray[q];			
			
			for (int i = 0;i < q; ++i) {
				qTuple[i] = vectorSet[(int)qInds[i]];
			}
			
			if (checkCompleteness(qTuple, vectorSet)) {
				return qTuple;
			}
		}
				
		return null;
	}
	
	public BitArray[] findBasis(BitArray[] vectorSet, int q) {
		if (vectorSet.length < q) {
			return null;
		}
		
		if (q == 1) {
			if (vectorSet.length != 0) {
				return new BitArray[]{ vectorSet[0] };
			}
			
			return null;
		}
		
		dim = vectorSet[0].fixedSize;
		
		if (q == 2) {
			return findTwo(vectorSet);
		}
		
		if (new CEnumerator(vectorSet.length, q).count().compareTo(BigInteger.valueOf(10000000)) < 0) {
			return findByBruteForce(vectorSet, q);
		}
		
		return findBasisImpl(vectorSet, q);
	}
	
	protected abstract BitArray[] findBasisImpl(BitArray[] vectorSet, int q);
		
}
