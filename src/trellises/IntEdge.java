package trellises;

import math.BitArray;

public class IntEdge implements ITrellisEdge {
	/**
	 * Индекс начала ребра в слое 
	 */
	public int src;
	/**
	 * Индекс конца ребра в слое
	 */
	public int dst;
	/**
	 * Метрики ребра (кол-во единичек, вероятность и т.д.)
	 */
	public int[] metrics;
	/**
	 * Кодовые символы на ребре
	 */
	public BitArray bits;

	public IntEdge() {}
	
	public IntEdge(int src, int dst, BitArray bits) {
		this.src = src;
		this.dst = dst;
		this.bits = bits;
		this.metrics = new int[] {bits.cardinality()};
	}
	
	public IntEdge(int src, int dst, BitArray bits, int metrics[]) {
		this.src = src;
		this.dst = dst;
		this.bits = bits;
		this.metrics = metrics;
	}
	
	@Override
	public long src() {
		return src;
	}
	@Override
	public long dst() {
		return dst;
	}
	@Override
	public BitArray bits() {
		return bits;
	}
	@Override
	public int metric(int i) {
		return metrics[i];
	}
	public int[] metrics() {
		return metrics;
	}
	@Override
	public String toString() {
		return src + "‒" + bits + "→" + dst;
	}
}