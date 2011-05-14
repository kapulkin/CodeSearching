package math;

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
	
	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		
		String str = new String();

		boolean firstCoeffPrinted = false;
		if (getCoeff(0)) {
			str += "1";
			firstCoeffPrinted = true;
		}
		
		if (getCoeff(1)) {
			str += (firstCoeffPrinted ? "+D" : "D");
			firstCoeffPrinted = true;
		}

		int power = 2;
		if (!firstCoeffPrinted) {
			// print first non-zero coeff
			for (; power <= getDegree(); ++power) {
				if (getCoeff(power)) {
					str += "D" + power;
					firstCoeffPrinted = true;
					break;
				}
			}
		}
		for (; power <= getDegree(); ++power) {
			if (getCoeff(power)) {
				str += "+D" + power;
			}
		}
		
		return str;
	}
}
