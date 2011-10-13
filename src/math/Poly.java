package math;

import java.util.StringTokenizer;

/**
 * 
 * Класс является реализацией многочлена с бинарными коэффициентами (из GF(2)).
 * Реализованы многие опрерации, такие как сложение, умножение, деление и т.п.
 * 
 * В данной реалицации коэффициенты хранятся в булевом векторе. Младшие 
 * коэффициенты хранятся в начале вектора, старшие в конце.
 * 
 * @author fedor
 *
 */

public class Poly implements Cloneable, Comparable<Poly> {
	private BitSet polyCoeffs = new BitSet(); 

	public Poly() {
	}
	
	public Poly(Boolean[] polyCoeffs)
	{
		if (polyCoeffs.length == 0) {
			throw new IllegalArgumentException("polyCoeffs length is zero.");
		}
		
		this.polyCoeffs = new BitSet(polyCoeffs.length);
		for(int i = 0;i < polyCoeffs.length;i ++)
		{
			this.polyCoeffs.set(i, polyCoeffs[i]);
		}
	}
	
	public Poly(boolean[] polyCoeffs)
	{
		if (polyCoeffs.length == 0) {
			throw new IllegalArgumentException("polyCoeffs length is zero.");
		}
		
		this.polyCoeffs = new BitSet(polyCoeffs.length);
		for(int i = 0;i < polyCoeffs.length;i ++)
		{
			this.polyCoeffs.set(i, polyCoeffs[i]);
		}
	}

	public Poly(int powers[]) {
		for (int power : powers) {
			polyCoeffs.set(power, true);
		}
	}
	
	public Poly(BitSet polyCoeffs) {
		this(polyCoeffs, true);
	}
	
	private Poly(BitSet polyCoeffs, boolean clone) {
		this.polyCoeffs = (clone ? (BitSet) polyCoeffs.clone() : polyCoeffs);
	}
	
	public Poly(Poly poly)
	{
		this(poly.polyCoeffs);
	}
	
	public int getDegree()
	{
		return Math.max(polyCoeffs.length() - 1, 0);
	}
	
	public Boolean getCoeff(int index)
	{
		return polyCoeffs.get(index);
	}
	
	public void setCoeff(int index, Boolean val)
	{
		polyCoeffs.set(index, val);
	}
	
	public void add(Poly poly) {
		polyCoeffs.xor(poly.polyCoeffs);
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
		if (isZero() || pow == 0) {
			return ;
		}
		
		BitSet newCoeffs = new BitSet();
		for (int bitIndex = polyCoeffs.nextSetBit(0); bitIndex >= 0; bitIndex = polyCoeffs.nextSetBit(bitIndex + 1)) {
			newCoeffs.set(bitIndex + pow);
		}
		
		polyCoeffs = newCoeffs;
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
		
		if (getDegree() < poly.getDegree())
		{
			return new Poly();
		}

		Poly tempPoly = new Poly(this);
		
		BitSet quotient = new BitSet(getDegree() - poly.getDegree() + 1);
		
		while (tempPoly.getDegree() >= poly.getDegree() && !tempPoly.isZero())
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
		
		return new Poly(quotient, false);
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
		return polyCoeffs.length() == 0;
	}
	
	public Poly clone()
	{
		return new Poly(this);
	}
	
	public static Poly getUnitPoly()
	{
		return new Poly(new boolean[]{ true });
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

	/**
	 * Compares two polynomials in a lexicographical manner, where the 
	 * polynomial is written as Dn+...+D2+D+1.
	 */
	public int compareTo(Poly poly) {
		int degreeComp = getDegree() - poly.getDegree();
		if (degreeComp != 0) {
			return degreeComp;
		}
		
		int degree = getDegree();
		
		for (int i = 0; i <= degree; ++i) {
			if (getCoeff(i) && !poly.getCoeff(i)) {
				return 1;
			}
			if (!getCoeff(i) && poly.getCoeff(i)) {
				return -1;
			}
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		
		String str = new String();

		int power = polyCoeffs.nextSetBit(0);
		switch (power) {
		case 0:
			str += "1";
			break;
		case 1:
			str += "D";
			break;
		default:
			str += "D" + power;
			break;
		}

		for (power = polyCoeffs.nextSetBit(power + 1); power >= 0; power = polyCoeffs.nextSetBit(power + 1)) {
			str += "+D" + ((power > 1) ? power : "");
		}
		
		return str;
	}

	public static Poly parsePoly(String str) {
		Poly poly = new Poly();
		StringTokenizer polyTokenizer = new StringTokenizer(str, "+");
		while (polyTokenizer.hasMoreTokens()) {
			String monomialString = polyTokenizer.nextToken();
			if (monomialString.equals("0")) {
				if (!poly.isZero()) {
					throw new NumberFormatException("Wrong format of string.");
				}
			} else if (monomialString.equals("1")) {
				if (poly.getCoeff(0)) {
					throw new NumberFormatException("Wrong format of string.");
				}
				poly.setCoeff(0, true);
			} else if (monomialString.equals("D")) {
				if (poly.getCoeff(1)) {
					throw new NumberFormatException("Wrong format of string.");
				}
				poly.setCoeff(1, true);
			} else {
				if (monomialString.charAt(0) != 'D') {
					throw new NumberFormatException("Wrong format of string.");
				}
				int power = Integer.parseInt(monomialString.substring(1));
				if (poly.getCoeff(power)) {
					throw new NumberFormatException("Wrong format of string.");
				}
				poly.setCoeff(power, true);
			}
		}
		return poly;
	}
}
