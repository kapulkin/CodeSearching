package search_procedures.block_codes;

import codes.ConvCode;
import search_heuristics.IHeuristic;
import search_procedures.conv_codes.IConvCodeEnumerator;

public class SiftingCCEnumerator implements IConvCodeEnumerator {
	private IHeuristic heuristic;	
	private IConvCodeEnumerator ccEnum;
	
	public SiftingCCEnumerator(IConvCodeEnumerator ccEnum, IHeuristic heuristic) {
		this.ccEnum = ccEnum;		
		this.heuristic = heuristic;
	}
	
	//20100001430 

	@Override
	public void reset() {
		ccEnum.reset();		
	}

	@Override
	public ConvCode next() {
		ConvCode convCode;
		
		while ((convCode = ccEnum.next()) != null) {									
			if (!heuristic.check(convCode)) {
				continue;
			}			
			
			return convCode;
		}
		
		return null;
	}
}
