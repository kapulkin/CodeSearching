package codes;

import math.BitArray;


public interface Code {
	/**
	 * Here <em>k</em> is an input length.
	 * @return input length of the code
	 */
	public int getK();
	
	/**
	 * Here <em>n</em> is an output length.
	 * @return output length of the code
	 */
	public int getN();
	
	/**
	 * The rate of the code. It should be equal k/n.
	 * @return rate of the code
	 */
	public double getRate();
	
	/**
	 * The method encodes the given information sequence into the code sequence.
	 * The information sequence should contain a whole number of information words, 
	 * i.e. length of the sequence should be divisible by <em>k</em>.   
	 * @param infSeq information sequence
	 * @return code sequence
	 */
	public BitArray encodeSeq(BitArray infSeq);
}
