package search_procedures.block_codes;

import search_heuristics.IHeuristic;
import search_procedures.CodesBaseSearchScheme;

import codes.BlockCode;

public class BasicBlockCodesSearcher<DesiredCode extends BlockCode> extends CodesBaseSearchScheme<DesiredCode> {
	protected IHeuristic heuristic;	
	
	public BasicBlockCodesSearcher() {
		
	}
	
	public void setHeuristic(IHeuristic heuristic) {
		this.heuristic = heuristic;		
	}
	
	@Override
	protected DesiredCode process(DesiredCode candidate) {
		if (heuristic != null && !heuristic.check(candidate)) {
			return null;
		}		
		
		return candidate;
	}
}
