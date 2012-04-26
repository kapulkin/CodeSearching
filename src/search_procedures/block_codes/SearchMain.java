package search_procedures.block_codes;

import in_out_interfaces.DistanceBoundsParser;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOMatrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

import math.BitArray;
import math.BlockCodeAlgs;
import math.ConvCodeAlgs;
import math.Matrix;
import math.MaximalLinearSubspace;

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
	
	private static class CodeLogger implements ICodeEnumerator<BlockCode> {
		static final private Logger logger = LoggerFactory.getLogger(CodeLogger.class);
		private ICodeEnumerator<? extends BlockCode> actualEnumerator;
		private int count = 0;
		
		public CodeLogger(ICodeEnumerator<? extends BlockCode> actualEnumerator) {
			this.actualEnumerator = actualEnumerator;
		}
		
		@Override
		public void reset() {
			count = 0;
			actualEnumerator.reset();
		}

		@Override
		public BlockCode next() {
			BlockCode code = actualEnumerator.next();
			
			if (code != null) {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
				logger.info("Code #" + count);
				/*try {
					IOMatrix.writeMatrix(code.generator(), writer);
					logger.info("Min. distance: " + code.getMinDist());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}/**/	
				++count;
			}
			
			return code;
		}

		@Override
		public BigInteger count() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
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
	
	private static void searchSingleCode(int k, int n, int s, int d, int kTrunc, ICodeEnumerator<ConvCode> ccEnum) throws Exception {
		BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
		BlockCodesSearcher.TaskPool pool = new BlockCodesSearcher.TaskPool();				
		
		task.K = k;
		task.N = n;
		task.MinDist = d;
		task.StateComplexity = s;
		
		TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(ccEnum, kTrunc, n);
		CodeLogger loggerEnum = new CodeLogger(truncEnum);
		
		CombinedHeuristic tb_heuristic = new CombinedHeuristic();
		
		tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(s, false));
		tb_heuristic.addHeuristic(0, new BCPreciseMinDist(d, false));
		
		CosetCodeSearcher cosetSearcher = new CosetCodeSearcher(k, d);
		
		cosetSearcher.setHeuristic(tb_heuristic);
		cosetSearcher.setCandidateEnumerator(loggerEnum);
		
		task.Algorithm = cosetSearcher;
		pool.Tasks.add(task);
		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		BlockCode[] codes = searcher.searchTruncatedCodes(new BlockCodesSearcher.TaskPool[] { pool });
		
		BlockCodesTable.writeCodes(codes, new BufferedWriter(new FileWriter(new File("TBCodes.txt"))));
	}
	
	private static void findGoodTruncatedCodes(ICodeEnumerator<ConvCode> ccEnum) throws IOException {
		ConvCode code;
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("truncated_codes.txt")));
		
		int code_number = 0;
		while ((code = ccEnum.next()) != null) {
			try {
				for (int n = (int)Math.ceil((double)30 / code.getN()) * code.getN(); n < 100; n += code.getN()) {
					for (int k = (int)Math.ceil((double)10 / code.getK()) * code.getK(); k <= n * code.getK() / code.getN(); k += code.getK()) {
						if (n - k > 20) {
							continue;
						}
						
						logger.info("code={}", code_number);
						logger.info("k={},n={}", k, n);
						
						TruncatedCode truncCode = ConvCodeAlgs.truncate(k, n, code);
						BitArray[] fatSyndroms = BlockCodeAlgs.buildCosetsWithBigWeight(truncCode, truncCode.getMinDist());
						
						if(fatSyndroms.length != 0) {
							writer.write(Integer.toString(truncCode.getK()));
							writer.newLine();
							IOMatrix.writeMatrix(truncCode.generator(), writer);
							writer.flush();
						}
					}
				}
			} catch (Exception e) {
				
			}
			++code_number;
		}
	}
	
	private static void searchNewCodes() throws IOException {
		FileBCEnumerator bcEnum = new FileBCEnumerator("truncated_codes.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("new_codes.txt")));
		
		int[][] lowerBounds = DistanceBoundsParser.parse(false);
		int[][] upperBounds = DistanceBoundsParser.parse(true);
		
		BlockCodesTable.createTables(256, 256);
		BlockCodesTable.distanceUpperBounds = upperBounds;
		
		int b = 2;
		
		BlockCodesTable.computeTBStateLowerBounds(b, b + 1);
		
		MaximalLinearSubspace basisSearcher = new MaximalLinearSubspace();
		BlockCode code;
		while ((code = bcEnum.next()) != null) {
			try {
				logger.info("d={},lb={}", code.getMinDist(), lowerBounds[code.getK()][code.getN()]);
				logger.info("k={},n={}", code.getK(), code.getN());
				if (code.getMinDist() > lowerBounds[code.getK()][code.getN()]) {
					writer.write(Integer.toString(code.getK()));
					writer.newLine();
					IOMatrix.writeMatrix(code.generator(), writer);
					writer.flush();
				}
				
				for (int q = 1;q <= 3; ++q) {
					logger.info("q={},lb={}", q, lowerBounds[code.getK() + q][code.getN()]);
					if (code.getMinDist() > lowerBounds[code.getK() + q][code.getN()]) {
						logger.info("found");
						BitArray[] fatSyndroms = BlockCodeAlgs.buildCosetsWithBigWeight(code, code.getMinDist());
						BitArray[] syndroms = basisSearcher.findBasis(fatSyndroms, q);
						
						if (syndroms == null) {
							break;
						}
						
						Matrix newGenerator = new Matrix(code.getK() + q, code.getN());
						
						for (int i = 0;i < code.getK(); ++i) {
							newGenerator.setRow(i, code.generator().getRow(i));
						}		
						
						for (int i = code.getK();i < code.getK() + q; ++i) {
							BitArray cosetLeader = BlockCodeAlgs.findCosetLeader(code, syndroms[i - code.getK()]); 
							newGenerator.setRow(i, cosetLeader);
						}
						
						BlockCode extendedCode = new BlockCode(newGenerator, true);
						
						logger.info("writing");
						writer.write(Integer.toString(code.getK()));
						writer.newLine();
						IOMatrix.writeMatrix(extendedCode.generator(), writer);
						writer.flush();
					}
					
				}
			} catch (Exception e) {
				
			}
		}
				
	}
	
	public static void main(String[] args) throws Exception {
		/*CombinedHeuristic heuristic = new CombinedHeuristic();		
		
		heuristic.addHeuristic(1, new LinearDependenceCashingHeur(task.MinDist, Math.min(task.StateComplexity, 4), new LinearDependenceCashingHeur.PolyLinearDependenceDataBase()));
		heuristic.addHeuristic(0, new CCPreciseFreeDist(task.MinDist));

		ExhaustiveHRCCEnumByCheckMatr exhaustiveEnum = new ExhaustiveHRCCEnumByCheckMatr(6, task.StateComplexity, heuristic);
		SiftingCCEnumerator ccEnum = new SiftingCCEnumerator(new EnumeratorLogger<ConvCode>(exhaustiveEnum), heuristic);
		logger.info("count=" + exhaustiveEnum.count());/**/
		FileCCEnumerator ccEnum = new FileCCEnumerator("3&10&7.txt");
		
		searchSingleCode(48, 64, 140, 6, 48, ccEnum);
		//findGoodTruncatedCodes(ccEnum);
		//searchNewCodes();
	}
}
