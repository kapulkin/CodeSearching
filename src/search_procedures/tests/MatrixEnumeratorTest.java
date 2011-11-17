package search_procedures.tests;

import static org.junit.Assert.*;

import java.util.Random;

import math.Matrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.tests.ConvCodeTest;

import search_tools.MatrixEnumerator;

public class MatrixEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(MatrixEnumeratorTest.class);

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
	
	@Test
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
