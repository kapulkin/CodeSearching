package search_heuristics;

import codes.BlockCode;
import codes.Code;

public class BCPreciseMinDist implements IHeuristic {
	private int minDist;
	private boolean checkGenRowIndependence; 
	
	public BCPreciseMinDist(int expectedMinDist, boolean checkGenRowIndependence) {
		this.minDist = expectedMinDist;
		this.checkGenRowIndependence = checkGenRowIndependence;
	}
	
	@Override
	public boolean check(Code code) {
		BlockCode _code = (BlockCode)code;
		
		if (checkGenRowIndependence){
			try {
				_code.getGeneratorSpanForm();
			}catch(Exception e) {
				return false;
			}
		}
		
		return _code.getMinDist() >= minDist;
	}

}
