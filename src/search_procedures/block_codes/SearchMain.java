package search_procedures.block_codes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import search_heuristics.CCFirstLastBlockStateHeur;
import search_heuristics.CCWeightsDistHeur;
import search_heuristics.CombinedHeuristic;
import search_heuristics.LRCCGreismerDistHeur;
import search_heuristics.TBWeightDistHeur;
import trellises.TrellisUtils;
import math.MinDistance;
import codes.BlockCode;

public class SearchMain {
	public static int[][] complexityLowerBounds;
	public static int[][] distanceUpperBounds;
	public static int[] maxDistanceUpperBound;
	
	public static int[][] complexitiesInPaper;
	public static int[][] distancesInPaper;
	
	public static void initDesiredParameters(String filename) throws FileNotFoundException{
		Scanner scanner = new Scanner(new FileReader(new File(filename)));
		StringTokenizer tokenizer = new StringTokenizer(scanner.nextLine(), " ()\n\r");
		int maxK = Integer.parseInt(tokenizer.nextToken()) + 1, maxN = Integer.parseInt(tokenizer.nextToken()) + 1;
				
		complexityLowerBounds = new int[maxK][maxN];
		distanceUpperBounds = new int[maxK][maxN];
		complexitiesInPaper = new int[maxK][maxN];
		distancesInPaper = new int[maxK][maxN];
		
		maxDistanceUpperBound = new int[maxK];
		
		while(scanner.hasNext()){
			tokenizer = new StringTokenizer(scanner.nextLine(), " ()\n\r");
			
			if(!tokenizer.hasMoreTokens())
				break;
			
			int k = Integer.parseInt(tokenizer.nextToken()), n = Integer.parseInt(tokenizer.nextToken());
			
			distancesInPaper[k][n] = Integer.parseInt(tokenizer.nextToken());
			distanceUpperBounds[k][n] = Integer.parseInt(tokenizer.nextToken());
			
			if(distanceUpperBounds[k][n] > maxDistanceUpperBound[k]){
				maxDistanceUpperBound[k] = distanceUpperBounds[k][n];
			}
			
			complexitiesInPaper[k][n] = Integer.parseInt(tokenizer.nextToken());
			complexityLowerBounds[k][n] = Integer.parseInt(tokenizer.nextToken());			
		}
	}
	
	private static void writeCodes(BlockCode[] codes, BufferedWriter writer) throws IOException{
		for(int i = 0;i < codes.length;i ++){
			if(codes[i] != null){				
				writer.write("k = " + codes[i].getK());
				writer.write(", n = " + codes[i].getN());
				writer.write(", d = " + codes[i].getMinDist());
				if(codes[i].getN() < distanceUpperBounds[codes[i].getK()].length){
					writer.write("(" + distanceUpperBounds[codes[i].getK()][codes[i].getN()] + "," + distancesInPaper[codes[i].getK()][codes[i].getN()] + ")");
				}
					writer.write(", s = " + TrellisUtils.stateComplexity(codes[i].getTrellis()));
				if(codes[i].getN() < distanceUpperBounds[codes[i].getK()].length){
					writer.write("(" + complexityLowerBounds[codes[i].getK()][codes[i].getN()] + "," + complexitiesInPaper[codes[i].getK()][codes[i].getN()] + ")");
				}
				writer.newLine();
				writer.write(MinDistance.findMinDist(codes[i].generator()));				
			}else{
				writer.write("null");
			}
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}
	
	public static void main(String[] args) throws IOException {
		initDesiredParameters("tb_codes_params.txt");
		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		BlockCodesSearcher.TaskPool[] pools = new BlockCodesSearcher.TaskPool[distanceUpperBounds.length - 3];
		
		for (int k = 3;k <= distanceUpperBounds.length - 1/*26*/; ++k) {						
			ArrayList<BlockCodesSearcher.SearchTask> tasks = new ArrayList<BlockCodesSearcher.SearchTask>();
			
			for (int ddelta = 0;ddelta < 3; ++ddelta) {
				for (int sdelta = 0;sdelta < 3; ++sdelta){					
					BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
					
					task.K = k;
					task.N = 2 * k;
					task.MinDist = distanceUpperBounds[k][2 * k] - ddelta;
					task.StateComplexity = complexityLowerBounds[k][2 * k] + sdelta;
			
					CombinedHeuristic heuristic = new CombinedHeuristic();
			
					//heuristic.addHeuristic(0, new CCWeightsDistHeur(tasks[k - 3].MinDist));
					//heuristic.addHeuristic(1, new CCFirstLastBlockStateHeur());
					//heuristic.addHeuristic(2, new LRCCGreismerDistHeur(tasks[k - 3].MinDist));
			
					task.ConvCodeEnum = new SiftingCCEnumerator(new ExhaustiveCCEnumByGenMatr(1, 2, task.StateComplexity), heuristic);
					task.Heuristic = null;//new TBWeightDistHeur(task.MinDist);
					tasks.add(task);
				}
			}
			
			pools[k - 3] = new BlockCodesSearcher.TaskPool();
			pools[k - 3].Tasks = tasks;
		}
				
		BlockCode[] codes = searcher.searchTruncatedCodes(pools);
		
		writeCodes(codes, new BufferedWriter(new FileWriter(new File("TBCodes.txt"))));
	}
}
