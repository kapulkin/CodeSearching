package search_procedures.conv_codes;

import java.math.BigInteger;

import math.Matrix;
import math.Poly;
import math.PolyMatrix;
import codes.ConvCode;
import search_procedures.ICodeEnumerator;
import search_tools.MatrixEnumerator;

public class ExhaustiveCCEnumByGenMatr implements ICodeEnumerator<ConvCode> {
	private int k;
	private int n;
	private int delay;
	private MatrixEnumerator matEnum;
	
	public ExhaustiveCCEnumByGenMatr(int k, int n, int delay) {
		this.k = k;
		this.n = n;
		this.delay = delay;
		this.matEnum = new MatrixEnumerator(k * (delay + 1), n);
	}
	
	@Override
	public ConvCode next() {
		if(!matEnum.hasNext())
			return null;		
		
		Matrix content = matEnum.getNext();
		PolyMatrix polyGen = new PolyMatrix(k, n);
		
		for (int i = 0;i < k; ++i) {
			for (int j = 0;j < n; ++j) {
				Poly p = new Poly();
				
				for (int c = 0;c < delay + 1; ++c) {
					p.setCoeff(c, content.get(i * (delay + 1) + c, j));
				}
				
				polyGen.set(i, j, p);
			}
		}
		
		return new ConvCode(polyGen, true);
	}

	@Override
	public void reset() {
		matEnum = new MatrixEnumerator(k, n * (delay + 1));
	}

	@Override
	public BigInteger count() {		
		return matEnum.count();
	}

}
