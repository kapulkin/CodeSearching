package search_heuristics;

import math.ConvCodeAlgs;
import math.MinDistance;
import codes.Code;
import codes.ConvCode;
import codes.ZTCode;

public class CCGenRowsSumHeur implements IHeuristic {
	private int freeDist;
	private int rowsCount;
	
	public CCGenRowsSumHeur(int freeDist, int rowsCount) {
		this.freeDist = freeDist;
		this.rowsCount = rowsCount;
	}
	
	@Override
	public boolean check(Code code) {
		ConvCode _code = (ConvCode)code;
		int kblocksCount = (int)Math.ceil(((double)rowsCount / _code.getK())) * _code.getK();
		//ZTCode ztCode = new ZTCode(_code, kblocksCount - 1);
		ZTCode ztCode = (ZTCode) ConvCodeAlgs.truncate(kblocksCount, (kblocksCount / _code.getK() + _code.getDelay()) * _code.getN(), _code);
		
		if (MinDistance.findMinDist(ztCode.generator()) < freeDist) {
			return false;
		}
		
		return true;
	}
	
}
