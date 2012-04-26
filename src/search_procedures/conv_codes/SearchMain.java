package search_procedures.conv_codes;

import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import math.ConvCodeAlgs;

import codes.ConvCode;
import codes.TBCode;
import codes.TruncatedCode;
import codes.ZTCode;

import search_heuristics.CCGenRowsSumHeur;
import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CCWeightsDistHeur;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_heuristics.LinearDependenceCashingHeur;
import search_procedures.EnumeratorLogger;
import search_procedures.ICodeEnumerator;

public class SearchMain {
	static final private Logger logger = LoggerFactory.getLogger(SearchMain.class);

	private static class RandomEnumerator implements ICodeEnumerator<ConvCode> {		
		private ExhaustiveHRCCEnumByCheckMatr ccEnum;
		
		public RandomEnumerator(ExhaustiveHRCCEnumByCheckMatr ccEnum) {
			this.ccEnum = ccEnum;
		}
		
		@Override
		public void reset() {
			ccEnum.reset();
		}

		@Override
		public ConvCode next() {			
			return ccEnum.random();
		}

		@Override
		public BigInteger count() {			
			return ccEnum.count();
		}
		
	}
	
	private static void highRateCCSearch(int k, int v, int d) throws IOException {
		LinearDependenceCashingHeur.PolyLinearDependenceDataBase parityCheckDataBase = new LinearDependenceCashingHeur.PolyLinearDependenceDataBase();
		CombinedHeuristic heuristic = new CombinedHeuristic();		
		
		//heuristic.addHeuristic(3, new CCWeightsDistHeur(d));
		heuristic.addHeuristic(2, new CCGenRowsSumHeur(d, 10));
		heuristic.addHeuristic(1, new LinearDependenceCashingHeur(d, Math.min(v, 5), parityCheckDataBase));
		heuristic.addHeuristic(0, new CCPreciseFreeDist(d));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(k + "&" + v + "&" + d + ".txt"), true));
		ExhaustiveHRCCEnumByCheckMatr ccEnum = new ExhaustiveHRCCEnumByCheckMatr(k, v, heuristic);
		RandomEnumerator randEnum = new RandomEnumerator(ccEnum);
		EnumeratorLogger<ConvCode> _ccEnum = new EnumeratorLogger<ConvCode>(randEnum, EnumeratorLogger.LoggingMode.TimeLogging);
		ConvCode code;
		long lastTimestamp = System.currentTimeMillis();
		int foundedCodes = 0;
		
		while ((code = _ccEnum.next()) != null) {
			try {					
				if (heuristic.check(code)){
					IOConvCode.writeConvCode(code, writer, "pc");
					writer.flush();					
					++foundedCodes;
				}/**/
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			if (System.currentTimeMillis() - lastTimestamp > 1000) {
				lastTimestamp = System.currentTimeMillis();
				logger.info("codes found: " + foundedCodes);
			}
		}
		
		writer.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int v, d;
		int k;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		StreamTokenizer tokenizer = new StreamTokenizer(in);

		System.out.println("k");
		
		tokenizer.nextToken();
		k = (int)tokenizer.nval;
		
		System.out.println("v, d");
		
		tokenizer.nextToken();
		v = (int)tokenizer.nval;
		
		tokenizer.nextToken();
		d = (int)tokenizer.nval;
		
		highRateCCSearch(k, v, d);
		
		/*ConvCode convCode = IOConvCode.readConvCode(new Scanner(new FileReader(new File("3&10&10.txt"))));		

		IOPolyMatrix.writeMatrix(convCode.parityCheck(), System.out);
		//System.out.println(convCode.getDelay());
	//	System.out.println(convCode.getFreeDist());
		
		//TBCode tbCode = new TBCode(convCode, 50);
		TruncatedCode code = ConvCodeAlgs.truncate(57, 84, convCode);
		//ZTCode code = new ZTCode(convCode, 17); 
		
		System.out.println(code.getK());
		System.out.println(code.getN());
		//IOMatrix.writeMatrix(tbCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));		
		System.out.println(code.getMinDist());/**/
	}

}
