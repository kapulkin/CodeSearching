package math;

import java.util.SortedSet;
import java.util.TreeSet;

public class SpanForm implements ISpanForm {

	public Matrix Matr;
	public boolean IsTailbiting = false;
		
	private int[] spanHeads;
	private int[] spanTails;		

	public SpanForm(Matrix mat, int[] spanHeads, int[] spanTails)
	{				
		this.Matr = mat;
		this.spanHeads = spanHeads;
		this.spanTails = spanTails;		
	}
	
	@Override
	public int getRowCount() {
		return Matr.getRowCount();
	}

	@Override
	public int getHead(int row) {
		return spanHeads[row];
	}

	@Override
	public int getTail(int row) {
		return spanTails[row];
	}

	@Override
	public void setHead(int row, int column) {
		spanHeads[row] = column;
	}

	@Override
	public void setTail(int row, int column) {
		spanTails[row] = column;
	}

	public int getUncycledTail(int spanInd)
	{
		return spanTails[spanInd] >= spanHeads[spanInd] ? spanTails[spanInd] : spanTails[spanInd] + Matr.getColumnCount();  
	}
	
	/**
	 * Проверяет, является ли ряд <code>row</code> активным для яруса, 
	 * расположенного <b>перед</b> столбцом <code>column</code>.
	 * 
	 * @param column столбец, активность ряда перед которым проверяется
	 * @param row проверяемый ряд
	 * @return true, если ряд активен.
	 */
	public boolean isRowActiveBefore(int column, int row) {
		return spanHeads[row] < column && column <= spanTails[row] || 
			(spanTails[row] < spanHeads[row] && !(spanTails[row] < column && column <= spanHeads[row]));
	}

	/**
	 * Проверяет, является ли ряд <code>row</code> активным для яруса, 
	 * расположенного <b>после</b> столбца <code>column</code>.
	 * 
	 * @param column столбец, активность ряда после которого проверяется
	 * @param row проверяемый ряд
	 * @return true, если ряд активен.
	 */
	public boolean isRowActiveAfter(int column, int row) {
		return spanHeads[row] <= column && column < spanTails[row] || 
			(spanTails[row] < spanHeads[row] && !(spanTails[row] <= column && column < spanHeads[row]));
	}

	public SortedSet<Integer> getActiveRowsBefore(int column) {
		SortedSet<Integer> activeRows = new TreeSet<Integer>();
		
		for (int row = 0; row < Matr.getRowCount(); ++row) {
			if (isRowActiveBefore(column, row)) {
				activeRows.add(row);
			}
		}
		
		return activeRows;
	}

	public SortedSet<Integer> getActiveRowsAfter(int column) {
		SortedSet<Integer> activeRows = new TreeSet<Integer>();
		
		for (int row = 0; row < Matr.getRowCount(); ++row) {
			if (isRowActiveAfter(column, row)) {
				activeRows.add(row);
			}
		}
		
		return activeRows;
	}
}
