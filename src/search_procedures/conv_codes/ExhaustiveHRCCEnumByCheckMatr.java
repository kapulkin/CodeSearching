package search_procedures.conv_codes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import math.Matrix;
import math.Poly;
import math.PolyMatrix;
import codes.ConvCode;
import search_procedures.ICodeEnumerator;
import search_tools.HammingBallEnumerator;
import search_tools.MatrixEnumerator;

public class ExhaustiveHRCCEnumByCheckMatr implements ICodeEnumerator<ConvCode> {
	private int k;
	private int delay;
	private MatrixEnumerator parityCheckEnum;
	private HammingBallEnumerator topRowEnum;
	private PolyMatrix currentParityCheck;
	
	public ExhaustiveHRCCEnumByCheckMatr(int k, int delay) {
		this.k = k;
		this.delay = delay;
		
		reset();
	}
	
	public BigInteger count() {
		return (new HammingBallEnumerator(k + 1, 2)).count().multiply(parityCheckEnum.count());
	}
	
	@Override
	public void reset() {				
		parityCheckEnum = new MatrixEnumerator(delay - 1, k + 1);		
		currentParityCheck = null;
	}
	
	private void fillTopRow() {
		long[] highBitPositions = topRowEnum.next();
		
		for (int column = 0; column < k + 1; ++column) {
			if (Arrays.binarySearch(highBitPositions, column) >= 0) {
				currentParityCheck.get(0, column).setCoeff(delay, true);
			}else{
				currentParityCheck.get(0, column).setCoeff(delay, false);
			}
		}
	}

	@Override
	public ConvCode next() {
		if (topRowEnum != null && topRowEnum.hasNext()) {
			fillTopRow();
			
			PolyMatrix parityCheck = currentParityCheck.clone();
			
			parityCheck.sortColumns();
			return new ConvCode(parityCheck, false);
		}
		
		if (!parityCheckEnum.hasNext())
			return null;
		
		Matrix content = parityCheckEnum.getNext();
		
		currentParityCheck = new PolyMatrix(1, k + 1);
		
		for (int i = 0;i < k + 1; ++i) {
			Poly p = new Poly();
			
			p.setCoeff(0, true);
			for (int c = 1;c < delay; ++c) {
				p.setCoeff(c, content.get(c - 1, i));
			}
			
			currentParityCheck.set(0, i, p);
		}
		
		topRowEnum = new HammingBallEnumerator(k + 1, 2);
		fillTopRow();		
		
		PolyMatrix parityCheck = currentParityCheck.clone();
		
		parityCheck.sortColumns();
		return new ConvCode(parityCheck, false);
	}
	
	public ConvCode random() {
		MatrixEnumerator contentEnum = new MatrixEnumerator(delay - 1, k + 1);
		BigInteger maxIndex = contentEnum.count().add(BigInteger.valueOf(-1));		
		BigInteger index = BigInteger.ZERO;
		Random rnd = new Random();
		
		for (int bitIndex = 0; bitIndex < maxIndex.bitCount(); ++bitIndex) {
			if (rnd.nextBoolean()) {
				index.setBit(bitIndex);
			}
		}
		
		Matrix content = contentEnum.getByIndex(index);
		int bitPairIndex = rnd.nextInt((k + 2) * (k + 1) + 2);
		int firstBitIndex = bitPairIndex >= (k + 2) * (k + 1) ? k + 1 : bitPairIndex / (k + 1);
		int secondBitIndex = bitPairIndex >= (k + 2) * (k + 1) ? k + 1 : bitPairIndex % (k + 1);
		
		if (secondBitIndex >= firstBitIndex) ++secondBitIndex;
		
		PolyMatrix parityCheck = new PolyMatrix(1, k + 1);
		
		for (int i = 0;i < k + 1; ++i) {
			Poly poly = new Poly();
			
			poly.setCoeff(0, true);
			for (int j = 1;j < delay; ++j) {
				poly.setCoeff(j, content.get(j - 1, i));
			}
			
			if (i == firstBitIndex || i == secondBitIndex) {
				poly.setCoeff(delay, true);
			}
		}
		
		return new ConvCode(parityCheck, false);
	}

}
