package math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.MaximalLinearSubspace.BitVectorComp;

import search_procedures.block_codes.SearchMain;
import search_tools.CEnumerator;

public class MLSRandomMethod extends MaximalLinearSubspace {
	static final private Logger logger = LoggerFactory.getLogger(SearchMain.class);
	private static Random rnd = new Random();
	private int iterations_count = 1000;
	private int subset_size = 1500;
	
	public MLSRandomMethod(int iterations_count) {
		this.iterations_count = iterations_count;
	}
	
	private boolean checkAllSums(int[] basis, BitArray ne, BitArray[] vectorSet) {
		BitArray combination = ne.clone();
		int q = basis.length;
		
		for (int n = 1;n < (1 << q); ++n) {
			int bit = GrayCode.getChangedPosition(n);
			
			combination.xor(vectorSet[basis[bit]]);
			if (Arrays.binarySearch(vectorSet, combination, new BitVectorComp()) < 0) {
				return false;
			}
		}
		
		return true;
	}
	
	private int[] bruteForceSearch(int[] basis, BitArray[] subSet, BitArray[] vectorSet, int q) {
		if (basis.length == q) {
			return basis;
		}
		
		int[] ex_basis = new int[basis.length + 1];
		int initialValue = -1;
		
		if (basis.length == 0) {
			initialValue = 0;
		} else {
			for (int i = 0;i < basis.length; ++i) {
				ex_basis[i] = basis[i];
			}
			initialValue = basis[basis.length - 1] + 1; 
		}
		
		for (int ne = initialValue; ne < subSet.length - (q - basis.length); ++ne) {
			if (checkAllSums(basis, vectorSet[ne], vectorSet)) {
				ArrayList<BitArray> siftedSubset = new ArrayList<BitArray>();
				
				ex_basis[basis.length] = ne;
				for (int i = 0;i < subSet.length; ++i) {
					if (checkAllSums(ex_basis, subSet[i], vectorSet)) {
						siftedSubset.add(subSet[i]);
					}
				}
				
				return bruteForceSearch(ex_basis, siftedSubset.toArray(new BitArray[0]), vectorSet, q);
			}
		}
		
		return null;
	}
	
	@Override
	protected BitArray[] findBasisImpl(BitArray[] vectorSet, int q) {
		CEnumerator subsetGen = new CEnumerator(vectorSet.length, Math.min(subset_size, vectorSet.length));
		
		for (int it = 0;it < iterations_count; ++it) {
			long[] subsetIndices = subsetGen.random();
			BitArray[] subset = new BitArray[subsetIndices.length];
			
			logger.debug("Iteration №" + (it+1));
			
			for (int i = 0;i < subsetIndices.length; ++i) {
				subset[i] = vectorSet[(int)subsetIndices[i]];
			}
			
			subset = vectorSet.clone();
			
			int[] basis = bruteForceSearch(new int[0], subset, vectorSet, q);
			
			if (basis != null) {
				BitArray[] _basis = new BitArray[q];
				
				for (int i = 0;i < q; ++i) {
					_basis[i] = vectorSet[basis[i]];
				}
				
				return _basis;
			}
		}
		
		return null;
	}

}
