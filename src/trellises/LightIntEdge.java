package trellises;

import math.BitArray;

public class LightIntEdge implements ITrellisEdge {
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
	public int metric;

	public LightIntEdge(ITrellisEdge edge) {
		this((int)edge.src(), (int)edge.dst(), edge.metric(0));
	}
	
	public LightIntEdge(int src, int dst, BitArray bits) {
		this.src = src;
		this.dst = dst;
		this.metric = bits.cardinality();
	}

	public LightIntEdge(int src, int dst, int metric) {
		this.src = src;
		this.dst = dst;
		this.metric = metric;
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
		return null;
	}
	@Override
	public int metric(int i) {
		return metric;
	}
	@Override
	public int[] metrics() {
		return new int[]{metric};
	}
	@Override
	public String toString() {
		return src + "?" + metric + ">" + dst;
	}
}