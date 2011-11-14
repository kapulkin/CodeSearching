package search_procedures.conv_codes;

import math.PolyMatrix;
import search_tools.CEnumerator;
import codes.ConvCode;

public class RowShiftingCodeEnumerator implements IConvCodeEnumerator {
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
		PolyMatrix generator = new PolyMatrix(1, (start + length + shift - 1) / shift);
		for (int i = 0; i < coeffs.length; ++i) {
			int pos = start + (int)coeffs[i];
			generator.get(0, pos / shift).setCoeff(pos % shift, true);
		}
		
		ConvCode code = new ConvCode(generator, true);
		return code;
	}

}
