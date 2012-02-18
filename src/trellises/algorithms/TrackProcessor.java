package trellises.algorithms;

public interface TrackProcessor<T extends PathTracker<T>> {
	void process(T forwardTrack, T backwardTrack);
}
