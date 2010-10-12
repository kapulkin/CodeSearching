package math;

import java.util.ArrayList;

public class Poly {
	private ArrayList<Boolean> polyCoeffs = new ArrayList<Boolean>(); 

	public Poly()
	{
		polyCoeffs.add(false);
	}
	
	public Poly(Boolean[] polyCoeffs)
	{
		for(int i = 0;i < polyCoeffs.length;i ++)
		{
			this.polyCoeffs.add(polyCoeffs[i]);
		}
		trim();
	}
	
	public Poly(Poly poly)
	{
		polyCoeffs = new ArrayList<Boolean>(poly.polyCoeffs);		
	}
	
	public int getDegree()
	{
		return polyCoeffs.size() - 1;
	}
	
	public Boolean getCoeff(int index)
	{
		if(polyCoeffs.size() <= index)
			return false;
		return polyCoeffs.get(index);
	}
	
	public void setCoeff(int index, Boolean val)
	{
		if(polyCoeffs.size() <= index)
			return;
		polyCoeffs.set(index, val);
	}
	
	public Poly sum(Poly poly)
	{
		if(getDegree() < poly.getDegree())
		{
			int delta = poly.getDegree() - getDegree();
			for(int i = 0;i < delta;i ++)
			{
				polyCoeffs.add(false);
			}
		}
		for(int i = 0;i <= getDegree();i ++)
		{
			if(i <= poly.getDegree())
			{
				polyCoeffs.set(i, polyCoeffs.get(i)^poly.polyCoeffs.get(i));
			}else{
				break;
			}
		}
		trim();
		return this;
	}
	
	public Poly mul(Poly poly)
	{
		Poly res = new Poly();
		for(int i = 0;i <= poly.getDegree();i ++)
		{
			if(poly.getCoeff(i))
			{
				Poly tmp = new Poly(this);
				tmp.mulPow(i);
				res.sum(tmp);
			}
		}
		polyCoeffs = new ArrayList<Boolean>(res.polyCoeffs);
		return this;
	}
	
	public Poly mulPow(int pow)
	{
		for(int i = 0;i < pow;i ++)
		{
			polyCoeffs.add(0, false);
		}
		return this;
	}
	
	public Poly getQuotient(Poly poly)
	{
		ArrayList<Boolean> quotient = new ArrayList<Boolean>();
		Poly tempPoly = new Poly(this);
		
		if(getDegree() < poly.getDegree())
		{
			quotient.add(false);
			return new Poly((Boolean[])quotient.toArray());
		}
		
		quotient.ensureCapacity(getDegree() - poly.getDegree() + 1);
		for(int i = 0;i < getDegree() - poly.getDegree() + 1; i++)
		{
			quotient.add(false);
		}
		while(tempPoly.getDegree() >= poly.getDegree())
		{
			/*if(tempPoly.getCoeff(tempPoly.getDegree()) == false)
			{
				tempCoeffs.remove(tempCoeffs.size()-1);
				continue;
			}/**/
			quotient.set(tempPoly.getDegree() - poly.getDegree(), true);
			Poly subPoly = new Poly(poly);
			subPoly.mulPow(tempPoly.getDegree() - poly.getDegree());
			tempPoly.sum(subPoly);
		}
		return new Poly((Boolean[])quotient.toArray());
	}
	
	public Poly getRemainder(Poly poly)
	{
		Poly remainder = new Poly(this);
		if(getDegree() < poly.getDegree())
		{
			return new Poly((Boolean[])polyCoeffs.toArray());
		}
		Poly quotient = getQuotient(poly);
		quotient.mul(poly);
		remainder.sum(quotient);
		return remainder;
	}
	
	public Boolean equals(Poly poly)
	{
		return polyCoeffs.equals(poly.polyCoeffs);
	}
	
	private void trim()
	{
		for(int i = polyCoeffs.size() - 1;i > 0;i --)
		{
			if(polyCoeffs.get(i) == false)
			{
				polyCoeffs.remove(i);
			}else{
				break;
			}
		}
	}
}
