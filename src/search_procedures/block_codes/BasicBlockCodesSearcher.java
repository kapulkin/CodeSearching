package search_procedures.block_codes;

import search_heuristics.IHeuristic;
import trellises.Trellis;

import codes.BlockCode;

public class BasicBlockCodesSearcher extends BlockCodesBaseSearchScheme {
	private IHeuristic heuristic;
	private int requiredMinDistance;
	private int requiredStateComplexity;
	
	public BasicBlockCodesSearcher(int requiredMinDistance, int requiredStateComplexity) {
		this.requiredMinDistance = requiredMinDistance;
		this.requiredStateComplexity = requiredStateComplexity;
	}
	
	public void setHeuristic(IHeuristic heuristic) {
		this.heuristic = heuristic;		
	}
	
	@Override
	protected BlockCode process(BlockCode candidate) {
		if (heuristic != null && !heuristic.check(candidate)) {
			return null;
		}
		
		Trellis trellis = candidate.getTrellis();
		
		if (trellis.stateComplexity() > requiredStateComplexity) {
			return null;
		}
		
		if (candidate.getMinDist() < requiredMinDistance) {
			return null;
		}		
		
		return candidate;
	}

}
