package search_procedures.block_codes;

import in_out_interfaces.DistanceBoundsParser;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.math.BigInteger;
import java.util.Arrays;

import math.BitArray;
import math.BlockCodeAlgs;
import math.BlockMatrix;
import math.ConvCodeAlgs;
import math.MLSRandomMethod;
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
import search_procedures.CollectionEnumerator;
import search_procedures.EnumeratorLogger;
import search_procedures.ICodeEnumerator;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import search_procedures.conv_codes.FileCCEnumerator;
import search_procedures.conv_codes.SiftingCCEnumerator;
import search_tools.CEnumerator;
import search_tools.HammingBallEnumerator;
import codes.BlockCode;
import codes.ConvCode;
import codes.TruncatedCode;
import database.CodesMongoDB;

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
				try {
				//	IOMatrix.writeMatrix(code.generator(), writer);
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
	
	private static class FileBCEnumeratorWrapper implements ICodeEnumerator<TruncatedCode> {
		private FileBCEnumerator bcEnum;
		private int b;
		
		public FileBCEnumeratorWrapper(FileBCEnumerator bcEnum, int b) {
			this.bcEnum = bcEnum;
			this.b = b;	
		}

		@Override
		public void reset() {
			bcEnum.reset();			
		}

		@Override
		public TruncatedCode next() {
			BlockCode code = bcEnum.next();
			
			if (code == null) {
				return null;
			}
			
			BlockMatrix generator = new BlockMatrix(code.generator(), b, b + 1);
			int v = generator.getColumnCount() - 1;
			
			for (;v >= 0; --v) {
				if (!generator.get(0, v).isZero()) {
					break;
				}
			}
			
			logger.debug("" + v);
			
			Matrix[] genBlocks = new Matrix[v + 1];
			
			for (int i = 0;i <= v; ++i) {
				genBlocks[i] = generator.get(0, i);
			}
			
			ConvCode convCode = new ConvCode(genBlocks);
			
			convCode.parityCheck();
			return ConvCodeAlgs.truncate(code.getK(), code.getN(), convCode);
		}

		@Override
		public BigInteger count() {			
			return bcEnum.count();
		}
	}
	
	private static class SubcodesEnumerator implements ICodeEnumerator<BlockCode> {
		private ICodeEnumerator<? extends BlockCode> codeEnum;
		private BlockCode code;
		private HammingBallEnumerator rowsEnum;
		private int rowsToRemove;
		
		public SubcodesEnumerator(ICodeEnumerator<? extends BlockCode> codeEnum, int rowsToRemove) {
			this.codeEnum = codeEnum;		
			this.rowsToRemove = rowsToRemove;
			reset();
		}
		
		public SubcodesEnumerator(BlockCode code, int rowsToRemove) {
			this.code = code;
			this.rowsToRemove = rowsToRemove;
			reset();
		}
		
		@Override
		public void reset() {			
			//rowsEnum = new CEnumerator(code.getK(), rowsEnum.getK());
			if (codeEnum != null) {
				codeEnum.reset();
				code = codeEnum.next();
			}
			rowsEnum = new HammingBallEnumerator(code.getK(), rowsToRemove);
		}

		@Override
		public BlockCode next() {			
			if (!rowsEnum.hasNext()) {
				if (codeEnum == null) {
					return null;
				}
				
				code = codeEnum.next();
				
				if (code == null) {
					return null;
				}
				
				rowsEnum = new HammingBallEnumerator(code.getK(), rowsToRemove);				
			}
			
			long[] rows = rowsEnum.next();
			
			Matrix subcodeGen = new Matrix(code.getK() - rows.length, code.getN());
			int subcodeRow = 0;
			
			for (int row = 0; row < code.getK(); ++row) {
				if (Arrays.binarySearch(rows, row) < 0) {
					for (int i = 0;i < code.getN(); ++i) {
						subcodeGen.set(subcodeRow, i, code.generator().get(row, i));
					}
					++subcodeRow;
				}
			}
			
			return new BlockCode(subcodeGen, true);
		}

		@Override
		public BigInteger count() {			
			return rowsEnum.count();
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
	
	private static void searchSingleCode(int k, int n, int s, int d, ICodeEnumerator<? extends BlockCode> bcEnum) throws Exception {
		BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
		BlockCodesSearcher.TaskPool pool = new BlockCodesSearcher.TaskPool();				
		
		task.K = k;
		task.N = n;
		task.MinDist = d;
		task.StateComplexity = s;
		
		//TruncatedCodeEnumerator truncEnum = new TruncatedCodeEnumerator(ccEnum, kTrunc, n);
		CodeLogger loggerEnum = new CodeLogger(bcEnum);
		
		CombinedHeuristic tb_heuristic = new CombinedHeuristic();
		
		tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(s, false));
		tb_heuristic.addHeuristic(0, new BCPreciseMinDist(d, false));
		
		CosetCodeSearcher cosetSearcher = new CosetCodeSearcher(k, n, d);
		
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
		//BufferedWriter writer = new BufferedWriter(new FileWriter(new File("truncated_codes.txt")));
		CodesMongoDB db = new CodesMongoDB("truncated_codes");		
		int[][] lowerBounds = DistanceBoundsParser.parse(false);		
		
		BlockCodesTable.createTables(256, 256);
		db.clear();
		
		int code_number = 0;
		while ((code = ccEnum.next()) != null) {
			
			for (int n = (int)Math.ceil((double)30 / code.getN()) * code.getN(); n < 100; n += code.getN()) {
				for (int k = (int)Math.ceil((double)10 / code.getK()) * code.getK(); k <= n * code.getK() / code.getN(); k += code.getK()) {
					if (n - k > 20) {
						continue;
					}
					
					logger.info("code={}", code_number);
					logger.info("k={},n={}", k, n);
					
					TruncatedCode truncCode = ConvCodeAlgs.truncate(k, n, code);
					
					try {
						if (truncCode.getMinDist() < lowerBounds[k][n]) {
							continue;							
						}
						
						BitArray[] fatSyndroms = BlockCodeAlgs.buildCosetsWithBigWeight(truncCode, truncCode.getMinDist());
						
						if(fatSyndroms.length != 0) {
							db.addBlockCode(truncCode, true);							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			++code_number;
		}
	}
	
	private static void searchNewCodes() throws IOException {		
		CodesMongoDB truncDB = new CodesMongoDB("truncated_codes");
		CodesMongoDB newDB = new CodesMongoDB("new_codes");
		CollectionEnumerator<BlockCode> bcEnum = new CollectionEnumerator<BlockCode>(truncDB.getBlockCodes());
		
		int[][] lowerBounds = DistanceBoundsParser.parse(false);
		int[][] upperBounds = DistanceBoundsParser.parse(true);
		
		BlockCodesTable.createTables(256, 256);
		BlockCodesTable.distanceUpperBounds = upperBounds;
		
		int b = 3;
		
		BlockCodesTable.computeTBStateLowerBounds(b, b + 1);
		
		MaximalLinearSubspace basisSearcher = new MLSRandomMethod(1);
		BlockCode code;
		int codesFound = 0;
		int codesViewed = 0;
		while ((code = bcEnum.next()) != null) {			
			//logger.info("d={},lb={}", code.getMinDist(), lowerBounds[code.getK()][code.getN()]);
			logger.info("k={},n={}", code.getK(), code.getN());
			logger.info("codes={}", codesFound);
			logger.info("viewed={}", codesViewed++);
			
			SubcodesEnumerator subEnum = new SubcodesEnumerator(code, 1);
			BlockCode subcode;
			int subcodeViewed = 0;
			
			while((subcode = subEnum.next()) != null) {
				if (subcode.getN() - subcode.getK() > 20) {
					continue;
				}
				
				logger.info("viewed={}", codesViewed);
				logger.info("subcodesViewed={}", subcodeViewed++);
				
				try {
					if (subcode.getMinDist() > lowerBounds[subcode.getK()][subcode.getN()]) {
						newDB.addBlockCode(subcode, true);					
					}
				} catch (Exception e) {				
					e.printStackTrace();
					continue;
				}
				
				for (int q = 1;q <= 10; ++q) {
					logger.info("q={}", q);
					
					try {
						if (subcode.getMinDist() > lowerBounds[subcode.getK() + q][subcode.getN()]) {						
							BitArray[] fatSyndroms = BlockCodeAlgs.buildCosetsWithBigWeight(subcode, subcode.getMinDist());
							BitArray[] syndroms = basisSearcher.findBasis(fatSyndroms, q);
							
							if (syndroms == null) {
								break;
							}
							
							++codesFound;
							
							Matrix newGenerator = new Matrix(subcode.getK() + q, subcode.getN());
							
							for (int i = 0;i < subcode.getK(); ++i) {
								newGenerator.setRow(i, subcode.generator().getRow(i));
							}		
							
							for (int i = subcode.getK();i < subcode.getK() + q; ++i) {
								BitArray cosetLeader = BlockCodeAlgs.findCosetLeader(subcode, syndroms[i - subcode.getK()]); 
								newGenerator.setRow(i, cosetLeader);
							}
							
							BlockCode extendedCode = new BlockCode(newGenerator, true);
							
							logger.info("writing");
							newDB.addBlockCode(extendedCode, true);
							break;
						}
					} catch (Exception e) {					
						e.printStackTrace();
					}
					
				}
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
		/*int d;
		int k, n;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		StreamTokenizer tokenizer = new StreamTokenizer(in);
		
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('&', '&');
		
		System.out.println("k, n, d");
		
		tokenizer.nextToken();
		k = (int)tokenizer.nval;
		
		tokenizer.nextToken();
		n = (int)tokenizer.nval;
		
		tokenizer.nextToken();
		d = (int)tokenizer.nval;
		
		System.out.println("1 - truncate convolutional codes");
		System.out.println("2 - use already defined truncated codes");
		
		int choise;
		String filename;
		
		tokenizer.nextToken();
		choise = (int)tokenizer.nval;
				
		ICodeEnumerator<? extends BlockCode> bcEnum;
		
		if (choise == 1) {
			System.out.println("file name");
			
			tokenizer.nextToken();
			filename = tokenizer.sval;
			
			FileCCEnumerator ccEnum = new FileCCEnumerator(filename + ".txt");
			int kTrunc;
			
			System.out.println("truncate to");
			
			tokenizer.nextToken();
			kTrunc = (int)tokenizer.nval;
			
			bcEnum = new TruncatedCodeEnumerator(ccEnum, kTrunc, n);
		} else {
			int b;
			
			filename = "truncated_codes";
			System.out.println("b");			
			
			tokenizer.nextToken();
			b = (int)tokenizer.nval;
			
			bcEnum = new SubcodesEnumerator(new FileBCEnumeratorWrapper(new FileBCEnumerator(filename + ".txt"), b), 1);
			//bcEnum = new FileBCEnumeratorWrapper(new FileBCEnumerator(filename + ".txt"), b);
		}
				
		searchSingleCode(k, n, 140, d, bcEnum);/**/
		//FileCCEnumerator ccEnum = new FileCCEnumerator("2&10&10.txt");
		//CodesMongoDB db = new CodesMongoDB("truncated_codes");
		//CollectionEnumerator<ConvCode> ccEnum = new CollectionEnumerator<ConvCode>(new CodesMongoDB("convolutional_codes").getConvCodes(2, 3, 11, 10));
		//ConvCode code;
		
		//db.clear();
		//while ((code = ccEnum.next()) != null) {
		//	db.addConvCode(code, false);
		//}
		//findGoodTruncatedCodes(ccEnum);
		searchNewCodes();
	}
}
