package math;

import math.BitSet;

/**
 * Массив из фиксированного колличества битов. 
 * @author stas
 *
 */
public class BitArray implements Cloneable{
	private BitSet bitSet;

	protected BitArray() {
	}
	
    /**
     * Creates a bit array with size <code>fixedSize</code>. All bits are 
     * initially <code>false</code>.
     *
     * @param     fixedSize   the initial size of the bit array.
     * @exception NegativeArraySizeException if the specified initial size
     *               is negative.
     */
	public BitArray(int fixedSize) {
		this.bitSet = new BitSet(fixedSize);
	}
	
	public BitArray(BitSet bitSet) {
		this.bitSet = bitSet;
	}
	
	public int getFixedSize() {
		return bitSet.getFixedSize();
	}
	
	public BitSet getBitSet() {
		return bitSet;
	}
	
	public void and(BitArray array) { 
		if (getFixedSize() != array.getFixedSize()) {
			throw new IllegalArgumentException("Fixed sizes are different: " + getFixedSize() + ", " + array.getFixedSize());
		}
		bitSet.and(array.bitSet);
	}

	public void andNot(BitArray array) {
		if (getFixedSize() != array.getFixedSize()) {
			throw new IllegalArgumentException("Fixed sizes are different: " + getFixedSize() + ", " + array.getFixedSize());
		}

		bitSet.and(array.bitSet);
	}

	public int	cardinality() {
		return bitSet.cardinality();
	}

	public void clear() {
		bitSet.clear();
	}

	public void clear(int bitIndex) {
		if (bitIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException("" + bitIndex);
		}
		
		bitSet.clear(bitIndex);
	}

	public void clear(int fromIndex, int toIndex) {
		if (toIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
		}
		
		bitSet.clear(fromIndex, toIndex);
	}

	public Object clone() {
		BitArray array = new BitArray();
		array.bitSet = (BitSet) bitSet.clone();
		
		return array;
	}

	public boolean	equals(Object obj) {
		BitArray array = (BitArray) obj;
		if (array == null) {
			return false;
		}
		return array.getFixedSize() == getFixedSize() && array.bitSet.equals(bitSet); 
	}

	public void flip(int bitIndex) {
		if (bitIndex >= getFixedSize()) {
			throw new IndexOutOfBoundsException("" + bitIndex);
		}
		
		bitSet.flip(bitIndex);
	}

	public void flip(int fromIndex, int toIndex) {
		if (toIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
		}
		
		bitSet.flip(fromIndex, toIndex);
	}

	public boolean get(int bitIndex) {
		if (bitIndex >= getFixedSize()) {
			throw new IndexOutOfBoundsException("" + bitIndex);
		}
		
		return bitSet.get(bitIndex);
	}

	/**
	 * Возвращает 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
    /**
     * Returns a new <tt>BitSet</tt> composed of bits from this <tt>BitSet</tt>
     * from <tt>fromIndex</tt> (inclusive) to <tt>toIndex</tt> (exclusive)
     * <strong>cyclically</strong>.
     *
     * @param     fromIndex   index of the first bit to include.
     * @param     toIndex     index after the last bit to include.
     * @return    a new <tt>BitSet</tt> from a cyclic range of this <tt>BitSet</tt>.
     * @exception IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *            or <tt>toIndex</tt> is negative, or <tt>toIndex</tt> is
     *            larger than <tt>fixedSize()</tt>.
     */
	public BitArray get(int fromIndex, int toIndex) {
		if (toIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
		}
		
		if (toIndex < fromIndex) {
			BitArray array = new BitArray(getFixedSize() - fromIndex + toIndex);
			array.bitSet.or(bitSet.get(fromIndex, getFixedSize()));
			for (int i = 0; i < toIndex; ++i) {
				array.set(getFixedSize() - fromIndex + i, bitSet.get(i));
			}
			return array;
		}
		
		BitArray array = new BitArray(toIndex - fromIndex);
		array.bitSet.or(bitSet.get(fromIndex, toIndex));
		
		return array;
	}

	public int hashCode() {
		return bitSet.hashCode();
	}
	
	public boolean intersects(BitSet set) {
		return bitSet.intersects(set);
	}

	public boolean isEmpty() {
		return bitSet.isEmpty();
	}

	public int length() {
		return bitSet.length();
	}

	public int nextClearBit(int fromIndex) {
		return bitSet.nextClearBit(fromIndex);
	}

	public int nextSetBit(int fromIndex) {
		return bitSet.nextSetBit(fromIndex);
	}

	public void or(BitArray array) {
		if (getFixedSize() != array.getFixedSize()) {
			throw new IllegalArgumentException("Fixed sizes are different: " + getFixedSize() + ", " + array.getFixedSize());
		}

		bitSet.or(array.bitSet);
	}

	public int previousClearBit(int fromIndex) {
		return bitSet.previousClearBit(fromIndex);
	}

	public int previousSetBit(int fromIndex) {
    	return bitSet.previousSetBit(fromIndex);
    }

	public void set(int bitIndex) {
		if (bitIndex >= getFixedSize()) {
			throw new IndexOutOfBoundsException("" + bitIndex);
		}

		bitSet.set(bitIndex);
	}

	public void set(int bitIndex, boolean value) {
		if (bitIndex >= getFixedSize()) {
			throw new IndexOutOfBoundsException("" + bitIndex);
		}

		bitSet.set(bitIndex, value);
	}

	public void set(int fromIndex, int toIndex) {
		if (toIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
		}

		bitSet.set(fromIndex, toIndex);
	}

	public void set(int fromIndex, int toIndex, boolean value) {
		if (toIndex > getFixedSize()) {
			throw new IndexOutOfBoundsException(fromIndex + ", " + toIndex);
		}

		bitSet.set(fromIndex, toIndex, value);
	}

	public int size() {
		return bitSet.size();
	}

	public String toString() {
		String bitString = new String();
		for (int i = 0; i < getFixedSize(); ++i) {
			bitString += bitSet.get(i) ? "1" : "0";
		}
		
		return bitString;
	}

	public void xor(BitArray array) {
		if (getFixedSize() != array.getFixedSize()) {
			throw new IllegalArgumentException("Fixed sizes are different: " + getFixedSize() + ", " + array.getFixedSize());
		}
		
		bitSet.xor(array.bitSet);
	}
	
	public Boolean[] toArray() {
		return bitSet.toArray();
	}
}
