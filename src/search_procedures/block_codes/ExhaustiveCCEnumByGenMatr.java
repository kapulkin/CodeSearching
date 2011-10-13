package search_procedures.block_codes;

import math.BlockMatrix;
import math.PolyMatrix;
import codes.ConvCode;
import search_procedures.conv_codes.IConvCodeEnumerator;
import search_tools.MatrixEnumerator;

public class ExhaustiveCCEnumByGenMatr implements IConvCodeEnumerator {
	private int k;
	private int n;
	private int delay;
	private MatrixEnumerator matEnum;
	
	public ExhaustiveCCEnumByGenMatr(int k, int n, int delay) {
		this.k = k;
		this.n = n;
		this.delay = delay;
		this.matEnum = new MatrixEnumerator(k, n * (delay + 1));
	}
	
	@Override
	public ConvCode next() {
		if(!matEnum.hasNext())
			return null;
		return new ConvCode(new PolyMatrix(new BlockMatrix(matEnum.getNext(), 1, delay + 1)), true);
	}

	@Override
	public void reset() {
		matEnum = new MatrixEnumerator(k, n * (delay + 1));
	}

}
