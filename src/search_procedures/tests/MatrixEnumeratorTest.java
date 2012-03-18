package search_procedures.tests;

import static org.junit.Assert.*;

import in_out_interfaces.IOConvCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import math.Matrix;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import codes.ConvCode;

import search_heuristics.CombinedHeuristic;
import search_heuristics.LinearDependenceCashingHeur;
import search_procedures.block_codes.BlockCodesTable;
import search_procedures.block_codes.SearchMain;
import search_procedures.conv_codes.ExhaustiveCCEnumByGenMatr;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import search_tools.MatrixEnumerator;

public class MatrixEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(MatrixEnumeratorTest.class);

	@Test
	public void numberOfMatrices(){
		int columns = 4;
		int rows = 5;
		MatrixEnumerator enumerator = new MatrixEnumerator(rows, columns);
		int expectedNumber = enumerator.count().intValue();
		int actualNumber = 0;		
		
		logger.debug("expected: " + expectedNumber);
		
		while (enumerator.hasNext()) {
			enumerator.getNext();
			++actualNumber;
		}
		
		logger.debug("actual: " + actualNumber);
		assertEquals(expectedNumber, actualNumber);
	}
	
	//@Test
	public void performanceTest() throws IOException {
		BlockCodesTable.initDesiredParameters("tb_code_paramsHR.txt");
		
		int minK = 3, maxK = BlockCodesTable.complexitiesInPaper.length - 1;
		
		for (int k = minK;k <= maxK; k += 3) {			
			int n = 4 * (k / 3);
			ExhaustiveHRCCEnumByCheckMatr cc_enum = new ExhaustiveHRCCEnumByCheckMatr(3, BlockCodesTable.complexitiesInPaper[k][n], new CombinedHeuristic());
			int cnt = 0;
						
			while (cc_enum.next() != null) {				
				++cnt;
			}
			
			logger.info("k = " + k + ", count " + Integer.toString(cnt));
		}
	}
	
	//@Test
	public void randomMatrixTest() {
		int columns = 5;
		int rows = 5;
		int tests = 1000;
		int succ_tests = 0;
		MatrixEnumerator enumerator = new MatrixEnumerator(rows, columns);
		Matrix[] randomMatrix = new Matrix[tests];
		boolean[] found = new boolean[tests];
		
		for (int i = 0;i < tests; ++i) {
			randomMatrix[i] = getRandomMatrix(rows, columns);
		}
		
		while (enumerator.hasNext()) {
			Matrix mat = enumerator.getNext();
			
			for (int i = 0;i < tests; ++i) {
				if (randomMatrix[i].equals(mat) && !found[i]) {
					found[i] = true;
					succ_tests++;
				}
			}
		}
		
		logger.debug("tests: " + tests);
		logger.debug("succsessful tests: " + succ_tests);
	}
	
	//@Test
	public void codesFromPaperTest() throws IOException {
		BlockCodesTable.initDesiredParameters("tb_codes_params.txt");
		
		int minK = 3, maxK = BlockCodesTable.complexitiesInPaper.length - 1;
		Scanner scanner = new Scanner(new File("HRconv_codes_for_tb_truncation.txt"));
		
		for (int k = minK;k <= maxK; ++k) {
			ConvCode gain_code = IOConvCode.readConvCode(scanner);
			ExhaustiveHRCCEnumByCheckMatr cc_enum = new ExhaustiveHRCCEnumByCheckMatr(gain_code.getK(), gain_code.getDelay(), new CombinedHeuristic());
			
			gain_code.parityCheck().sortColumns();
			
			ConvCode code = null;
			while ((code = cc_enum.next()) != null) {				
				if (code.parityCheck().equals(gain_code.parityCheck())) {
					break;
				}
			}
			
			if (code == null) {
				fail("code k = " + k + " missed");
			}
		}
	}
		
	public Matrix getRandomMatrix(int rows, int columns) {
		Random randomGenerator = new Random();
		Matrix randomMatrix = new Matrix(rows, columns);
		int[] countForColumns = new int[1 << rows];
		
		for (int i = 0;i < columns; ++i) {
			countForColumns[Math.abs(randomGenerator.nextInt()) % countForColumns.length] ++;			
		}
		
		int column = 0;
		for (int value = 0;value < countForColumns.length; ++value) {
			for (int j = 0;j < countForColumns[value]; ++j) {
				for (int k = 0;k < rows; ++k) {
					randomMatrix.set(k, column, (value & (1 << k)) != 0);
				}
				column ++;
			}
		}
		
		return randomMatrix;
	}
	
	public int combination(int n, int k){
		int num = 1;
		
		for (int v = n - k + 1;v <= n; ++v){
			num *= v;
		}
		
		for (int v = 2;v <= k; ++v){
			num /=v;
		}
		
		return num;
	}
}
