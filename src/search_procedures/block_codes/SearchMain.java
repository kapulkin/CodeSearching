package search_procedures.block_codes;

import in_out_interfaces.DistanceBoundsParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import math.ConvCodeAlgs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_heuristics.BCPreciseMinDist;
import search_heuristics.BCPreciseStateComplexity;
import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_heuristics.LinearDependenceCashingHeur;
import search_procedures.EnumeratorLogger;
import search_procedures.ICodeEnumerator;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import search_procedures.conv_codes.FileCCEnumerator;
import search_procedures.conv_codes.SiftingCCEnumerator;
import codes.BlockCode;
import codes.ConvCode;
import codes.TruncatedCode;

public class SearchMain {
	
	private static final int freqCPU = (int)2400e6;
	static final private Logger logger = LoggerFactory.getLogger(SearchMain.class);
	
	public static int estimateTaskTime(BlockCodesSearcher.SearchTask task, ExhaustiveHRCCEnumByCheckMatr ccEnum, IHeuristic ccHeur) {
		int attempts = 10000;		
		long avgTime = 1;
		
		for (int i = 0;i < attempts; ++i) {
			ConvCode convCode = ccEnum.random();
			long startTime = System.currentTimeMillis();
			
			if (ccHeur.check(convCode)) {						
				TruncatedCode trncCode = ConvCodeAlgs.truncate(task.K, task.N, convCode);
			
				task.Heuristic.check(trncCode);
			}
			
			long endTime = System.currentTimeMillis();
			
			avgTime += endTime - startTime;
		}
		
		//avgTime /= attempts;
		
		BigInteger time = ccEnum.count().multiply(BigInteger.valueOf(avgTime)).divide(BigInteger.valueOf(1000 * attempts));
		
		if (time.bitCount() > Integer.SIZE) {
			return Integer.MAX_VALUE;
		}
		
		return time.intValue();
	} 
	
	private static void searchSingleCode(int k, int n, int s, int d, int kTrunc, ICodeEnumerator<ConvCode> ccEnum) throws IOException {
		BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
		BlockCodesSearcher.TaskPool pool = new BlockCodesSearcher.TaskPool();				
		
		task.K = k;
		task.N = n;
		task.MinDist = d;
		task.StateComplexity = s;
		
		TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(ccEnum, kTrunc, n);
		
		CombinedHeuristic tb_heuristic = new CombinedHeuristic();
		
		tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(s, true));
		tb_heuristic.addHeuristic(0, new BCPreciseMinDist(d, true));
		
		CosetCodeSearcher cosetSearcher = new CosetCodeSearcher(k, d);
		
		cosetSearcher.setHeuristic(tb_heuristic);
		cosetSearcher.setCandidateEnumerator(truncEnum);
		
		task.Algorithm = cosetSearcher;
		pool.Tasks.add(task);
		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		BlockCode[] codes = searcher.searchTruncatedCodes(new BlockCodesSearcher.TaskPool[] { pool });
		
		BlockCodesTable.writeCodes(codes, new BufferedWriter(new FileWriter(new File("TBCodes.txt"))));
	}
	
	public static void main(String[] args) throws IOException {
		/*CombinedHeuristic heuristic = new CombinedHeuristic();		
		
		heuristic.addHeuristic(1, new LinearDependenceCashingHeur(task.MinDist, Math.min(task.StateComplexity, 4), new LinearDependenceCashingHeur.PolyLinearDependenceDataBase()));
		heuristic.addHeuristic(0, new CCPreciseFreeDist(task.MinDist));

		ExhaustiveHRCCEnumByCheckMatr exhaustiveEnum = new ExhaustiveHRCCEnumByCheckMatr(6, task.StateComplexity, heuristic);
		SiftingCCEnumerator ccEnum = new SiftingCCEnumerator(new EnumeratorLogger<ConvCode>(exhaustiveEnum), heuristic);
		logger.info("count=" + exhaustiveEnum.count());/**/
		FileCCEnumerator ccEnum = new FileCCEnumerator("2&14&12.txt");
		
		searchSingleCode(43, 60, 15, 12, 40, ccEnum);
	}
}
