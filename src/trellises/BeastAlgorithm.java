package trellises;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.BeastAlgorithm.Path.PathIterator;

/**
 * 
 * @author stas
 *
 */
public class BeastAlgorithm {
	private static Logger logger = LoggerFactory.getLogger(BeastAlgorithm.class);
	
	public static class Path {
		static class PathTail {
			int index;
			PathTail prev;
			
			public PathTail(int index) {
				this.index = index;
				this.prev = null;
			}
			
			public PathTail(int index, PathTail prev) {
				this.index = index;
				this.prev = prev;
			}
		}
		
		static class PathIterator {
			PathTail tail;
			public PathIterator(PathTail tail) {
				this.tail = tail;
			}
			
			public boolean hasPrev() {
				return tail != null;
			}
			
			public int prev() {
				if (!hasPrev()) {
					throw new NoSuchElementException();
				}
				int index = tail.index;
				tail = tail.prev;
				
				return index;
			}
		}
		
		private int length;
		private double weight;

		private PathTail tail;
		
		public Path(int vertexIndex) {
			length = 1;
			weight = 0;
			tail = new PathTail(vertexIndex);
		}
		
		public Path(Path path) {
			length = path.length;
			weight = path.weight;
			tail = path.tail;
		}
		
		public int length() {
			return length;
		}
		
		public double weight() {
			return weight;
		}
		
		public void addVertex(int vertexIndex, double delta_weight) {
			PathTail newTail = new PathTail(vertexIndex, tail);
			tail = newTail;
			
			++length;
			weight += delta_weight;
		}
		
		public void addReversedPath(PathIterator iterator, double weight) {
			for (; iterator.hasPrev(); ) {
				addVertex(iterator.prev(), 0);
			}
			
			this.weight += weight;
		}

		public PathIterator iterator() {
			return new PathIterator(tail);
		}
		
		@Override
		public String toString() {
			// По построению путь содержит хотя бы одну вершину.
			String str = iterator().prev() + " : " + weight;
			while (iterator().hasPrev()) {
				str = iterator().prev() + "-" + str;
			}
			return str;
		}
	}
	
	public static class PathCollector implements Comparable<PathCollector> {
		/**
		 * Итератор, указывающий на последнюю вершину в пути
		 */
		public ITrellisIterator iterator;
		/**
		 * Собираемый путь
		 */
		public Path path;
		
		public PathCollector() {
			iterator = null;
			path = null;
		}
		
		public PathCollector(ITrellisIterator iterator) {
			this.iterator = iterator;
			this.path = new Path(iterator.vertexIndex());
		}
		
