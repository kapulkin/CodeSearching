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
		polyCoeffs = new ArrayList<Boolean>();
		
		for(int i = 0;i < poly.polyCoeffs.size();i ++)
		{
			polyCoeffs.add(poly.polyCoeffs.get(i));
		}
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
		Poly res = new Poly(this);
		
		if(getDegree() < poly.getDegree())
		{
			int delta = poly.getDegree() - getDegree();
			
			for(int i = 0;i < delta;i ++)
			{				
				res.polyCoeffs.add(false);
			}
		}
		
		for(int i = 0;i <= res.getDegree();i ++)
		{
			if(i <= poly.getDegree())
			{
				res.polyCoeffs.set(i, res.polyCoeffs.get(i)^poly.polyCoeffs.get(i));
			}else{
				break;
			}
		}
		
		res.trim();
		return res;
	}
	
	public Poly mul(Poly poly)
	{
		Poly res = new Poly();
		for(int i = 0;i <= poly.getDegree();i ++)
		{
			if(poly.getCoeff(i))
			{
				Poly tmp = new Poly(this);
				
				tmp = tmp.mulPow(i);
				res = res.sum(tmp);
			}
		}
		
		return res;
	}
	
	public Poly mulPow(int pow)
	{
		Poly res = new Poly(this);
		for(int i = 0;i < pow;i ++)
		{
			res.polyCoeffs.add(0, false);
		}
		return res;
	}
	
	public Poly getQuotient(Poly poly)
	{
		ArrayList<Boolean> quotient = new ArrayList<Boolean>();
		Poly tempPoly = new Poly(this);
		
		if(getDegree() < poly.getDegree())
		{
			quotient.add(false);
			return new Poly(quotient.toArray(new Boolean[quotient.size()]));
		}
		
		quotient.ensureCapacity(getDegree() - poly.getDegree() + 1);
		for(int i = 0;i < getDegree() - poly.getDegree() + 1; i++)
		{
			quotient.add(false);
		}
		
		while(tempPoly.getDegree() >= poly.getDegree() && !tempPoly.isZero())
		{
			/*if(tempPoly.getCoeff(tempPoly.getDegree()) == false)
			{
				tempCoeffs.remove(tempCoeffs.size()-1);
				continue;
			}/**/
			quotient.set(tempPoly.getDegree() - poly.getDegree(), true);
			
			Poly subPoly = new Poly(poly);
			
			subPoly = subPoly.mulPow(tempPoly.getDegree() - poly.getDegree());
			tempPoly = tempPoly.sum(subPoly);
		}
		
		return new Poly(quotient.toArray(new Boolean[quotient.size()]));
	}
	
	public Poly getRemainder(Poly poly)
	{
		Poly remainder = new Poly(this);
		
		if(getDegree() < poly.getDegree())
		{
			return remainder;
		}
		
		Poly quotient = getQuotient(poly);
				
		remainder = remainder.sum(quotient.mul(poly));
		
		return remainder;
	}
	
	public Poly[] divide(Poly poly)
	{
		Poly remainder = new Poly(this);
		
		if(getDegree() < poly.getDegree())
		{
			return new Poly[]{ new Poly(), remainder };
		}
		
		Poly quotient = getQuotient(poly);
				
		remainder = remainder.sum(quotient.mul(poly));
		
		return new Poly[]{quotient, remainder};
	}
	
	public boolean equals(Poly poly)
	{
		return polyCoeffs.equals(poly.polyCoeffs);
	}
	
	public boolean isZero()
	{
		return polyCoeffs.size() == 1 && polyCoeffs.get(0) == false;
	}
	
	public Poly clone()
	{
		return new Poly(this);
	}
	
	public static Poly unitPoly()
	{
		return new Poly(new Boolean[]{ true });
	}
	
	public static Poly[] extendedEuclid(Poly a, Poly b)
	{
		Poly x, y, d; 
		
		if(a.isZero())
		{
			x = new Poly();
			y = new Poly(new Boolean[]{ true });
			d = new Poly(b);
			
			return new Poly[]{x, y, d};
		}
		
		Poly[] divisionCoeffs = b.divide(a);
		Poly q = divisionCoeffs[0], r = divisionCoeffs[1];
		
		Poly[] recursiveOut = extendedEuclid(r, a);
		Poly _x = recursiveOut[0], _y = recursiveOut[1];
		
		d = recursiveOut[2];
		x = _y.sum(q.mul(_x));
		y = _x;
		
		return new Poly[] {x, y, d};
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
