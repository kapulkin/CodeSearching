package search_procedures.block_codes;

import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import math.ConvCodeSpanForm.SpanFormException;
import math.MinDistance;

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
import search_procedures.conv_codes.ExhaustiveCCEnumByGenMatr;
import search_procedures.conv_codes.SiftingCCEnumerator;
import codes.Code;
import codes.ConvCode;
import codes.ZTCode;

public class DiscreditableCodesSearcher {
	static final private Logger logger = LoggerFactory.getLogger(DiscreditableCodesSearcher.class);
	
	static class StatisticsHeuristicWrapper implements IHeuristic {
		private IHeuristic heuristic;
		private int accepts = 0;
		private int rejects = 0;
		
		public StatisticsHeuristicWrapper(IHeuristic heuristic) {
			this.heuristic = heuristic;
		}
		
		public int getAccepts() {
			return accepts;
		}
		
		public int getRejects() {
			return rejects;
		}
		
		@Override
		public boolean check(Code code) {
			boolean res = heuristic.check(code);
			
			if (res) {
				++accepts;
			}else{
				++rejects;
			}
			
			return res;
		}
		
	}
	
	static class StupidCodeFinderHeur implements IHeuristic {
		private static final int BUFFER_SIZE = 10;
		private int freeDist;
		private int lowerBound;
		private TreeMap<Double, ConvCode> stupidCodes = new TreeMap<Double, ConvCode>();
		private int stupidCodesCount = 0;
		
		public StupidCodeFinderHeur(int freeDist, int lowerBound) {
			this.freeDist = freeDist;
			this.lowerBound = lowerBound;
		}
		
		public int getFreeDist() {
			return freeDist;
		}
		
		public int getLowerBound() {
			return lowerBound;
		}
		
		public int getStupidCodesCount() {
			return stupidCodesCount;
		}
		
		public ConvCode[] getStupidCodes() {
			return stupidCodes.values().toArray(new ConvCode[0]);
		}
		
		@Override
		public boolean check(Code code) {
			ConvCode _code = (ConvCode)code;
			
			try {
				if (_code.getFreeDist() < lowerBound) {
					stupidCodes.put((double)_code.getFreeDist() / freeDist, _code);
					if (stupidCodes.size() > BUFFER_SIZE) {
						stupidCodes.entrySet().iterator().remove();
					}
					++stupidCodesCount;
					return false;
				}/**/
				
				return _code.getFreeDist() >= freeDist;
			} catch (Exception e) {
				return false;
			}
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BlockCodesTable.initDesiredParameters("tb_codes_params.txt");
		
		int min_k = 15, max_k = 25;
		
		ArrayList<StupidCodeFinderHeur> stupidCodesStatistic = new ArrayList<StupidCodeFinderHeur>();		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		BlockCodesSearcher.TaskPool[] pools = new BlockCodesSearcher.TaskPool[max_k - min_k + 1];		
		
		for (int k = min_k;k <= max_k; ++k) {						
			ArrayList<BlockCodesSearcher.SearchTask> tasks = new ArrayList<BlockCodesSearcher.SearchTask>();
			
			for (int ddelta = 0;ddelta <= BlockCodesTable.distanceUpperBounds[k][2 * k] - BlockCodesTable.distancesInPaper[k][2 * k]; ++ddelta) {
				for (int sdelta = 0;sdelta <= BlockCodesTable.complexitiesInPaper[k][2 * k] - BlockCodesTable.complexityLowerBounds[k][2 * k]; ++sdelta) {					
					BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
					
					task.K = k;
					task.N = 2 * k;
					task.MinDist = BlockCodesTable.distanceUpperBounds[k][2 * k] - ddelta;
					task.StateComplexity = BlockCodesTable.complexityLowerBounds[k][2 * k] + sdelta;
			
					CombinedHeuristic heuristic = new CombinedHeuristic();
					StupidCodeFinderHeur stupidCodeFinder = new StupidCodeFinderHeur(task.MinDist, task.MinDist / 2);
					
					stupidCodesStatistic.add(stupidCodeFinder);
			
					heuristic.addHeuristic(4, new CCWeightsDistHeur(task.MinDist));
					heuristic.addHeuristic(3, new CCFirstLastBlockStateHeur());
					heuristic.addHeuristic(2, new LRCCGreismerDistHeur(task.MinDist));
					heuristic.addHeuristic(1, new CCGenRowsSumHeur(task.MinDist, 5));
					heuristic.addHeuristic(0, stupidCodeFinder);
					//heuristic.addHeuristic(3, new CCPreciseFreeDist(task.MinDist));
			
					task.ConvCodeEnum = new SiftingCCEnumerator(new ExhaustiveCCEnumByGenMatr(1, 2, task.StateComplexity), heuristic);
					
					CombinedHeuristic tb_heuristic = new CombinedHeuristic();
					
					tb_heuristic.addHeuristic(0, new TBWeightDistHeur(task.MinDist));
					tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(task.StateComplexity, true));
					tb_heuristic.addHeuristic(2, new BCPreciseMinDist(task.MinDist, true));
					
					task.Heuristic = tb_heuristic;
					tasks.add(task);
				}
			}
			
			pools[k - min_k] = new BlockCodesSearcher.TaskPool(tasks);
		}
				
		searcher.searchTruncatedCodes(pools);
		
		for (StupidCodeFinderHeur heur : stupidCodesStatistic) {
		
			logger.info("Stupid codes count: " + heur.getStupidCodesCount());
			
			for (ConvCode code : heur.getStupidCodes()) {			
				try {
					System.out.println(code.getFreeDist() + " " + heur.getFreeDist() + " " + MinDistance.findMinDist(new ZTCode(code, 5).generator()));
				} catch (SpanFormException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				IOPolyMatrix.writeMatrix(code.generator(),  System.out);
				IOMatrix.writeMatrix(new ZTCode(code, 5).generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
			}			
		}
	}

}