		public PathCollector(final PathCollector collector) {
			iterator = collector.iterator.clone();
			path = new Path(collector.path);
		}
		
		
		/**
		 * Пути равны, если идут к одной вершине
		 */
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PathCollector)) {
				return false;
			}
			
			if (obj == this) {
				return true;
			}
			
			PathCollector collector = (PathCollector) obj;
			
			return iterator.layer() == collector.iterator.layer() &&
				iterator.vertexIndex() == collector.iterator.vertexIndex();
		}

		@Override
		public int compareTo(PathCollector collector) {
			if (iterator.layer() == collector.iterator.layer()) {
				return iterator.vertexIndex() - collector.iterator.vertexIndex();
			}
			return iterator.layer() - collector.iterator.layer();
		}
		
		@Override
		public String toString() {
			return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + path.weight + ")";
		}
	}
	
	static class Vertex implements Comparable<Vertex> {
		int layer;
		int index;
		int weight;
		long pathNumber;
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Vertex)) {
				return false;
			}
			
			if (obj == this) {
				return true;
			}
			
			Vertex vertex = (Vertex) obj;

			return layer == vertex.layer && index == vertex.index;
		}

		@Override
		public int compareTo(Vertex vertex) {
			if (layer == vertex.layer) {
				return index - vertex.index;
			}
			return layer - vertex.layer;
		}
	}

	/**
	 * Алгоритм BEAST для поиска минимального/свободного расстояния кода в решетке.
	 * 
	 * Метод ищет кратчайшие ненулевые пути в заданной метрике от между вершинами 
	 * решетки <code>root</code> и <code>toor</code>. Метрики путей хранятся в 
	 * решетке, конкретная метрика выбирается агрументом <code>metric</code>.
	 * 
	 * Нулевой путь в решетке должен проходить только через вершины с индексом 0, 
	 * из одной вершины нулевого пути в другую вершину нулевого пути должно вести ребро с индексом 0.    
	 * 
	 *  В целом работа алгоритма предполагает, что вершины <code>root</code> и 
	 *  <code>toor</code> лежат на нулевом пути, и ищутся все ненулевые пути между ними.  
	 */
	public static Path[] findOptimalPaths(ITrellisIterator root, ITrellisIterator toor, int metric, double weights[]) {
		TreeSet<PathCollector> Forward = new TreeSet<PathCollector>(), Backward = new TreeSet<PathCollector>();
		double tresholdForward = 0, tresholdBackward = 0;
	
		PathCollector rootPath = new BeastAlgorithm.PathCollector(root);
		Forward.add(rootPath);

		PathCollector toorPath = new BeastAlgorithm.PathCollector(toor);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0) {
			// добавляем все вершины нулевого пути/цикла в решетке. 
			for (PathCollector zeroPath = new PathCollector(toor); zeroPath.iterator.hasBackward() && zeroPath.iterator.vertexIndex() != toor.vertexIndex(); ) {
				zeroPath.iterator.moveBackward(0);
				zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
				Backward.add(new PathCollector(zeroPath));
			}
		}
		
		ArrayList<Path> paths = new ArrayList<Path>();
		
		for (double weight: weights) {
			if (Forward.size() < Backward.size()) {
				tresholdForward += weight;
				Forward = findForwardPaths(Forward, tresholdForward, metric);
			} else {
				tresholdBackward += weight;
				Backward = findBackwardPaths(Backward, tresholdBackward, metric);
			}
			
			for (PathCollector fpath : Forward) {
				if (Backward.contains(fpath)) {
					PathCollector bpath = Backward.floor(fpath);
					
					if (fpath.path.weight() == 0 && bpath.path.weight() == 0) {
						// нулевой путь, пропускаем
					}
					
					Path newPath = new BeastAlgorithm.Path(fpath.path);
					// Добавляем обратный (backward) путь к прямому (forward). 
					// Перед добавлением пропускаем первую вершину обратного пути, т.к. она совпадает с последней в прямом.
					PathIterator iterator = bpath.path.iterator();
					if (iterator.hasPrev()) {
						iterator.prev();
					}
					newPath.addReversedPath(iterator, bpath.path.weight());
					
					paths.add(newPath);
				}
			}
			
			if (!paths.isEmpty()) {
				break;
			}
		}

		return paths.toArray(new Path[0]);
	}
	
	public static int[] findSpectrum(Trellis trellis, int metric, int maxWeight, int codeLength) {
		int spectrum[] = new int[maxWeight];
		for (int i = 0; i < maxWeight; ++i) {
			spectrum[i] = 0;
		}
		
		for (int i = 0; i < trellis.Layers.length; ++i) {
			int curSpectrum[] = findSpectrum(trellis, i, metric, maxWeight, codeLength);
			
			for (int j = 0; j < maxWeight; ++j) {
				spectrum[j] += curSpectrum[j];
			}
		}
		
		return spectrum;
	}
	
	public static int[] findSpectrum(Trellis trellis, int toorLayer, int metric, int maxWeight, int codeLength) {
		TreeSet<Vertex> forward = new TreeSet<Vertex>(), backward = new TreeSet<Vertex>();
		
		Vertex root = new BeastAlgorithm.Vertex();
		root.layer = root.index = root.weight = 0;
		root.pathNumber = 1;
		forward.add(root);
		
		Vertex toor = new BeastAlgorithm.Vertex();
		toor.layer = toorLayer;
		toor.index = toor.weight = 0;
		toor.pathNumber = 1;
		backward.add(toor);
		
		int spectrum[] = new int[maxWeight];
		for (int i = 0; i < maxWeight; ++i) {
			spectrum[i] = 0;
		}
		
		for (int weight = 1; weight <= maxWeight; ++weight) {
			forward = countForwardPath(forward, weight/2, trellis, metric);
			backward = countBackwardPath(forward, weight - weight/2, trellis, metric, toorLayer);
			
			for (Vertex fvertex : forward) {
				if (backward.contains(fvertex)) {
					Vertex bvertex = backward.floor(fvertex);

					if (fvertex.weight + bvertex.weight == 0) {
						continue;
					}
					
					spectrum[weight - 1] += fvertex.pathNumber * bvertex.pathNumber;
				}
			}
		}
		
		return spectrum;
	}

	private static TreeSet<Vertex> countForwardPath(TreeSet<Vertex> forward,
			int weight, Trellis trellis, int metric) {
		if (weight <= forward.first().weight) {
			return forward;
		}
		
		int startWeight = forward.first().weight;
		
		TreeSet<Vertex> forwards[] = new TreeSet[weight + 1];
		for (int i = 0; i <= weight; ++i) {
			forwards[i] = new TreeSet<Vertex>();
		}
		forwards[startWeight] = forward;
		
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int curWeight = startWeight; curWeight < weight; ++curWeight) {
				for (Iterator<Vertex> iterator = forwards[curWeight].iterator(); iterator.hasNext();) {
					Vertex vertex = iterator.next();
					if (vertex.layer != 0 && vertex.index == 0) {
						// Вершина на нулевом пути
						continue;
					}
					changed = true;
					iterator.remove();
					
					for (Trellis.Edge edge : trellis.Layers[vertex.layer][vertex.index].Accessors) {
						if (vertex.layer == 0 && vertex.index == 0 && edge.Metrics[metric] == 0) {
							// Запрещаем нулевой путь
							continue;
						}
						
						Vertex newVertex = new BeastAlgorithm.Vertex();
						
						newVertex.layer = vertex.layer + 1;
						newVertex.index = edge.Dst;
						newVertex.weight = vertex.weight + (int)edge.Metrics[metric];
						newVertex.pathNumber = vertex.pathNumber;
						
						if (forwards[newVertex.weight].contains(newVertex)) {
							Vertex oldVertex = forwards[newVertex.weight].floor(newVertex);
							oldVertex.pathNumber += newVertex.pathNumber;
						} else {
							forwards[newVertex.weight].add(newVertex);
						}
					}
				}
			}
		}
		
		for (Vertex vertex : forwards[weight]) {
			while (vertex.index != 0) {
				for (Trellis.Edge edge : trellis.Layers[vertex.layer][vertex.index].Accessors) {
					if (edge.Metrics[metric] == 0) {
						++vertex.layer;
						vertex.index = edge.Dst;
					}
				}				
			}
		}
		
		return forwards[weight];
	}

	private static TreeSet<Vertex> countBackwardPath(TreeSet<Vertex> backward,
			int weight, Trellis trellis, int metric, int toorLayer) {
		if (weight <= backward.first().weight) {
			return backward;
		}
		
		int startWeight = backward.first().weight;
		
		TreeSet<Vertex> backwards[] = new TreeSet[weight + 1];
		for (int i = 0; i <= weight; ++i) {
			backwards[i] = new TreeSet<Vertex>();
		}
		backwards[startWeight] = backward;

		boolean changed = true;
		while (changed) {
			changed = false;
			for (int curWeight = startWeight; curWeight < weight; ++curWeight) {
				for (Iterator<Vertex> iterator = backwards[curWeight].iterator(); iterator.hasNext();) {
					Vertex vertex = iterator.next();
					if (vertex.layer != 0 && vertex.index == 0) {
						// Вершина на нулевом пути
						continue;
					}
					changed = true;
					iterator.remove();
					
					for (Trellis.Edge edge : trellis.Layers[vertex.layer][vertex.index].Accessors) {
						if (vertex.layer == toorLayer && vertex.index == 0 && edge.Metrics[metric] == 0) {
							// Запрещаем нулевой путь
							continue;
						}
						
						Vertex newVertex = new BeastAlgorithm.Vertex();
						
						newVertex.layer = vertex.layer + 1;
						newVertex.index = edge.Dst;
						newVertex.weight = vertex.weight + (int)edge.Metrics[metric];
						newVertex.pathNumber = vertex.pathNumber;
						
						if (backwards[newVertex.weight].contains(newVertex)) {
							Vertex oldVertex = backwards[newVertex.weight].floor(newVertex);
							oldVertex.pathNumber += newVertex.pathNumber;
						} else {
							backwards[newVertex.weight].add(newVertex);
						}
					}
				}
			}
		}
		
		return backwards[weight];
	}

	private static TreeSet<PathCollector> findForwardPaths(TreeSet<PathCollector> Forward,
			double tresholdForward, int metric) {
		logger.debug("tresholdForward = " + tresholdForward);
		
		TreeSet<PathCollector> newForward = new TreeSet<PathCollector>();
		
		while (!Forward.isEmpty()) {
			TreeSet<PathCollector> oldForward = new TreeSet<PathCollector>();
			
			for (Iterator<PathCollector> iterator = Forward.iterator(); iterator.hasNext();) {
				PathCollector path = iterator.next();
				iterator.remove();
				
				if (path.path.weight() >= tresholdForward || !path.iterator.hasForward()) {
					addTheLeastPath(newForward, path);
					continue;
				}

				for (int i = 0; i < path.iterator.getAccessors().length; ++i) {
					Trellis.Edge edge = path.iterator.getAccessors()[i];
					if (path.iterator.vertexIndex() == 0 && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь
						continue;
					}

					PathCollector newPath = new BeastAlgorithm.PathCollector(path);				// копируем путь
					newPath.iterator.moveForward(i);											// двигаемся вперед
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.Metrics[metric]);
										
					if (newPath.path.weight >= tresholdForward) {
						addTheLeastPath(newForward, newPath);
					} else {
						addTheLeastPath(oldForward, newPath);
					}
				}
			}
			
			Forward.addAll(oldForward);
		}
		
		logger.debug("newForward: "  + newForward);
		return newForward;
	}

	private static TreeSet<PathCollector> findBackwardPaths(TreeSet<PathCollector> Backward,
			double tresholdBackward, int metric) {
		logger.debug("tresholdBackward = " + tresholdBackward);

		TreeSet<PathCollector> newBackward = new TreeSet<PathCollector>();
		
		while (!Backward.isEmpty()) {
			TreeSet<PathCollector> oldBackward = new TreeSet<PathCollector>();
			
			for (Iterator<PathCollector> iterator = Backward.iterator(); iterator.hasNext();) {
				PathCollector path = iterator.next();
				iterator.remove();
				
				if (!path.iterator.hasBackward()) {
					addTheLeastPath(newBackward, path);	// некуда дальше двигаться, переносим путь в новое множество
					continue;
				}
				
				for (int i = 0; i < path.iterator.getPredecessors().length; ++i) {
					Trellis.Edge edge = path.iterator.getPredecessors()[i];
					if (path.iterator.vertexIndex() == 0 && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь
						continue;
					}
					
					PathCollector newPath = new BeastAlgorithm.PathCollector(path);				// копируем путь
					newPath.iterator.moveBackward(i);											// двигаемся назад
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.Metrics[metric]);
					
					if (newPath.path.weight > tresholdBackward) {
						addTheLeastPath(newBackward, path);
					} else {
						addTheLeastPath(oldBackward, newPath);
					}
				}
			}
			
			Backward.addAll(oldBackward);
		}
		
		logger.debug("newBackward: "  + newBackward);
		return newBackward;
	}

	/**
	 * Метод добавляет <code>path</code> в множество <code>pathes</code>, если в нем не содержится путь, эквивалентный <code>path</code>,
	 * с меньшей метрикой. Более строго, <code>path</code> не добавляется тогда, когда в <code>pathes</code> уже есть путь <code>path2</code>
	 * такой, что <code>path2.equals(path) && path2.metric <= path.metric</code>.  
	 * @param pathes множество путей
	 * @param path добавляемый путь
	 */
	private static void addTheLeastPath(TreeSet<PathCollector> pathes, PathCollector path) {
		if (pathes.contains(path)) {
			PathCollector path2 = pathes.floor(path);
			if (path.equals(path2) && path2.path.weight > path.path.weight) {
				// существующий путь с большей метрикой - удаляем его
				pathes.remove(path2);
			}
		}
		
		pathes.add(path); // если в pathes был путь с меньшей метрикой, то path не добавится.
	}
}
