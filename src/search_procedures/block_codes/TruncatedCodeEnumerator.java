package search_procedures.block_codes;

import java.math.BigInteger;

import math.ConvCodeAlgs;
import search_procedures.ICandidateEnumerator;
import search_procedures.ICodeEnumerator;
import codes.ConvCode;
import codes.TBCode;
import codes.TruncatedCode;
import codes.ZTCode;

public class TruncatedCodeEnumerator implements ICodeEnumerator<TruncatedCode> {
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
		
		return ConvCodeAlgs.truncate(k, n, convCode);
	}

	@Override
	public BigInteger count() {		
		return ccEnum.count();
	}

	@Override
	public void reset() {
		ccEnum.reset();		
	}
}
