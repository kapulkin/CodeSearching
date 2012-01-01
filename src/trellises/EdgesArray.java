package trellises;

import math.BitArray;

public class EdgesArray {
	static public class Edge implements ITrellisEdge {
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
		/**
		 * Кодовые символы на ребре
		 */
		public BitArray bits;
	
		public Edge(int src, int dst, BitArray bits, int metric) {
			this.src = src;
			this.dst = dst;
			this.bits = bits;
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
			return bits;
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
			return src + "‒" + bits + "→" + dst;
		}
	}

	public class EdgeReference implements ITrellisEdge {
		int edgeIndex;
		int metric;
		
		public EdgeReference(int edgeIndex) {
			this.edgeIndex = edgeIndex;
			metric = metrics[edgeIndex];
		}
		
		@Override
		public long src() {
			return src[edgeIndex];
		}
		@Override
		public long dst() {
			return dst[edgeIndex];
		}
		@Override
		public BitArray bits() {
			return bits.get(edgeIndex * sectionLength, (edgeIndex + 1) * sectionLength);
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
			return src() + "‒" + bits() + "→" + dst();
		}
	}
	
	
	int sectionLength;
	int edgesCount;
	
	BitArray bits;
	int src[];
	int dst[];
	int metrics[];
	
	public EdgesArray(int capacity, int sectionLength) {
		src = new int[capacity];
		dst = new int[capacity];
		metrics = new int[capacity];

		this.sectionLength = sectionLength;
		bits = new BitArray(capacity * sectionLength);
	}
	
	public int size() {
		return edgesCount;
	}
	
	public Edge getEdge(int edgeIndex) {
		if (edgeIndex < 0 || edgeIndex >= edgesCount) {
			throw new IndexOutOfBoundsException(String.valueOf(edgeIndex));
		}
		return new Edge(src[edgeIndex], dst[edgeIndex], bits.get(edgeIndex * sectionLength, (edgeIndex + 1) * sectionLength), metrics[edgeIndex]);
	}
	
	public int addEdge(ITrellisEdge edge) {
		return addEdge((int)edge.src(), (int)edge.dst(), edge.bits(), edge.metric(0));
	}
	
	public int addEdge(int src, int dst, BitArray bits, int metric) {
		if (bits == null) {
			if (sectionLength != 0) {
				throw new IllegalArgumentException("bits are null, but not expected to be.");
			}
		}
		else if (bits.getFixedSize() != sectionLength) {
			throw new IllegalArgumentException("Wrong fixed size of bits: " + bits.getFixedSize());
		}

		this.src[edgesCount] = src;
		this.dst[edgesCount] = dst;
		for (int i = 0, j = edgesCount * sectionLength; i < sectionLength; ++i, ++j) {
			this.bits.set(j, bits.get(i));
		}
		this.metrics[edgesCount] = metric;
		
		return edgesCount++;
	}
}