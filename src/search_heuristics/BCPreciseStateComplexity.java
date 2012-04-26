package search_heuristics;

import trellises.TrellisUtils;
import codes.BlockCode;
import codes.Code;

public class BCPreciseStateComplexity implements IHeuristic {
	private int stateComplexity;
	private boolean checkGenRowIndependence;
	
	public BCPreciseStateComplexity(int expectedStateComplexity, boolean checkGenRowIndependence) {
		this.stateComplexity = expectedStateComplexity;
		this.checkGenRowIndependence = checkGenRowIndependence;
	}
	
	@Override
	public boolean check(Code code) {
		BlockCode _code = (BlockCode)code;
		
		if (checkGenRowIndependence) {
			try {
				_code.getGeneratorSpanForm();
			}catch(Exception e) {
				return false;
			}
		}
		
		try {
			return TrellisUtils.stateComplexity(_code.getTrellis()) <= stateComplexity;
		} catch (Exception e) {
			return false;
		}
	}

}
