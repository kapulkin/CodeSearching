package smith_decomposition;

import math.Poly;
import math.PolyMatrix;

public class ColumnInterchangeOperation implements PostElementaryOperation {
	PolyMatrix mat;
	int i;
	int j;
	
	public ColumnInterchangeOperation(int size, int i, int j) {
		mat = PolyMatrix.getIdentity(size);

		mat.set(i, i, new Poly());
		mat.set(i, j, Poly.getUnitPoly());
		
		mat.set(j, j, new Poly());
		mat.set(j, i, Poly.getUnitPoly());
	}
	
	public int size() {
		return mat.getColumnCount();
	}

	@Override
	public PolyMatrix getMatrix() {
		return mat;
	}

	@Override
	public PolyMatrix getInverseMatrix() {
		return mat;
	}

	@Override
	public void performOperation(PolyMatrix matrix) {
		if (matrix.getRowCount() != size() || matrix.getRowCount() != size()) {
			throw new IllegalArgumentException();
		}

		for (int k = 0; k < matrix.getRowCount(); ++k) {
			Poly tmp = matrix.get(k, i);
			
			matrix.set(k, i, matrix.get(k, j));
			matrix.set(k, j, tmp);
		}
	}

	@Override
	public void performInverseOperation(PolyMatrix matrix) {
		if (matrix.getRowCount() != size() || matrix.getRowCount() != size()) {
			throw new IllegalArgumentException();
		}
		
		for (int k = 0; k < matrix.getColumnCount(); ++k) {
			Poly tmp = matrix.get(i, k);
			
			matrix.set(i, k, matrix.get(j, k));
			matrix.set(j, k, tmp);
		}
	}
}
