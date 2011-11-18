package search_procedures.block_codes;

import math.BlockMatrix;
import math.Matrix;
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
		this.matEnum = new MatrixEnumerator(k * (delay + 1), n);
	}
	
	@Override
	public ConvCode next() {
		if(!matEnum.hasNext())
			return null;		
		
		Matrix content = matEnum.getNext();
		BlockMatrix polyGen = new BlockMatrix(k, n, 1, delay + 1);
		
		for (int i = 0;i < k; ++i) {
			for (int j = 0;j < n; ++j) {
				Matrix cell = new Matrix(1, delay + 1);
				
				for (int c = 0;c < delay + 1; ++c) {
					cell.set(0, c, content.get(i * (delay + 1) + c, j));
				}
				
				polyGen.set(i, j, cell);
			}
		}
		
		return new ConvCode(new PolyMatrix(polyGen), true);
	}

	@Override
	public void reset() {
		matEnum = new MatrixEnumerator(k, n * (delay + 1));
	}

}
