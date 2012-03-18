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
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

import math.ConvCodeAlgs;

import codes.ConvCode;
import codes.TBCode;
import codes.TruncatedCode;
import codes.ZTCode;

import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_heuristics.LinearDependenceCashingHeur;
import search_procedures.EnumeratorLogger;
import search_procedures.ICodeEnumerator;

public class SearchMain {

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
		
//		heuristic.addHeuristic(1, new LinearDependenceCashingHeur(d, Math.min(v, 4), parityCheckDataBase));
//		heuristic.addHeuristic(0, new CCPreciseFreeDist(d));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(k + "&" + v + "&" + d + ".txt")));
		ExhaustiveHRCCEnumByCheckMatr ccEnum = new ExhaustiveHRCCEnumByCheckMatr(k, v, heuristic);
		EnumeratorLogger<ConvCode> _ccEnum = new EnumeratorLogger<ConvCode>(ccEnum, EnumeratorLogger.LoggingMode.TimeLogging);
		ConvCode code;
		
		while ((code = _ccEnum.next()) != null) {
			/*if (heuristic.check(code)){
				IOConvCode.writeConvCode(code, writer, "pc");
				writer.close();
			}/**/
		}
		
		writer.close();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		highRateCCSearch(2, 13, 11);
		
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
