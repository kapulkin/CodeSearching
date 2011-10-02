package search_heuristics;

import math.Matrix;
import codes.Code;
import codes.ConvCode;

public class LRCCGreismerDistHeur implements IHeuristic {
	private int freeDist;
	
	public LRCCGreismerDistHeur(int freeDist) {
		this.freeDist = freeDist;
	}

	@Override
	public boolean check(Code code) {
		ConvCode convCode = (ConvCode)code;
		Matrix[] genBlocks = convCode.getGenBlocks();
		int n0 = 0, n1 = 0;
		
		for (int i = 0;i < convCode.getN(); ++i) {
			if (!genBlocks[0].get(0, i)) {
				++n0;
			}
			if (!genBlocks[convCode.getDelay()].get(0, i)) {
				++n1;
			}
		}
		
		for (int j = 1;j < Math.log(freeDist) / Math.log(2); ++j) {
			int sum = 0;
			
			for (int i = 0;i < j; ++i) {
				sum += Math.ceil( freeDist / Math.pow(2, i));
			}
			
			if (sum > (convCode.getDelay() + j) * convCode.getN() - n0 - n1) {
				return false;
			}
		}
		
		return true;
	}
}
