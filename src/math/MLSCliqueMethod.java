package math;

import java.util.ArrayList;
import java.util.Arrays;

import search_tools.CEnumerator;

public class MLSCliqueMethod extends MaximalLinearSubspace {
	private final int MAX_SETSIZE = (1 << 8);

	@Override
	protected BitArray[] findBasisImpl(BitArray[] vectorSet, int q) {
		
		int qDiv3 = (int)Math.ceil((double)q / 3);
		CEnumerator qDiv3SubsetsEnum = new CEnumerator(vectorSet.length, qDiv3);
		ArrayList<long[]> qDiv3IndependentSequences = new ArrayList<long[]>();		
		
		int subsetsViewed = 0;
		while (qDiv3SubsetsEnum.hasNext()) {
			long[] qDiv3Subset = qDiv3SubsetsEnum.next();			
			BitArray[] qDiv3Sequence = new BitArray[qDiv3];
			
			++subsetsViewed;
			if (subsetsViewed > (1 << 25)) {
//				break;
			}
			
			for (int i = 0;i < qDiv3; ++i) {
				qDiv3Sequence[i] = vectorSet[(int)qDiv3Subset[i]];
			}
			
			if (checkCompleteness(qDiv3Sequence, vectorSet)) {
				qDiv3IndependentSequences.add(qDiv3Subset);
				if (qDiv3IndependentSequences.size() > MAX_SETSIZE) {
					break;
				}
			}
		}
		
		int subsetsCount = qDiv3IndependentSequences.size();
		
		if (subsetsCount < q) {
			return null;
		}
		
		int nearestPowerOfTwo = (subsetsCount & (subsetsCount - 1)) == 0 ? subsetsCount : Integer.highestOneBit(subsetsCount) * 2; 
		double[][] graphOfSubsets = new double[nearestPowerOfTwo][nearestPowerOfTwo];
		boolean[][] independenceFlags = new boolean[subsetsCount][subsetsCount];
		
		for (int indA = 0;indA < subsetsCount; ++indA){
			long[] qDiv3SubsetA = qDiv3IndependentSequences.get(indA);			
			ArrayList<BitArray> q2Div3Sequence = new ArrayList<BitArray>();
			
			for (int i = 0;i < qDiv3; ++i) {
				q2Div3Sequence.add(vectorSet[(int)qDiv3SubsetA[i]]);
			}
			
			graphOfSubsets[indA][indA] = 0.0;
			
			for (int indB = indA + 1;indB < subsetsCount; ++indB) {
				long[] qDiv3SubsetB = qDiv3IndependentSequences.get(indB);
				
				q2Div3Sequence = new ArrayList<BitArray>(q2Div3Sequence.subList(0, qDiv3));
				
				for (int i = 0;i < qDiv3; ++i) {
					if (Arrays.binarySearch(qDiv3SubsetA, qDiv3SubsetB[i]) < 0) {
						q2Div3Sequence.add(vectorSet[(int)qDiv3SubsetB[i]]);
					}
				}
				
				if (q2Div3Sequence.size() < 2 * qDiv3 - 1) {
					graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 0.0;
					independenceFlags[indA][indB] = independenceFlags[indB][indA] = false; 
					continue;
				}				
				
				graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 0.0;
				independenceFlags[indA][indB] = independenceFlags[indB][indA] = false;
				
				if (3 * qDiv3 == q + 2) {
					if (q2Div3Sequence.size() != 2 * qDiv3 - 1) {						
						continue;
					}
					graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 1.0;
					independenceFlags[indA][indB] = independenceFlags[indB][indA] = true;
				} else if (3 * qDiv3 == q + 1) {
					if (q2Div3Sequence.size() == 2 * qDiv3 - 1) {
						graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 1.0;						
					} else {
						independenceFlags[indA][indB] = independenceFlags[indB][indA] = true;
					}
				} else {
					if (q2Div3Sequence.size() != 2 * qDiv3) {
						continue;
					}
					graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 1.0;
					independenceFlags[indA][indB] = independenceFlags[indB][indA] = true;
				}
				
				if (!checkCompleteness(q2Div3Sequence.toArray(new BitArray[0]), vectorSet)) {
					graphOfSubsets[indA][indB] = graphOfSubsets[indB][indA] = 0.0;
					independenceFlags[indA][indB] = independenceFlags[indB][indA] = false;
					continue;
				}
			}		
		}
		
		double[][] squaredGraphOfSubsets = Strassen.multiply(graphOfSubsets, graphOfSubsets.clone());
		
		for (int indA = 0;indA < subsetsCount; ++indA) {
			long[] qDiv3SubsetA = qDiv3IndependentSequences.get(indA);
			ArrayList<BitArray> qBasis = new ArrayList<BitArray>();
			
			for (int i = 0;i < qDiv3; ++i) {
				qBasis.add(vectorSet[(int)qDiv3SubsetA[i]]);
			}
			
			for (int indB = indA + 1;indB < subsetsCount; ++indB) {
				if (independenceFlags[indA][indB] && squaredGraphOfSubsets[indA][indB] == 1.0) {					
					long[] qDiv3SubsetB = qDiv3IndependentSequences.get(indB);
					
					qBasis = new ArrayList<BitArray>(qBasis.subList(0, qDiv3));
					
					for (int i = 0;i < qDiv3; ++i) {
						if (Arrays.binarySearch(qDiv3SubsetA, qDiv3SubsetB[i]) < 0) {
							qBasis.add(vectorSet[(int)qDiv3SubsetB[i]]);
						}
					}
					
					int qBasisSize = qBasis.size();
					for (int indC = 0;indC < subsetsCount; ++indC) {
						if (graphOfSubsets[indA][indC] == 1.0 && graphOfSubsets[indC][indB] == 1.0) {
							long[] qDiv3SubsetC = qDiv3IndependentSequences.get(indC);
							
							qBasis = new ArrayList<BitArray>(qBasis.subList(0, qBasisSize));
							
							for (int i = 0;i < qDiv3; ++i) {
								if (Arrays.binarySearch(qDiv3SubsetA, qDiv3SubsetC[i]) < 0 &&
										Arrays.binarySearch(qDiv3SubsetB, qDiv3SubsetC[i]) < 0) {
									qBasis.add(vectorSet[(int)qDiv3SubsetC[i]]);
								}
							}
							
							//System.out.println(Boolean.toString(checkCompleteness(qBasis.toArray(new BitArray[0]), vectorSet)));
							
							if (checkCompleteness(qBasis.toArray(new BitArray[0]), vectorSet)) {
								return qBasis.toArray(new BitArray[0]);
							}
						}
					}
				}
			}
		}
		
		return null;
	}

}
