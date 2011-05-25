package trellises;

import java.util.ArrayList;

/**
 * Ярусы решетки кода расположены между сегментами порождающей 
 * матрицы. Сегмент включает в себя один или несколько последовательно 
 * расположенных столбцов. Объекты данного класса представляют собой структуру с 
 * описанием изменений, которые происходят в сегменте при движении по решетке
 * слева направо.
 * @author stas
 *
 */
public class TrellisSection {
	public static class Boundary {
		public int row;
		public int column;
		
		public Boundary(int row, int column) {
			this.row = row;
			this.column = column;
		}
	}

	public ArrayList<Boundary> spanHeads = new ArrayList<Boundary>();
	public ArrayList<Boundary> spanTails = new ArrayList<Boundary>();
	
	public int beginColumn() {
		int minColumn = Integer.MAX_VALUE;
		
		for (Boundary head : spanHeads) {
			if (minColumn > head.column) {
				minColumn = head.column;
			}
		}
		
		for (Boundary tail : spanTails) {
			if (minColumn > tail.column) {
				minColumn = tail.column;
			}
		}

		return minColumn;
	}
	
	@Override
	public String toString() {
		String str = "[";
		
		if (!spanHeads.isEmpty()) {
			str += "h:";
			for (Boundary head : spanHeads) {
				str += " " + head.column;
			}
			
			if (!spanTails.isEmpty()) {
				str += ", ";
			}
		}
		
		if (!spanTails.isEmpty()) {
			str += "t:";
			for (Boundary head : spanTails) {
				str += " " + head.column;
			}
		}
		str += "]";
		
		return str;
	}
}
