package search_heuristics;

import math.Matrix;
import codes.Code;
import codes.ConvCode;

public class CCFirstLastBlockStateHeur implements IHeuristic {

	@Override
	public boolean check(Code code) {
		ConvCode convCode = (ConvCode)code;
		Matrix[] genBlocks = convCode.getGenBlocks();
		
		if (genBlocks[0].isZero() || genBlocks[convCode.getDelay()].isZero()) {
			return false;
		}
		
		return true;
	}
	
	
}
