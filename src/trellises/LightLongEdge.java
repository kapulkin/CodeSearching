package trellises;

import math.BitArray;

public class LightLongEdge implements ITrellisEdge {
	private long src;
	private long dst;
	private int metric;
	
	/**
	 * Создает ребро, где в качестве метрики берется вес ребра.
	 * @param src индекс вершины, из которой исходит ребро
	 * @param dst индекс вершины, в которую ведет ребро
	 * @param bits кодовые символы на ребре
	 */
	public LightLongEdge(long src, long dst, BitArray bits) {
		this.src = src;
		this.dst = dst;
		metric = bits.cardinality();
	}
	/**
	 * Создает ребро с заданными метриками
	 * @param src индекс вершины, из которой исходит ребро
	 * @param dst индекс вершины, в которую ведет ребро
	 * @param bits метка из битов на ребре
	 * @param metrics метрики ребра
	 */
	public LightLongEdge(long src, long dst, int metric) {
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
		return new int[] {metric};
	}
	@Override
	public String toString() {
		return src + "‒" + metric + "→" + dst;
	}
}
