package search_procedures.block_codes;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import search_heuristics.IHeuristic;
import search_procedures.conv_codes.IConvCodeEnumerator;
import search_procedures.tests.BasicBlockCodeSearcherTest;

public class BlockCodesSearcher {
	static final private Logger logger = LoggerFactory.getLogger(BasicBlockCodeSearcherTest.class);
	
	public static class SearchTask {
		public int K;
		public int N;
		public int MinDist;
		public int StateComplexity;
		public IConvCodeEnumerator ConvCodeEnum;
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
				SearchTask task = tasks.get(j);
				TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(task.ConvCodeEnum, task.K, task.N);
				BasicBlockCodesSearcher searcher = new BasicBlockCodesSearcher(task.MinDist, task.StateComplexity);
			
				searcher.setCandidateEnumerator(truncEnum);
				searcher.setHeuristic(task.Heuristic);
			
				if ((codes[i] = searcher.findNext()) != null) {
					logger.info("task: " + "k=" + task.K + " n=" + task.N + " s=" + task.StateComplexity + " d=" + task.MinDist);
					break;
				}
			}
		}
		
		return codes;
	}
}
