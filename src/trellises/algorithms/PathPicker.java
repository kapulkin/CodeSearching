package trellises.algorithms;

import java.util.ArrayList;
import java.util.Iterator;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

/**
 * Сохраняет путь, пройденный в решетке
 * @author stas
 *
 */
public class PathPicker implements PathTracker, Comparable<PathPicker> {
	//TODO: после рефакторинга сделать 3 поля ниже приватными 
	/**
	 * Итератор, указывающий на последнюю вершину в пути
	 */
	ITrellisIterator iterator;
	/**
	 * Номер метрики на ребрах, используемой для вычисления длины пути
	 */
	int metric;
	/**
	 * Собираемый путь
	 */
	TrellisPath path;
	
	public PathPicker(ITrellisIterator iterator, int metric) {
		this.iterator = iterator;
		this.metric = metric;
		this.path = new TrellisPath(iterator.vertexIndex());
	}
	
	public PathPicker(final PathPicker tracker) {
		iterator = tracker.iterator.clone();
		metric = tracker.metric;
		path = new TrellisPath(tracker.path);
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
			PathPicker tracker = this.clone();
			tracker.iterator.moveForward(i);
			path.addVertex(iterator.vertexIndex(), edges[i].metric(metric));
			trackers.add(tracker);
		}

		return trackers.iterator();
	}

	@Override
	public Iterator<PathTracker> backwardIterator() {
		ITrellisEdge edges[] = iterator.getPredecessors();
		
		ArrayList<PathTracker> trackers = new ArrayList<PathTracker>(edges.length);
		for (int i = 0; i < edges.length; ++i) {
			PathPicker tracker = this.clone();
			tracker.iterator.moveBackward(i);
			path.addVertex(iterator.vertexIndex(), edges[i].metric(metric));
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

	public int weight() {
		return path.weight();
	}
	
	public TrellisPath path() {
		return path;
	}
	
	@Override
	public PathPicker clone() {
		return new PathPicker(this);
	}
	
	/**
	 * Пути равны, если идут к одной вершине
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PathPicker)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		
		PathTracker picker = (PathTracker) obj;
		
		return iterator.layer() == picker.layer() &&
			iterator.vertexIndex() == picker.vertexIndex();
	}

	public int compareTo(PathPicker tracker) {
		return BeastAlgorithm.comparePathTrackers(this, tracker);
	}
	
	@Override
	public String toString() {
		return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + path.weight() + ")";
	}
}