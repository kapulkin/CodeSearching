package math;

public interface ISpanForm {
	public int getRowCount();
	
	public int getHead(int row);
	public int getTail(int row);
	
	public void setHead(int row, int column);
	public void setTail(int row, int column);
}
