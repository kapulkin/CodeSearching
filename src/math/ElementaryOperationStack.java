package math;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * Стек элементарных операций позволяет хранить последовательность операций, 
 * применяемых к матрице при декомпозиции Смита. В любой момент для 
 * последовательности операции, лежащих на стеке, можно получить матрицу,
 * являющуюся их композицией.
 * 
 *  Так как при декомпозиции Смита создаются две матрицы преобразований, то 
 *  предполагается, что будут использоваться два стека. 
 *  
 * @author stas
 *
 */
public class ElementaryOperationStack {
	int matrixSize;
	PolyMatrix matrix, inverseMatrix;
	Stack<ElementaryOperation> operations = new Stack<ElementaryOperation>();

	ElementaryOperationStack(int matrixSize) {
		this.matrixSize = matrixSize;
		matrix = PolyMatrix.getIdentity(matrixSize);
		inverseMatrix = PolyMatrix.getIdentity(matrixSize);
	}
	
	public void push(ElementaryOperation operation) {
		operations.push(operation);
		
		operation.performOperation(matrix);
		operation.performInverseOperation(inverseMatrix);
	}

	// а нужен ли этот метод?
	public ElementaryOperation  pop() {
		ElementaryOperation  operation = operations.pop();
		
		operation.performOperation(inverseMatrix);
		operation.performInverseOperation(matrix);
	
		return operation;
	}
	
	public int size() {
		return operations.size();
	}
	
	public boolean isEmpty() {
		return operations.isEmpty();
	}
	
	public ElementaryOperation peek() {
		if (isEmpty()) {
			throw new EmptyStackException();
		}
		return null;
	}
	
	public PolyMatrix getMatrix() {
		return matrix;
	}
	
	public PolyMatrix getInverseMatrix() {
		return inverseMatrix;
	}
}
