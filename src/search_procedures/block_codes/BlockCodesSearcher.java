package search_procedures.block_codes;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import search_heuristics.IHeuristic;
import search_procedures.CodesBaseSearchScheme;
import search_procedures.ICodeEnumerator;
import search_procedures.tests.BasicBlockCodeSearcherTest;


public class BlockCodesSearcher {
	static final private Logger logger = LoggerFactory.getLogger(BasicBlockCodeSearcherTest.class);
	
	private int timeThreshold = 300;
	
	public static class SearchTask {
		public int K;
		public int N;
		public int MinDist;
		public int StateComplexity;
		public int ExpectedTime = -1;
		public ICodeEnumerator<ConvCode> ConvCodeEnum;
		public IHeuristic Heuristic;
		public CodesBaseSearchScheme<BlockCode> Algorithm;
	}
	
	public static class TaskPool {
		public ArrayList<SearchTask> Tasks = new ArrayList<BlockCodesSearcher.SearchTask>();
		
		public TaskPool(ArrayList<SearchTask> tasks) {
			Tasks = tasks;
		}

		public TaskPool() {
			
		}
	}	
	
	public BlockCode[] searchTruncatedCodes(TaskPool[] pools) {
		BlockCode[] codes = new BlockCode[pools.length];		
		
		for (int i = 0;i < pools.length; ++i) {
			ArrayList<SearchTask> tasks = pools[i].Tasks;
			
			for (int j = 0;j < tasks.size(); ++j) {
				SearchTask task = tasks.get(j);
				
				if (task.ExpectedTime > timeThreshold) {
					logger.info("task: " + "k=" + task.K + " n=" + task.N + " s=" + task.StateComplexity + " d=" + task.MinDist);
					logger.info("expected time " + task.ExpectedTime + "s is too big");
					continue;
				}
				
				/*TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(task.ConvCodeEnum, task.K, task.N);				
				BasicBlockCodesSearcher<BlockCode> searcher = new BasicBlockCodesSearcher<BlockCode>();
			
				searcher.setCandidateEnumerator(truncEnum);
				searcher.setHeuristic(task.Heuristic);/**/
				
				CodesBaseSearchScheme<BlockCode> searcher = task.Algorithm;
			
				if (task.ExpectedTime > 0) {
					logger.info("expected time " + task.ExpectedTime + "s");
				}
				
				if ((codes[i] = searcher.findNext()) != null) {
					logger.info("task: " + "k=" + task.K + " n=" + task.N + " s=" + task.StateComplexity + " d=" + task.MinDist);
					break;
				}
			}
		}
		
		return codes;
	}
}
