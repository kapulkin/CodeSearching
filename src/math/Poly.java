package math;

import java.util.ArrayList;

/**
 * 
 * Класс является реализацией многочлена с бинарными коэффициентами (из GF(2)).
 * Реализованы многие опрерации, такие как сложение, умножение, деление и т.п.
 * 
 * В данной реалицации коэффициенты хранятся в векторе из переменных типа 
 * <code>Boolean</code>. Младшие коэффициенты хранятся в начале вектора, 
 * старшие в конце. Корректным состоянием класса является такое, при котором в 
 * векторе коэффициент при старшей степени многочлена степени хотя бы 1 
 * ненулевой.
 * 
 * @author fedor
 *
 */

public class Poly implements Cloneable {
	private ArrayList<Boolean> polyCoeffs = new ArrayList<Boolean>(); 

	public Poly()
	{
		polyCoeffs.add(false);
	}
	
	public Poly(Boolean[] polyCoeffs)
	{
		if (polyCoeffs.length == 0) {
			throw new IllegalArgumentException("polyCoeffs length is zero.");
		}
		
		this.polyCoeffs.ensureCapacity(polyCoeffs.length);
		for(int i = 0;i < polyCoeffs.length;i ++)
		{
			this.polyCoeffs.add(polyCoeffs[i]);
		}
		trim();
	}
	
	public Poly(int powers[]) {
		polyCoeffs.add(false);

		for (int power : powers) {
			if (polyCoeffs.size() <= power) {
				polyCoeffs.ensureCapacity(power + 1);
				for (int i = polyCoeffs.size(); i < power; ++i) {
					polyCoeffs.add(false);
				}
				polyCoeffs.add(true);
			} else {
				polyCoeffs.set(power, true);
			}
		}
	}
	
	private Poly(ArrayList<Boolean> polyCoeffs) {
		if (polyCoeffs.size() == 0) {
			throw new IllegalArgumentException("polyCoeffs size is zero.");
		}

		this.polyCoeffs = polyCoeffs;

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
			throw new IndexOutOfBoundsException();
		polyCoeffs.set(index, val);

		trim();
	}
	
	public void add(Poly poly) {
		if (getDegree() < poly.getDegree()) {
			int delta = poly.getDegree() - getDegree();
			
			for(int i = 0; i < delta; ++i)  {				
				polyCoeffs.add(false);
			}
		}
		
		for (int i = 0; i <= poly.getDegree(); ++i) {
			polyCoeffs.set(i, polyCoeffs.get(i) ^ poly.polyCoeffs.get(i));
		}
		
		trim();
	}
	
	public Poly sum(Poly poly)
	{
		Poly res = new Poly(this);
		
		res.add(poly);
		
		return res;
	}
	
	public Poly mul(Poly poly)
	{
		Poly res = new Poly();

		Poly tmp = new Poly(this);
		int lastMulPow = 0;
		for(int i = 0;i <= poly.getDegree();i ++)
		{
			if(poly.getCoeff(i))
			{			
				tmp.increaseDegree(i - lastMulPow);
				lastMulPow = i;
				res.add(tmp);
			}
		}
		
		return res;
	}
	
	public void increaseDegree(int pow) {
		if (getDegree() == 0 && getCoeff(0) == false) {
			return ;
		}
		
		for(int i = 0; i < pow; ++i) {
			polyCoeffs.add(i, false);
		}
	}
	
	public Poly mulPow(int pow)
	{
		Poly res = new Poly(this);
		
		res.increaseDegree(pow);

		return res;
	}
	
	public Poly getQuotient(Poly poly)
	{
		if (poly.isZero()) {
			throw new IllegalArgumentException("Division by zero!");
		}
		
		if(getDegree() < poly.getDegree())
		{
			return new Poly();
		}

		ArrayList<Boolean> quotient = new ArrayList<Boolean>();
		Poly tempPoly = new Poly(this);
		
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
			
			Poly subPoly = poly.mulPow(tempPoly.getDegree() - poly.getDegree());
			
			tempPoly = tempPoly.sum(subPoly);
		}
		
		return new Poly(quotient);
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
	
	public static Poly getUnitPoly()
	{
		return new Poly(new Boolean[]{ true });
	}
	
	/**
	 * Return such <em>x</em>, <em>y</em>, <em>d</em>, that <em>d = a*x + b*y</em> and <em>d</em> has a minimum degree.
	 * @param a polynomial
	 * @param b polynomial
	 * @return array of three polynomials <em>x</em>, <em>y</em>, <em>d</em>.
	 */
	public static Poly[] extendedEuclid(Poly a, Poly b)
	{
		Poly x, y, d; 
		
		if(a.isZero())
		{
			x = new Poly();
			y = getUnitPoly();
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
		for(int i = polyCoeffs.size() - 1; i > 0; --i)
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
