package math.tests;

import static org.junit.Assert.*;

import math.Poly;
import math.PolyMatrix;
import math.SmithDecomposition;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SmithDecompositionTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void validSmithDecompositionExample() {
		PolyMatrix G = new PolyMatrix(2, 3);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 1}));
		G.set(0, 1, new Poly(new int[] {1}));
		G.set(0, 2, Poly.getUnitPoly());
		// row 1
		G.set(1, 0, new Poly(new int[] {2}));
		G.set(1, 1, Poly.getUnitPoly());
		G.set(1, 2, new Poly(new int[] {0, 1, 2}));
		
		PolyMatrix A = new PolyMatrix(2, 2);
		// row 0
		A.set(0, 0, Poly.getUnitPoly());
		A.set(0, 1, new Poly());
		// row 1
		A.set(1, 0, new Poly(new int[] {0, 1, 2}));
		A.set(1, 1, Poly.getUnitPoly());
		
		PolyMatrix B = new PolyMatrix(3, 3);
		// row 0
		B.set(0, 0, new Poly(new int[] {0, 1}));
		B.set(0, 1, new Poly(new int[] {1}));
		B.set(0, 2, Poly.getUnitPoly());
		// row 1
		B.set(1, 0, new Poly(new int[] {0, 2, 3}));
		B.set(1, 1, new Poly(new int[] {0, 1, 2, 3}));
		B.set(1, 2, new Poly());
		// row 2
		B.set(2, 0, new Poly(new int[] {1, 2}));
		B.set(2, 1, new Poly(new int[] {0, 1, 2}));
		B.set(2, 2, new Poly());
		
		PolyMatrix D = new PolyMatrix(2, 3);
		// row 0
		D.set(0, 0, Poly.getUnitPoly());
		D.set(0, 1, new Poly());
		D.set(0, 2, new Poly());
		// row 1
		D.set(1, 0, new Poly());
		D.set(1, 1, Poly.getUnitPoly());
		D.set(1, 2, new Poly());

		testDecomposition(G, A, D, B);
	}
	
	@Test
	public void inverseMatrixDecompositionExample() {
		PolyMatrix G = new PolyMatrix(3, 2);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 2, 4}));
		G.set(0, 1, new Poly(new int[] {0, 1, 2}));
		// row 1
		G.set(1, 0, new Poly(new int[] {1, 4}));
		G.set(1, 1, new Poly(new int[] {1, 2}));
		// row 2
		G.set(2, 0, new Poly(new int[] {1, 3, 4}));
		G.set(2, 1, new Poly(new int[] {0, 2}));
		
		PolyMatrix A = new PolyMatrix(3, 3);
		// row 0
		A.set(0, 0, new Poly());
		A.set(0, 1, new Poly(new int[] {0, 1, 2}));
		A.set(0, 2, new Poly(new int[] {0, 1, 2, 3}));
		// row 1
		A.set(1, 0, new Poly());
		A.set(1, 1, new Poly(new int[] {1, 2}));
		A.set(1, 2, new Poly(new int[] {0, 2, 3}));
		// row 2
		A.set(2, 0, Poly.getUnitPoly());
		A.set(2, 1, new Poly(new int[] {0, 2}));
		A.set(2, 2, new Poly(new int[] {0, 1, 3}));
		
		PolyMatrix B = new PolyMatrix(2, 2);
		// row 0
		B.set(0, 0, Poly.getUnitPoly());
		B.set(0, 1, new Poly());
		// row 1
		B.set(1, 0, new Poly(new int[] {0, 1, 2}));
		B.set(1, 1, Poly.getUnitPoly());
		
		PolyMatrix D = new PolyMatrix(3, 2);
		// row 0
		D.set(0, 0, Poly.getUnitPoly());
		D.set(0, 1, new Poly());
		// row 1
		D.set(1, 0, new Poly());
		D.set(1, 1, Poly.getUnitPoly());
		// row 2
		D.set(2, 0, new Poly());
		D.set(2, 1, new Poly());

		testDecomposition(G, A, D, B);
	}
	
	@Test
	public void catasrophicEncodingMatrixDecompositionExample() {
		PolyMatrix G = new PolyMatrix(1, 2);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 3}));
		G.set(0, 1, new Poly(new int[] {0, 1, 2, 3}));
		
		PolyMatrix A = new PolyMatrix(1, 1);
		// row 0
		A.set(0, 0, Poly.getUnitPoly());
		
		PolyMatrix B = new PolyMatrix(2, 2);
		// row 0
		B.set(0, 0, new Poly(new int[] {0, 1, 2}));
		B.set(0, 1, new Poly(new int[] {0, 2}));
		// row 1
		B.set(1, 0, new Poly(new int[] {0, 1}));
		B.set(1, 1, new Poly(new int[] {1}));
		
		PolyMatrix D = new PolyMatrix(1, 2);
		// row 0
		D.set(0, 0, new Poly(new int[] {0, 1}));
		D.set(0, 1, new Poly());

		testDecomposition(G, A, D, B);
	}
	
	@Test
	public void gettingBasicGeneratorMatrixThroughDecomposition() {		
		PolyMatrix G = new PolyMatrix(1, 2);
		// row 0
		G.set(0, 0, new Poly(new int[] {0, 3}));
		G.set(0, 1, new Poly(new int[] {0, 1, 2, 3}));

		SmithDecomposition decomposition = new SmithDecomposition(G);

		PolyMatrix B = decomposition.getB();
		logger.debug("B:\n" + B);
		
		PolyMatrix G_ = new PolyMatrix(G.getRowCount(), G.getColumnCount());
		for (int i = 0; i < G_.getRowCount(); ++i) {
			for (int j = 0; j < G_.getColumnCount(); ++j) {
				G_.set(i, j, B.get(i, j));
			}
		}
		
		PolyMatrix D = new PolyMatrix(G.getRowCount(), G.getColumnCount());
		for (int i = 0; i < Math.min(D.getRowCount(), D.getColumnCount()); ++i) {
			D.set(i, i, Poly.getUnitPoly());
		}
		
		SmithDecomposition decomposition2 = new SmithDecomposition(G_);
		assertTrue(decomposition2.getD().equals(D));
	}
	
	/**
	 * Тестирует реализацию алгоритма декомпозиции Смита на заданой матрице G.
	 * @param G матрица для декомпозиции
	 * @param A ожидаемое значение матрицы A
	 * @param D ожидаемое значение матрицы D
	 * @param B ожидаемое значение матрицы B
	 */
	private void testDecomposition(PolyMatrix G, PolyMatrix A, PolyMatrix D, PolyMatrix B) {
		// проверка корректности входных параметров.
		assertTrue(G.equals(A.mul(D.mul(B))));

		SmithDecomposition decomposition = new SmithDecomposition(G);

		assertTrue(D.equals(decomposition.getD()));
		assertTrue(G.equals(decomposition.getA().mul(decomposition.getD().mul(decomposition.getB()))));
	}
}
