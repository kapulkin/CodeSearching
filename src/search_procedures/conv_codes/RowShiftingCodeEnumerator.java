package search_procedures.conv_codes;

import math.PolyMatrix;
import search_procedures.ICodeEnumerator;
import search_tools.CEnumerator;
import codes.ConvCode;

public class RowShiftingCodeEnumerator implements ICodeEnumerator<ConvCode> {
	private int weight;
	private int length;
	private int shift;
	private int start;
	
	CEnumerator enumerator;
	
	public RowShiftingCodeEnumerator(int weight, int length, int shift, int start) {
		if (weight < 2 || length < 2 || shift < 2 || start < 0) {
			throw new IllegalArgumentException("Row with given parameters cannot form a convolutional code: weight = " + weight + ", length = " + length + ", shift = " + shift + ", start = " + start);
		}
		
		this.weight = weight;
		this.length = length;
		this.shift = shift;
		this.start = start;
		reset();
	}

	public RowShiftingCodeEnumerator(int weight, int length, int shift) {
		this(weight, length, shift, 0);
	}
	
	/**
	 * Gets input word length of enumerating codes.
	 * @return input word length of enumerating codes.
	 */
	public int getK() {
		return 1;
	}
	
	/**
	 * Gets code word length of enumerating codes.
	 * @return code word length of enumerating codes.
	 */
	public int getN() {
		return shift;
	}
	
	/**
	 * Gets degree of enumerating codes.
	 * @return degree of enumerating codes.
	 */
	public int getDegree() {
//		return (start + length + shift - 1) / shift - 1;
		return (start + length - 1) / shift;
	}
	
	@Override
	public void reset() {
		enumerator = new CEnumerator(length, weight);
	}

	@Override
	public ConvCode next() {
		if (!enumerator.hasNext()) {
			return null;
		}
		
		long coeffs[] = enumerator.next();
		PolyMatrix generator = new PolyMatrix(1, getDegree() + 1);
		for (int i = 0; i < coeffs.length; ++i) {
			int pos = start + (int)coeffs[i];
			generator.get(0, pos / shift).setCoeff(pos % shift, true);
		}
		
		ConvCode code = new ConvCode(generator, true);
		return code;
	}

}
