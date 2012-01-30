package search_procedures.block_codes;

import in_out_interfaces.DistanceBoundsParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import math.ConvCodeAlgs;
import math.EuclidAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_heuristics.BCPreciseMinDist;
import search_heuristics.BCPreciseStateComplexity;
import search_heuristics.CCFirstLastBlockStateHeur;
import search_heuristics.CCGenRowsSumHeur;
import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CCWeightsDistHeur;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_heuristics.LRCCGreismerDistHeur;
import search_heuristics.TBWeightDistHeur;
import search_procedures.block_codes.HighRateFieldSearcher.LinearDependenceCashingHeur;
import search_procedures.block_codes.HighRateFieldSearcher.PolyLinearDependenceDataBase;
import search_procedures.conv_codes.ExhaustiveCCEnumByGenMatr;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import search_procedures.conv_codes.SiftingCCEnumerator;
import search_procedures.tests.BasicBlockCodeSearcherTest;
import codes.BlockCode;
import codes.ConvCode;
import codes.TruncatedCode;

public class SearchMain {
	
	private static final int freqCPU = (int)2400e6;
	static final private Logger logger = LoggerFactory.getLogger(SearchMain.class);
	
	private static int estimateTaskTime(BlockCodesSearcher.SearchTask task, ExhaustiveHRCCEnumByCheckMatr ccEnum, IHeuristic ccHeur) {
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
	
	public static void main(String[] args) throws IOException {
		int[][] lowerBounds = DistanceBoundsParser.parse(false);
		int[][] upperBounds = DistanceBoundsParser.parse(true);
				
		BlockCodesTable.createTables(256, 256);
		BlockCodesTable.distanceUpperBounds = upperBounds;
		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		ArrayList<BlockCodesSearcher.TaskPool> pools = new ArrayList<BlockCodesSearcher.TaskPool>();
		PolyLinearDependenceDataBase parityCheckDataBase = new PolyLinearDependenceDataBase();
		
		for (int k = 1;k <= 40; ++k) {
			for (int n = 1;n <= Math.min(2 * k, 256); ++n) {
				if (lowerBounds[k][n] == upperBounds[k][n]) {
					continue;
				}
				
				int gcd = EuclidAlgorithm.gcd(k, n);
				int b = k / gcd;
				
				if (n / gcd != b + 1) {
					continue;
				}
				
				ArrayList<BlockCodesSearcher.SearchTask> tasks = new ArrayList<BlockCodesSearcher.SearchTask>();			
				
				BlockCodesTable.computeTBStateLowerBounds(b, b + 1);
				
				for (int d = upperBounds[k][n];d >= lowerBounds[k][n]; --d) {
					for (int sdelta = 0;sdelta < 3; ++sdelta) {					
						BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();						
						
						task.K = k;
						task.N = n;
						task.MinDist = d;
						task.StateComplexity = Math.max(BlockCodesTable.complexityLowerBounds[k][n] + sdelta, 1);
				
						CombinedHeuristic heuristic = new CombinedHeuristic();
				
						/*heuristic.addHeuristic(3, new CCWeightsDistHeur(task.MinDist));
						heuristic.addHeuristic(2, new CCFirstLastBlockStateHeur());
						heuristic.addHeuristic(1, new LRCCGreismerDistHeur(task.MinDist));
						//heuristic.addHeuristic(3, new CCGenRowsSumHeur(task.MinDist, 5));
						heuristic.addHeuristic(0, new CCPreciseFreeDist(task.MinDist));/**/
						
						heuristic.addHeuristic(1, new LinearDependenceCashingHeur(task.MinDist, Math.min(task.StateComplexity, 4), parityCheckDataBase));
						heuristic.addHeuristic(0, new CCPreciseFreeDist(task.MinDist));
				
						//task.ConvCodeEnum = new SiftingCCEnumerator(new ExhaustiveCCEnumByGenMatr(1, 2, task.StateComplexity), heuristic);
						ExhaustiveHRCCEnumByCheckMatr exhaustiveEnum = new ExhaustiveHRCCEnumByCheckMatr(b, task.StateComplexity);
						
						logger.debug("k=" + k + "n=" + n + ",s=" + task.StateComplexity + ",d=" + task.MinDist + ",count=" + exhaustiveEnum.count());					
						task.ConvCodeEnum = new SiftingCCEnumerator(exhaustiveEnum, heuristic);
						
						CombinedHeuristic tb_heuristic = new CombinedHeuristic();
						
						//tb_heuristic.addHeuristic(2, new TBWeightDistHeur(task.MinDist));
						tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(task.StateComplexity, false));
						tb_heuristic.addHeuristic(0, new BCPreciseMinDist(task.MinDist, false));
						
						task.Heuristic = tb_heuristic;
						task.ExpectedTime = estimateTaskTime(task, exhaustiveEnum, heuristic);//exhaustiveEnum.count().divide(BigInteger.valueOf(freqCPU)).intValue();
						
						logger.debug("time=" + task.ExpectedTime + "s");
						
						tasks.add(task);
					}
				}
				
				pools.add(new BlockCodesSearcher.TaskPool(tasks));
			}
		}
				
		BlockCode[] codes = searcher.searchTruncatedCodes(pools.toArray(new BlockCodesSearcher.TaskPool[0]));
		
		BlockCodesTable.writeCodes(codes, new BufferedWriter(new FileWriter(new File("TBCodes.txt"))));
	}
}
