package smith_decomposition;

import math.PolyMatrix;

/**
 * Элементарные операции при декомпозиции Смита, должны иметь единичный 
 * детерминант.
 * @author stas
 *
 */
public interface ElementaryOperation {
	PolyMatrix getMatrix();
	PolyMatrix getInverseMatrix();
	void performOperation(PolyMatrix matrix);
	void performInverseOperation(PolyMatrix matrix);
}
