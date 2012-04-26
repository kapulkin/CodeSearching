package search_heuristics;

import codes.Code;
import codes.ConvCode;

public class CCPreciseFreeDist implements IHeuristic  {
	private int freeDist;
	
	public CCPreciseFreeDist(int expectedFreeDist) {
		this.freeDist = expectedFreeDist;
	}
	
	@Override
	public boolean check(Code code) {
		try {
			if (((ConvCode)code).getFreeDist() < freeDist) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
