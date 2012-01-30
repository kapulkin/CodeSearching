package search_procedures.block_codes;

import codes.ConvCode;
import codes.TruncatedCode;
import search_procedures.conv_codes.RowShiftingCodeEnumerator;

public class RowShiftingZTCodeSearcher extends BasicBlockCodesSearcher<TruncatedCode> {
	RowShiftingCodeEnumerator rowCcEnum;

	public RowShiftingZTCodeSearcher(int requiredMinDistance,
			int requiredStateComplexity, RowShiftingCodeEnumerator rowCcEnum, int scale) {
		//super(requiredMinDistance, requiredStateComplexity);
		this.rowCcEnum = rowCcEnum;
		this.candidateEnum = new TruncatedCodeEnumerator(rowCcEnum, scale, rowCcEnum.getN() * (rowCcEnum.getDegree() + scale));
	}
	
	public RowShiftingZTCodeSearcher(int requiredMinDistance,
			int requiredStateComplexity, ConvCode ccCode, int k, int n) {
		//super(requiredMinDistance, requiredStateComplexity);

		int scale1 = DZTCodeSearcher.getScale1(ccCode, k, n);

		int start = DZTCodeSearcher.getStart(ccCode);		
		int scale2 = DZTCodeSearcher.getScale2(ccCode, scale1, k, n);
		
		this.rowCcEnum = new RowShiftingCodeEnumerator(requiredMinDistance, n - ccCode.getN() * (scale2 - 1), ccCode.getN(), start);

		this.candidateEnum = new TruncatedCodeEnumerator(rowCcEnum, scale2, rowCcEnum.getN() * (rowCcEnum.getDegree() + scale2));
	}

	public RowShiftingCodeEnumerator getRowShiftingCodeEnumerator() {
		return rowCcEnum;
	}
}
