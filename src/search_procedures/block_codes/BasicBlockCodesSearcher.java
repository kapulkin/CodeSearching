package search_procedures.block_codes;

import search_heuristics.IHeuristic;
import trellises.ITrellis;
import trellises.TrellisUtils;

import codes.BlockCode;

public class BasicBlockCodesSearcher<DesiredCode extends BlockCode> extends CodesBaseSearchScheme<DesiredCode> {
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
	protected DesiredCode process(DesiredCode candidate) {
		if (heuristic != null && !heuristic.check(candidate)) {
			return null;
		}
		
		try {
			candidate.getGeneratorSpanForm();
		}catch(Exception e) {
			return null;
		}
		
		ITrellis trellis = candidate.getTrellis();
		
		if (TrellisUtils.stateComplexity(trellis) > requiredStateComplexity) {
			return null;
		}
		
		if (candidate.getMinDist() < requiredMinDistance) {
			return null;
		}		
		
		return candidate;
	}
}
