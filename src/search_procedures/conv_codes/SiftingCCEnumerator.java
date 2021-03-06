package search_procedures.conv_codes;

import java.math.BigInteger;

import codes.ConvCode;
import search_heuristics.IHeuristic;
import search_procedures.ICodeEnumerator;

public class SiftingCCEnumerator implements ICodeEnumerator<ConvCode> {
	private IHeuristic heuristic;	
	private ICodeEnumerator<ConvCode> ccEnum;
	
	public SiftingCCEnumerator(ICodeEnumerator<ConvCode> ccEnum, IHeuristic heuristic) {
		this.ccEnum = ccEnum;		
		this.heuristic = heuristic;
	}

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

	@Override
	public BigInteger count() {		
		return ccEnum.count();
	}
}
