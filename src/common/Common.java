package common;

import math.BitSet;

public class Common {
	static public String bitsToString(BitSet bits, int size) {
		String bitString = new String();
		for (int i = 0; i < size; ++i) {
			bitString += bits.get(i) ? "1" : "0";
		}
		
		return bitString;
	}

	static public String bitsToString(BitSet bits) {
		return bitsToString(bits, bits.length());
	}
}
