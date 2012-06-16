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
import database.CodesMongoDB;

import search_heuristics.CCGenRowsSumHeur;
import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CCWeightsDistHeur;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_heuristics.LinearDependenceCashingHeur;
import search_procedures.EnumeratorLogger;
import search_procedures.ICodeEnumerator;
import sun.net.www.content.audio.wav;

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
	
	private static void highRateCCSearch(int k, int v, int d, boolean random) throws IOException {
		LinearDependenceCashingHeur.PolyLinearDependenceDataBase parityCheckDataBase = new LinearDependenceCashingHeur.PolyLinearDependenceDataBase();
		CombinedHeuristic heuristic = new CombinedHeuristic();		
		
		//heuristic.addHeuristic(3, new CCWeightsDistHeur(d));
		//heuristic.addHeuristic(2, new LinearDependenceCashingHeur(d, Math.min(v, 5), parityCheckDataBase));
		heuristic.addHeuristic(1, new CCGenRowsSumHeur(d, 10));
		heuristic.addHeuristic(0, new CCPreciseFreeDist(d));
		
		ICodeEnumerator<ConvCode> ccEnum;
		ExhaustiveHRCCEnumByCheckMatr exEnum = new ExhaustiveHRCCEnumByCheckMatr(k, v, heuristic);
		
		if (random) {
			RandomEnumerator randEnum = new RandomEnumerator(exEnum);
			ccEnum = randEnum;
		} else {
			ccEnum = exEnum;
		}
		
		CodesMongoDB db = new CodesMongoDB("convolutional_codes");
		EnumeratorLogger<ConvCode> _ccEnum = new EnumeratorLogger<ConvCode>(ccEnum, EnumeratorLogger.LoggingMode.TimeLogging);
		ConvCode code;
		long lastTimestamp = System.currentTimeMillis();
		int foundedCodes = 0;
		
		while ((code = _ccEnum.next()) != null) {
			try {					
				if (heuristic.check(code)){
					db.addConvCode(code, false);
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
	}
	
	private static String menu(int b, int v, int d, boolean random) {
		String menu = "1. Set target parameters (b=" + b + ",v=" + v + ",d=" + d + ")" + "\n" +
					  "		Total codes: " + new ExhaustiveHRCCEnumByCheckMatr(b, v, new CombinedHeuristic()).count() + "\n" +
					  "2. Set search type (" + (random ? "random" : "exhaustive") + ")" + "\n" +
					  "3. Run search";
		
		return menu;
	}
	
	public static void savePreferences(int b, int v, int d, boolean random) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("cc_search_settings.txt")));
			
			writer.write("" + b + " " + v + " " + d + " " + (random ? 1 : 0));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		int b = 2, v = 7, d = 7;
		boolean random = false;
		
		try {
			StreamTokenizer tokenizer = new StreamTokenizer(new FileReader(new File("cc_search_settings.txt")));
			
			tokenizer.nextToken();
			b = (int)tokenizer.nval;
			
			tokenizer.nextToken();
			v = (int)tokenizer.nval;
			
			tokenizer.nextToken();
			d = (int)tokenizer.nval;
			
			int type;
			
			tokenizer.nextToken();
			type = (int)tokenizer.nval;
			
			random = (type == 1);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		while (true) {			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			StreamTokenizer tokenizer = new StreamTokenizer(in);
			
			System.out.println(menu(b, v, d, random));
			
			int choise;
			
			tokenizer.nextToken();
			choise = (int)tokenizer.nval;
			
			if (choise == 1) {
				System.out.println("Setting b, v, d:");
				
				tokenizer.nextToken();
				b = (int)tokenizer.nval;
				
				tokenizer.nextToken();
				v = (int)tokenizer.nval;
				
				tokenizer.nextToken();
				d = (int)tokenizer.nval;
			} else if (choise == 2) {
				int type;
				
				System.out.println("Setting search type (0 - exhaustive, 1 - random):");
				
				tokenizer.nextToken();
				type = (int)tokenizer.nval;
				
				if (type == 0) {
					random = false;
				} else if (type == 1) {
					random = true;
				}
			} else if (choise == 3) {
				break;
			}
		}
		
		savePreferences(b, v, d, random);
		
		highRateCCSearch(b, v, d, random);
		
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
