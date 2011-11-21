package search_procedures.block_codes;

import java.util.ArrayList;

import codes.BlockCode;
import codes.ConvCode;
import search_heuristics.IHeuristic;
import search_procedures.ICodeEnumerator;

public class BlockCodesSearcher {		
	public static class SearchTask {
		public int K;
		public int N;
		public int MinDist;
		public int StateComplexity;
		public ICodeEnumerator<ConvCode> ConvCodeEnum;
		public IHeuristic Heuristic;
	}
	
	public static class TaskPool {
		public ArrayList<SearchTask> Tasks;
	}
	
	public BlockCode[] searchTruncatedCodes(TaskPool[] pools) {
		BlockCode[] codes = new BlockCode[pools.length];		
		
		for (int i = 0;i < pools.length; ++i) {
			ArrayList<SearchTask> tasks = pools[i].Tasks;
			
			for (int j = 0;j < tasks.size(); ++j) {				
				TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(tasks.get(j).ConvCodeEnum, tasks.get(j).K, tasks.get(j).N);
				BasicBlockCodesSearcher<BlockCode> searcher = new BasicBlockCodesSearcher<BlockCode>(tasks.get(j).MinDist, tasks.get(j).StateComplexity);
			
				searcher.setCandidateEnumerator(truncEnum);
				searcher.setHeuristic(tasks.get(j).Heuristic);
			
				if ((codes[i] = searcher.findNext()) != null) {
					break;
				}
			}
		}
		
		return codes;
	}
}
