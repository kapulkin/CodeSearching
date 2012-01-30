package search_procedures.block_codes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

import math.MinDistance;
import trellises.TrellisUtils;
import codes.BlockCode;

public class BlockCodesTable {
	public static int[][] complexityLowerBounds;
	public static int[][] distanceUpperBounds;
	public static int[] maxDistanceUpperBound;
	
	public static int[][] complexitiesInPaper;
	public static int[][] distancesInPaper;
	
	
	public static void createTables(int maxK, int maxN) {
		complexityLowerBounds = new int[maxK + 1][maxN + 1];
		distanceUpperBounds = new int[maxK + 1][maxN + 1];
	}
	
	public static void computeTBStateLowerBounds(int b, int c) {
		for (int k = b;k < complexityLowerBounds.length; k += b) {
			int n = (k / b) * c;
			
			if (n >= complexityLowerBounds[k].length) {
				continue;
			}
			
			int bound = -1;
			
			for (int j = 1;j <= k; ++j) {
				int minDist = distanceUpperBounds[k][n];
				bound = Math.max(bound, (int)Math.ceil((double)k * MinDistance.minN(j, minDist) / n - j));
			}			
			
			complexityLowerBounds[k][n] = bound;
		}
	}
	
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
	
	public static void writeCodes(BlockCode[] codes, BufferedWriter writer) throws IOException{
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
}
