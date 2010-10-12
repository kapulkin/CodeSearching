package math;

public class SpanForm {

	public Matrix Matr;
	public int[] SpanHeads;
	public int[] SpanTails;		
	public boolean IsTailbiting = false;
		
	public SpanForm(Matrix mat, int[] spanHeads, int[] spanTails)
	{				
		this.Matr = mat;
		this.SpanHeads = spanHeads;
		this.SpanTails = spanTails;		
	}
	
	public int getUncycledTail(int spanInd)
	{
		return SpanTails[spanInd] >= SpanHeads[spanInd] ? SpanTails[spanInd] : SpanTails[spanInd] + Matr.getColumnCount();  
	}
		
}
