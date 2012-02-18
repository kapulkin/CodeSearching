package search_procedures.block_codes;

import math.BitArray;
import math.BlockCodeAlgs;
import math.Matrix;
import math.MaximalLinearSubspace;
import codes.BlockCode;
import codes.TruncatedCode;

public class CosetCodeSearcher extends BasicBlockCodesSearcher<BlockCode> {
	private int k;
	private int d;
	
	public CosetCodeSearcher(int k, int d) {
		this.k = k;
		this.d = d;
	}
	
	@Override
	protected BlockCode process(BlockCode candidate) {
		TruncatedCode subcode = (TruncatedCode)candidate;
		
		if (heuristic != null && !heuristic.check(subcode)) {
			return null;
		}		
		
		MaximalLinearSubspace basisSearcher = new MaximalLinearSubspace();		
		int q = k - subcode.getK();
		
		if (q == 0) {
			return subcode;
		}
		
		BitArray[] syndroms = basisSearcher.findBasis(BlockCodeAlgs.buildCosetsWithBigWeight(subcode, d), q);
		
		if (syndroms == null) {
			return null;
		}
		
		Matrix newGenerator = new Matrix(k, subcode.getN());
		
		for (int i = 0;i < subcode.getK(); ++i) {
			newGenerator.setRow(i, subcode.generator().getRow(i));
		}		
		
		for (int i = subcode.getK();i < k; ++i) {
			BitArray cosetLeader = BlockCodeAlgs.findCosetLeader(subcode, syndroms[i - subcode.getK()]); 
			newGenerator.setRow(i, cosetLeader);
		}
		
		BlockCode extendedCode = new BlockCode(newGenerator, true); 
		
		if (heuristic != null && !heuristic.check(extendedCode)) {
			return null;
		}		
		
		return extendedCode;
	}

}
