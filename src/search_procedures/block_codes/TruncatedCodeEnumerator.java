package search_procedures.block_codes;

import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

import math.BlockCodeAlgs;
import math.ConvCodeAlgs;
import math.PolyMatrix;
import math.SmithDecomposition;
import search_procedures.ICandidateEnumerator;
import search_procedures.ICodeEnumerator;
import trellises.TrellisUtils;
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
		
		TruncatedCode code = ConvCodeAlgs.truncate(k, n, convCode); 
				
		/*try {			
			PolyMatrix gen = code.getParentCode().generator();
			PolyMatrix pc = code.getParentCode().parityCheck();
		
			System.out.println("parent code generator:");
			IOPolyMatrix.writeMatrix(code.getParentCode().generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("parent code parity check:");
			IOPolyMatrix.writeMatrix(code.getParentCode().parityCheck(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("correctness test: production must be zero matrix:");
			IOPolyMatrix.writeMatrix(gen.mul(pc.transpose()), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println("truncated code generator:");
			IOMatrix.writeMatrix(code.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
			
			System.out.println(code.getParentCode().getFreeDist());	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(code.getMinDist());/**/	
		
		return code;
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
