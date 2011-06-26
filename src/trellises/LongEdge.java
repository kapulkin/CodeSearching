package trellises;

import math.BitArray;

public class LongEdge implements ITrellisEdge {
	private long src;
	private long dst;
	private BitArray bits;
	private double metrics[];
	
	/**
	 * Создает ребро, где в качестве метрики берется вес ребра.
	 * @param src индекс вершины, из которой исходит ребро
	 * @param dst индекс вершины, в которую ведет ребро
	 * @param bits кодовые символы на ребре
	 */
	public LongEdge(long src, long dst, BitArray bits) {
		this.src = src;
		this.dst = dst;
		this.bits = bits;
		metrics = new double[] { bits.cardinality() };
	}
	/**
	 * Создает ребро с заданными метриками
	 * @param src индекс вершины, из которой исходит ребро
	 * @param dst индекс вершины, в которую ведет ребро
	 * @param bits метка из битов на ребре
	 * @param metrics метрики ребра
	 */
	public LongEdge(long src, long dst, BitArray bits, double metrics[]) {
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
	public double[] metrics() {
		return metrics;
	}
	@Override
	public String toString() {
		return src + "‒" + bits + "→" + dst;
	}
}
