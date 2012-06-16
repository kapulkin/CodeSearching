package math.tests;

import in_out_interfaces.IOMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Test;

import math.BitArray;
import math.MathAlgs;
import math.Matrix;
import search_tools.MatrixEnumerator;

public class MathAlgsTest {	

	private BitArray cyclicShift(BitArray word, int shift) {
		BitArray shifted = new BitArray(word.getFixedSize());
		
		for (int i = 0;i < word.getFixedSize(); ++i) {
			shifted.set(i, word.get((i + word.getFixedSize() - shift) % (word.getFixedSize())));
		}
		
		return shifted;
	}
	
	private Matrix horizontalCyclicShift(Matrix mat, int shift) {
		Matrix shifted = new Matrix(mat.getRowCount(), mat.getColumnCount());
		
		for (int i = 0;i < mat.getRowCount(); ++i) {
			shifted.setRow(i, cyclicShift(mat.getRow(i), shift));
		}
		
		return shifted;
	}
	
	@Test
	public void orthogonalMatrixCalculation() {
		int n = 10;
		int k = 4;
		//MatrixEnumerator randomMatrix = new MatrixEnumerator(k, n - k);
		Matrix mat = new Matrix(k, n);
		//Matrix randomBlock = randomMatrix.random();
		BitArray word = new BitArray(n - k, new int[] { 0, 1 });
		int shift = 1;
		
		for (int row = 0; row < k; ++row) {
			for (int col = 0; col < n - k; ++col) {
				mat.set(row, col, word.get((col + n - k - shift * row) % (n - k)));
			}
			
			mat.set(row, n - k + row, true);
		}
		
		mat = horizontalCyclicShift(mat, 1);
		
		try {
			IOMatrix.writeMatrix(mat, new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
		} catch (IOException e) {				
			e.printStackTrace();
		}
		
		Matrix ort = MathAlgs.findOrthogonalMatrix(mat, true);
		Matrix shouldBeZero = mat.mul(ort.transpose());
		
		try {
			IOMatrix.writeMatrix(mat, new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
			IOMatrix.writeMatrix(ort, new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
			IOMatrix.writeMatrix(ort.transpose(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
			IOMatrix.writeMatrix(shouldBeZero, new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {				
			e.printStackTrace();
		}
	}
}
