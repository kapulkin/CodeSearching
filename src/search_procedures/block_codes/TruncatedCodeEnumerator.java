package search_procedures.block_codes;

import search_procedures.conv_codes.ICodeEnumerator;
import codes.ConvCode;
import codes.TBCode;
import codes.TruncatedCode;
import codes.ZTCode;

public class TruncatedCodeEnumerator implements ICandidateEnumerator<TruncatedCode> {
	private ICodeEnumerator<ConvCode> ccEnum;	
//	private int tailTruncation = -1;
//	private int scaleDelta = -1;
	private int k = -1;
	private int n = -1;
	
	public TruncatedCodeEnumerator(ICodeEnumerator<ConvCode> ccEnum, int k, int n) {
		this.ccEnum = ccEnum;		
		this.k = k;
		this.n = n;
	}
	
	/*public TruncatedCodeEnumerator(IConvCodeEnumerator ccEnum, int tailTruncation, int scaleDelta) {
		this.ccEnum = ccEnum;
		this.tailTruncation = tailTruncation;
		this.scaleDelta = scaleDelta;		
	}/**/	
	
	@Override
	public TruncatedCode next() {
		ConvCode convCode = ccEnum.next();
		
		if (convCode == null)
			return null;
		
		int L, L0;
		
		if ((n % convCode.getN() != 0) ||  (k % convCode.getK() != 0)) {
			return null;
		}
		
		L = n / convCode.getN();
		L0 = L - k / convCode.getK();
		
		if (L0 < 0) {
			return null;
		}
		
		if (L0 == 0) {
			return new TBCode(convCode, L - (convCode.getDelay() + 1));
		}
		
		if (L0 == convCode.getDelay()) {
			return new ZTCode(convCode, L - (convCode.getDelay() + 1));
		}		
		
		return new TruncatedCode(convCode, L0, L - (convCode.getDelay() + 1));
	}
}
