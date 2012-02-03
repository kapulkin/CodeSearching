package trellises.algorithms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HashMapLayeredFront<T extends PathTracker<T>> extends AbstractFront<T> implements Front<T> {
	HashMap<Integer, Map<Long, T>> layers = new HashMap<Integer, Map<Long,T>>();
	
	public HashMapLayeredFront() {
		
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T> () {
			Iterator<Map<Long, T>> layerIter = layers.values().iterator();
			Map<Long, T> currentLayer;
			Iterator<T> iter = null;
			@Override
			public boolean hasNext() {
				return (iter != null && iter.hasNext()) || layerIter.hasNext();
			}

			@Override
			public T next() {
				if (iter == null || !iter.hasNext()) {
					currentLayer = layerIter.next();
					iter = currentLayer.values().iterator();
				}
				return iter.next();
			}

			@Override
			public void remove() {
				iter.remove();
				--size;
				if (currentLayer.isEmpty()) {
					layerIter.remove();
				}
			}
		};
	}

	@Override
	public boolean add(T t) {
		Map<Long, T> layerMap = layers.get(t.layer());
		if (layerMap == null) {
			layerMap = new HashMap<Long, T>();
			layers.put(t.layer(), layerMap);
		}
		if (null == layerMap.put(t.vertexIndex(), t)) {
			++size;
			return true;
		}
		return false;
	}

	@Override
	public T get(int layer, long vertexIndex) {
		Map<Long, T> layerMap = layers.get(layer);
		if (layerMap == null) {
			return null;
		}
		return layerMap.get(vertexIndex);
	}

	@Override
	public Iterable<T> getLayer(int layer) {
		Map<Long, T> layerMap = layers.get(layer);
		if (layerMap == null) {
			return null;
		}
		return layerMap.values();
	}
	
	@Override
	public boolean remove(int layer, long vertexIndex) {
		Map<Long, T> layerMap = layers.get(layer);
		if (layerMap == null) {
			return false;
		}		
		T removed = layerMap.remove(vertexIndex);
		if (removed != null) {
			--size;
			if (layerMap.isEmpty()) {
				layers.remove(layer);
			}
		}
		return true;
	}
	
	@Override
	public boolean contains(int layer, long vertexIndex) {
		Map<Long, T> layerMap = layers.get(layer);
		if (layerMap == null) {
			return false;
		}
		return layerMap.containsKey(vertexIndex);
	}
	
	@Override
	public void clear() {
		for (Map<Long, T> layer : layers.values()) {
			layer.clear();
		}
		layers.clear();
	}
}
