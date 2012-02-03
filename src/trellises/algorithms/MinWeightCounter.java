package trellises.algorithms;

public class MinWeightCounter<T extends PathTracker<T>> implements TrackProcessor<T> {
	private int minWeight = Integer.MAX_VALUE;
	private int minLayer;
	private long minVertexIndex;
	
	@Override
	public void process(T forwardTrack, T backwardTrack) {
		int trackWeight = forwardTrack.weight()  + backwardTrack.weight();
		if (minWeight > trackWeight) {
			minWeight = trackWeight;
			minLayer = forwardTrack.layer();
			minVertexIndex = forwardTrack.vertexIndex();
		}
	}
	
	public int getMinWeight() {
		return minWeight; 
	}
	
	public int getMinLayer() {
		return minLayer;
	}
	
	public long getMinVertexIndex() {
		return minVertexIndex;
	}
}
