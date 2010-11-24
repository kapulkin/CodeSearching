package math;

import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SmithDecomposition 
{	
	private int b;
	private int c;
	
	private PolyMatrix A;
	private PolyMatrix InvA;
	private PolyMatrix D;
	private PolyMatrix B;
	private PolyMatrix InvB;
	
	public SmithDecomposition(PolyMatrix G)
	{
		b = G.getRowCount();
		c = G.getColumnCount();
		
		A = PolyMatrix.getIdentity(b);
		InvA = PolyMatrix.getIdentity(b);
		B = PolyMatrix.getIdentity(c);
		InvB = PolyMatrix.getIdentity(c);
		D = G.clone();
		
		decomposeSubmatrix(0);
	}
	
	public PolyMatrix getA()
	{
		return A;
	}
	
	public PolyMatrix getInvA()
	{
		return InvA;
	}
	
	public PolyMatrix getB()
	{
		return B;
	}
	
	public PolyMatrix getInvB()
	{
		return InvB;
	}
	
	public PolyMatrix getD()
	{
		return D;		
	}	
	
	private void decomposeSubmatrix(int diagInd)
	{
		cleanRow(diagInd);		
		
		/*try {
			IOPolyMatrix.writeMatrix(D, new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}/**/
		
		cleanColumn(diagInd);		
		
		while(true)
		{	
			Poly corner = D.get(diagInd, diagInd);
			int badRow = -1;
			
			if(corner.isZero())
			{
				break;
			}
			
			for(int i = diagInd + 1;i < b;i ++)
			{
				for(int j = diagInd + 1;j < c;j ++)
				{
					if(!D.get(i, j).getRemainder(corner).isZero())
					{
						badRow = i;
						break;
					}
				}
				
				if(badRow != -1)
				{
					break;					
				}
			}
			
			if(badRow == -1)
			{
				break;
			}
			
			addRows(diagInd, badRow, Poly.unitPoly());
			cleanRow(diagInd);			
		}
		
		if(diagInd < b - 1)
		{
			decomposeSubmatrix(diagInd + 1);
		}
	}
	
	private void cleanRow(int diagInd)
	{
		Poly corner = D.get(diagInd, diagInd);
		
		if(corner.isZero())
		{
			for(int i = diagInd + 1;i < c;i ++)
			{
				if(!D.get(diagInd, i).isZero())
				{
					addColumns(diagInd, i, Poly.unitPoly());
					corner = D.get(diagInd, diagInd);
					break;
				}
			}
			
			if(corner.isZero())
			{
				return;
			}
		}
		
		for(int i = diagInd + 1;i < c;i ++)
		{
			Poly rowItem = D.get(diagInd, i);
			
			if(rowItem.isZero())
			{
				continue;
			}
			
			Poly[] gcdCoeffs = Poly.extendedEuclid(corner, rowItem);
			Poly x = gcdCoeffs[0], y = gcdCoeffs[1], gcd = gcdCoeffs[2];
			Poly qy = rowItem.getQuotient(gcd);
			Poly qx = corner.getQuotient(gcd);			
						
			specialColumnCombination(diagInd, i, x, y, qx, qy);			
			
			corner = gcd;
		}
	}
	
	private void cleanColumn(int diagInd)
	{
		Poly corner = D.get(diagInd, diagInd);
		
		if(corner.isZero())
		{
			for(int i = diagInd + 1;i < b;i ++)
			{
				if(!D.get(i, diagInd).isZero())
				{
					addRows(diagInd, i, Poly.unitPoly());
					corner = D.get(diagInd, diagInd);
					break;
				}
			}
			
			if(corner.isZero())
			{
				return;
			}
		}
		
		for(int i = diagInd + 1;i < b;i ++)
		{
			Poly columnItem = D.get(i, diagInd);
			
			if(columnItem.isZero())
			{
				continue;
			}
			
			Poly[] gcdCoeffs = Poly.extendedEuclid(corner, columnItem);
			Poly x = gcdCoeffs[0], y = gcdCoeffs[1], gcd = gcdCoeffs[2];
			Poly qy = columnItem.getQuotient(gcd);
			Poly qx = corner.getQuotient(gcd);
			
			specialRowCombination(diagInd, i, x, y, qx, qy);	
			
			corner = gcd;
		}
	}
	
	private void swapRows(int i, int j)
	{
		for(int k = 0;k < b;k ++)
		{
			Poly tmp = A.get(k, i);
			
			A.set(k, i, A.get(k, j));
			A.set(k, j, tmp);
		}
		
		for(int k = 0;k < c;k ++)
		{
			Poly tmp = D.get(i, k);
			
			D.set(i, k, D.get(j, k));
			D.set(j, k, tmp);
		}
	}
	
	private void addRows(int i, int j, Poly factor)
	{
		for(int k = 0;k < c;k ++)
		{			
			D.set(i, k, D.get(i, k).sum(D.get(j, k).mul(factor)));			
		}
		
		for(int k = 0;k < b;k ++)
		{
			A.set(k, j, A.get(k, j).sum(A.get(k, i).mul(factor)));
			InvA.set(i, k, InvA.get(i, k).sum(InvA.get(j, k).mul(factor)));
		}
	}	
	
	private void swapColumns(int i, int j)
	{
		for(int k = 0;k < c;k ++)
		{
			Poly tmp = B.get(i, k);
			
			B.set(i, k, B.get(j, k));
			B.set(j, k, tmp);			
		}
		
		for(int k = 0;k < b;k ++)
		{
			Poly tmp = D.get(k, i);
			
			D.set(k, i, D.get(k, j));
			D.set(k, j, tmp);
		}
	}
	
	private void specialRowCombination(int i, int j, Poly x, Poly y, Poly qx, Poly qy)
	{				
		for(int k = 0;k < c;k ++)
		{
			Poly pi = D.get(i, k).clone();
			Poly pj = D.get(j, k).clone();
			
			D.set(i, k, pi.mul(x).sum(pj.mul(y)));
			D.set(j, k, pi.mul(qy).sum(pj.mul(qx)));
		}
		
		for(int k = 0;k < b;k ++)
		{
			Poly pi = A.get(k, i).clone();
			Poly pj = A.get(k, j).clone();
			
			A.set(k, i, pi.mul(qx).sum(pj.mul(qy)));
			A.set(k, j, pi.mul(y).sum(pj.mul(x)));	
			
			pi = InvA.get(i, k).clone();
			pj = InvA.get(j, k).clone();
			
			InvA.set(i, k, pi.mul(x).sum(pj.mul(y)));
			InvA.set(j, k, pi.mul(qy).sum(pj.mul(qx)));
		}
	}
	
	private void specialColumnCombination(int i, int j, Poly x, Poly y, Poly qx, Poly qy)
	{				
		for(int k = 0;k < b;k ++)
		{
			Poly pi = D.get(k, i).clone();
			Poly pj = D.get(k, j).clone();
			
			D.set(k, i, pi.mul(x).sum(pj.mul(y)));
			D.set(k, j, pi.mul(qy).sum(pj.mul(qx)));
		}
		
		for(int k = 0;k < c;k ++)
		{
			Poly pi = B.get(i, k).clone();
			Poly pj = B.get(j, k).clone();
			
			B.set(i, k, pi.mul(qx).sum(pj.mul(qy)));
			B.set(j, k, pi.mul(y).sum(pj.mul(x)));	
			
			pi = InvB.get(k, i).clone();
			pj = InvB.get(k, j).clone();
			
			InvB.set(k, i, pi.mul(x).sum(pj.mul(y)));
			InvB.set(k, j, pi.mul(qy).sum(pj.mul(qx)));
		}
	}
	
	private void addColumns(int i, int j, Poly factor)
	{
		for(int k = 0;k < b;k ++)
		{			
			D.set(k, i, D.get(k, i).sum(D.get(k, j).mul(factor)));			
		}
		
		for(int k = 0;k < c;k ++)
		{
			B.set(j, k, B.get(j, k).sum(B.get(i, k).mul(factor)));
			InvB.set(k, i, InvB.get(k, i).sum(InvB.get(k, j).mul(factor)));
		}
	}	
	
}
