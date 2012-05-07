package search_procedures.conv_codes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import math.Matrix;
import math.Poly;
import math.PolyMatrix;
import codes.ConvCode;
import search_heuristics.IHeuristic;
import search_procedures.CodesCounter;
import search_procedures.ICodeEnumerator;
import search_tools.HammingBallEnumerator;
import search_tools.MatrixEnumerator;

public class ExhaustiveHRCCEnumByCheckMatr implements ICodeEnumerator<ConvCode> {
	private int k;
	private int delay;
	private MatrixEnumerator parityCheckEnum;
	private HammingBallEnumerator topRowEnum;
	private PolyMatrix currentParityCheck;
	private IHeuristic checker;
	
	public ExhaustiveHRCCEnumByCheckMatr(int k, int delay, IHeuristic checker) {
		this.k = k;
		this.delay = delay;
		this.checker = checker;
		
		reset();
	}
	
	public BigInteger count() {
		return (new HammingBallEnumerator(k + 1, 2)).count().multiply(parityCheckEnum.count());
	//	return CodesCounter.count(this);
	}
	
	@Override
	public void reset() {				
		parityCheckEnum = new MatrixEnumerator(delay - 1, k + 1);		
		currentParityCheck = null;
	}
	
	private int firstPolyInGroup(int group) {
		if (group == 0) {
			return 0;
		}
		
		for (int i = 1;i < k + 1; ++i) {
			if (!currentParityCheck.get(0, i).equals(currentParityCheck.get(0, i - 1))) {
				--group;
				if (group == 0) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private int numberOfPolyGroups() {
		int count = 1;
		
		for (int i = 1;i < k + 1; ++i) {
			if (!currentParityCheck.get(0, i).equals(currentParityCheck.get(0, i - 1))) {
				++count;
			}
		}
		
		return count;
	}
	
	private void eraseTopRow() {
		for (int i = 0;i < k + 1; ++i) {
			currentParityCheck.get(0, i).setCoeff(delay, false);
		}
	}
	
	private void fillTopRow() {
		long[] highBitPositions = topRowEnum.next();//skipEquivalentHighBitPositions();
				
		eraseTopRow();	
		
		if (highBitPositions.length == 2) {
			currentParityCheck.get(0, firstPolyInGroup((int)highBitPositions[1])).setCoeff(delay, true);
		}		
		
		currentParityCheck.get(0, firstPolyInGroup((int)highBitPositions[0])).setCoeff(delay, true);
	}
	
	private int checkSubmatrices() {
		for (int i = 1;i < k + 1; ++i) {
			PolyMatrix subMatrix = new PolyMatrix(1, i + 1);
			
			for (int j = 0;j <= i; ++j) {
				Poly p = new Poly();
				
				for (int c = 0;c < delay; ++c) {
					p.setCoeff(c, currentParityCheck.get(0, j).getCoeff(c));
				}
				
				subMatrix.set(0, j, p);
			}
			
			if (!checker.check(new ConvCode(subMatrix, false))) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public ConvCode next() {
		if (topRowEnum != null && topRowEnum.hasNext()) {
			fillTopRow();
			
			PolyMatrix parityCheck = currentParityCheck.clone();
			
			parityCheck.sortColumns();
			return new ConvCode(parityCheck, false);
		}
		
		int badColumn = -1;
		//while (true) {
			if (!parityCheckEnum.hasNext())
				return null;
			
			Matrix content = parityCheckEnum.getNext();//badColumn == -1 ? parityCheckEnum.getNext() : parityCheckEnum.getNext(badColumn);
			
			currentParityCheck = new PolyMatrix(1, k + 1);
			
			for (int i = 0;i < k + 1; ++i) {
				Poly p = new Poly();
				
				p.setCoeff(0, true);
				for (int c = 1;c < delay; ++c) {
					p.setCoeff(c, content.get(c - 1, i));
				}
				
				currentParityCheck.set(0, i, p);
			}
			
		//	badColumn = checkSubmatrices(); 
		//	if (badColumn == -1) {
		//		break;
		//	}
		//}
		
		int polyGroups = numberOfPolyGroups(); 
		
		topRowEnum = new HammingBallEnumerator(polyGroups, Math.min(2, polyGroups));
		fillTopRow();		
		
		PolyMatrix parityCheck = currentParityCheck.clone();
		
		parityCheck.sortColumns();
		return new ConvCode(parityCheck, false);
	}
	
	private static Random rnd = new Random();
	
	public ConvCode random() {
		MatrixEnumerator contentEnum = new MatrixEnumerator(delay - 1, k + 1);
		BigInteger maxIndex = contentEnum.count().add(BigInteger.valueOf(-1));		
		BigInteger index = BigInteger.ZERO;		
		
		boolean matchPrefix = true;
		for (int bitIndex = maxIndex.bitCount() - 1; bitIndex >= 0; --bitIndex) {
			if (matchPrefix && !maxIndex.testBit(bitIndex)) continue;
			
			if (rnd.nextBoolean()) {
				index = index.setBit(bitIndex);
			} else {
				matchPrefix = false;
			}
		}
		
		Matrix content = contentEnum.random();//contentEnum.getByIndex(index);
		int bitPairIndex = rnd.nextInt((k + 1) * k + 2);
		int firstBitIndex = bitPairIndex >= (k + 1) * k ? k + 1 : bitPairIndex / k;
		int secondBitIndex = bitPairIndex >= (k + 1) * k + 1 ? k + 1 : bitPairIndex % k;
		
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
			
			parityCheck.set(0, i, poly);
		}
		
		parityCheck.sortColumns();
		return new ConvCode(parityCheck, false);
	}

}
