package search_procedures.tests;

import static org.junit.Assert.*;

import in_out_interfaces.IOConvCode;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

import math.BlockMatrix;
import math.Matrix;
import math.Poly;
import math.PolyMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;

import search_procedures.block_codes.ExhaustiveCCEnumByGenMatr;
import search_procedures.block_codes.SearchMain;
import search_tools.MatrixEnumerator;

public class MatrixEnumeratorTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void numberOfMatrices(){
		int columns = 5;
		int rows = 5;
		MatrixEnumerator enumerator = new MatrixEnumerator(rows, columns);
		int expectedNumber = 0;
		int actualNumber = 0;
		
		for (int c = 1; c <= Math.min((1 << rows), columns); ++c) {
			int differentColumnCombination = combination((1 << rows), c);
			int finalCombination = combination(columns - 1, c - 1);
			
			expectedNumber += differentColumnCombination * finalCombination;
		}
		
		logger.debug("expected: " + expectedNumber);
		
		while (enumerator.hasNext()) {
			enumerator.getNext();
			++actualNumber;
		}
		
		logger.debug("actual: " + actualNumber);
		assertEquals(expectedNumber, actualNumber);
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
	
	@Test
	public void codesFromPaperTest() throws IOException {
		SearchMain.initDesiredParameters("tb_codes_params.txt");
		
		int minK = 3, maxK = SearchMain.complexitiesInPaper.length - 1;
		Scanner scanner = new Scanner(new File("conv_codes_for_tb_truncation.txt"));
		
		for (int k = minK;k <= maxK; ++k) {
			ConvCode gain_code = sortColumns(IOConvCode.readConvCode(scanner));
			ExhaustiveCCEnumByGenMatr cc_enum = new ExhaustiveCCEnumByGenMatr(gain_code.getK(), gain_code.getN(), gain_code.getDelay());
			
			ConvCode code = null;
			while ((code = cc_enum.next()) != null) {
				if (code.generator().equals(gain_code.generator())) {
					break;
				}
			}
			
			if (code == null) {
				fail("code k = " + k + " missed");
			}
		}
	}
	
	private int compareColumns(int i, int j, int delay, PolyMatrix mat) {
		for (int k = mat.getRowCount() - 1;k >= 0; --k) {
			for (int c = delay; c >= 0; --c) {
				Poly pi = mat.get(k, i);
				Poly pj = mat.get(k, j);
				boolean bi = pi.getDegree() < c ? false : pi.getCoeff(c);
				boolean bj = pj.getDegree() < c ? false : pj.getCoeff(c);
				
				if (bi && !bj) {
					return 1;
				} else if (!bi && bj) {
					return -1;
				}
			}
		}
		
		return 0;
	}
	
	private void swapColumns(int i, int j, PolyMatrix mat) {
		for (int k = 0;k < mat.getRowCount(); ++k) {
			Poly b = mat.get(k, i);
			
			mat.set(k, i, mat.get(k, j));
			mat.set(k, j, b);
		}
	}
	
	private ConvCode sortColumns(ConvCode code) {
		PolyMatrix gen = code.generator().clone();
		
		for (int i = 0;i < gen.getColumnCount() - 1; ++i) {
			for (int j = i + 1;j > 0; --j) {
				if (compareColumns(j, j - 1, code.getDelay(), gen) == -1) {
					swapColumns(j, j - 1, gen);
				}
			}
		}
		
		return new ConvCode(gen, true);
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
