package trellises;

import java.util.ArrayList;
import java.util.Iterator;

class PathCounter implements PathTracker, Comparable<PathTracker> {
	//TODO: после рефакторинга сделать 4 поля ниже приватными 
	/**
	 * Итератор текущей вершины
	 */
	ITrellisIterator iterator;
	/**
	 * Номер метрики на ребрах, используемой для вычисления длины пути
	 */
	int metric;
	/**
	 * Вес пройденнного пути
	 */
	int weight;
	/**
	 * Колличество путей до текущей вершины
	 */
	long pathNumber;
	
	public PathCounter(ITrellisIterator iterator, int metric) {
		this.iterator = iterator;
		this.metric = metric;
		this.weight = 0;
		this.pathNumber = 1;
	}
	
	public PathCounter(PathCounter vertex) {
		iterator = vertex.iterator.clone();
		metric = vertex.metric;
		weight = vertex.weight;
		pathNumber = vertex.pathNumber;
	}

	@Override
	public boolean hasForward() {
		return iterator.hasForward();
	}

	@Override
	public boolean hasBackward() {
		return iterator.hasBackward();
	}
	
	@Override
	public Iterator<PathTracker> forwardIterator() {
		ITrellisEdge edges[] = iterator.getAccessors();
		
		ArrayList<PathTracker> trackers = new ArrayList<PathTracker>(edges.length);
		for (int i = 0; i < edges.length; ++i) {
			PathCounter tracker = this.clone();
			tracker.iterator.moveForward(i);
			tracker.weight += edges[i].metrics()[metric];
			trackers.add(tracker);
		}

		return trackers.iterator();
	}

	@Override
	public Iterator<PathTracker> backwardIterator() {
		ITrellisEdge edges[] = iterator.getPredecessors();
		
		ArrayList<PathTracker> trackers = new ArrayList<PathTracker>(edges.length);
		for (int i = 0; i < edges.length; ++i) {
			PathCounter tracker = this.clone();
			tracker.iterator.moveBackward(i);
			tracker.weight += edges[i].metrics()[metric];
			trackers.add(tracker);
		}

		return trackers.iterator();
	}

	@Override
	public int layer() {
		return iterator.layer();
	}

	@Override
	public long vertexIndex() {
		return iterator.vertexIndex();
	}

	public double weight() {
		return (double) weight;
	}

	public PathCounter clone() {
		return new PathCounter(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		PathTracker vertex = (PathTracker) obj;
		if (vertex == null) {
			return false;
		}

		return iterator.layer() == vertex.layer() &&
			iterator.vertexIndex() == vertex.vertexIndex();
	}

	public int compareTo(PathTracker tracker) {
		return BeastAlgorithm.comparePathTrackers(this, tracker);
	}

	@Override
	public String toString() {
		return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + weight + ")";
	}
}