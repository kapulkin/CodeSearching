package search_heuristics;

import codes.Code;
import codes.ConvCode;
import codes.TBCode;

public class TBWeightDistHeur implements IHeuristic {
	private int minDist; 
	
	public TBWeightDistHeur(int minDist) {
		this.minDist = minDist;
	}
	
	@Override
	public boolean check(Code code) {
		TBCode tbCode = (TBCode)code;
		ConvCode parentCode = tbCode.getParentCode();
		int cycles = tbCode.getN() / parentCode.getN();
		int weight = 0;
		
		for (int i = 0;i < parentCode.getN(); ++i) {
			boolean bit = false;
			
			for (int j = 0;j < parentCode.getDelay() + 1; ++j) {
				bit ^= parentCode.generator().get(0, i).getCoeff(j);
			}
			
			if (bit) {
				++weight;
			}
		}
		
		if (weight * cycles < minDist) {
			return false;
		}
		
		return true;
	}

}
