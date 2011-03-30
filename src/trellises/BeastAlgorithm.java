package trellises;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * 
 * @author stas
 *
 */
public class BeastAlgorithm {
	public static final boolean DISTANCE = false;
	public static final boolean DECODING = true; 

	public static final boolean BLOCK = false;
	public static final boolean CONVOLUTIONAL = true; 
	
	public class Path {
		class PathTail {
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
		
		class PathIterator {
			PathTail tail;
			public PathIterator(PathTail tail) {
				this.tail = tail;
			}
			
			public boolean hasPrev() {
				return this.tail != null;
			}
			
			public int prev() {
				if (!hasPrev()) {
					throw new NoSuchElementException();
				}
				int index = this.tail.index;
				this.tail = this.tail.prev;
				
				return index;
			}
		}
		
		private int length;
		private double metric;

		private PathTail tail;
		
		public Path(int vertexIndex) {
			length = 1;
			metric = 0;
			tail = new PathTail(vertexIndex);
		}
		
		public Path(Path path) {
			length = path.length;
			metric = path.metric;
			tail = path.tail;
		}
		
		public int length() {
			return length;
		}
		
		public double metric() {
			return metric;
		}
		
		public void addVertex(int vertexIndex, double delta_metric) {
			PathTail newTail = new PathTail(vertexIndex, tail);
			tail = newTail;
			
			++length;
			metric += delta_metric;
		}
		
		public void addReversedPath(Path path) {
			for (PathIterator iterator = path.iterator(); iterator.hasPrev(); ) {
				addVertex(iterator.prev(), 0);
			}
			
			metric += path.metric;
		}
		
		public PathIterator iterator() {
			return new PathIterator(tail);
		}
	}
	
	public class PathCollector implements Comparable<PathCollector> {
		/**
		 * Итератор, указывающий на последнюю вершину в пути
		 */
		public TrellisIterator iterator;
		/**
		 * Собираемый путь
		 */
		public Path path;
		
		public PathCollector() {
			iterator = null;
			path = null;
		}
		
		public PathCollector(TrellisIterator iterator) {
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
			PathCollector collector = (PathCollector) obj;
			if (collector == null) {
				return false;
			}
			
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
	}
	
	class Vertex implements Comparable<Vertex> {
		int layer;
		int index;
		int weight;
		long pathNumber;
		
		@Override
		public boolean equals(Object obj) {
			Vertex vertex = (Vertex) obj;
			if (vertex == null) {
				return false;
			}
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

	// декодирование с помощью BEAST работает эффективно только для блоковых кодов
	/**
	 * Метод ищет кратчайшие в заданной метрике пути от нулевого яруса (root layer) решетки 
	 * до <code>toorLayer</code> яруса решетки. 
	 * Поиск путей ведется между вершинами в указанных ярусах с индексом <code>initialVertex</code>.
	 * Метрики путей хранится в решетке, конкретная меткрика выбирается агрументом <code>metric</code>.
	 */
	public static Path[] findOptimalPaths(TrellisIterator root, TrellisIterator toor, int metric, double weights[], boolean mode) {
		TreeSet<PathCollector> Forward = new TreeSet<PathCollector>(), Backward = new TreeSet<PathCollector>();
		double tresholdForward = 0, tresholdBackward = 0;
		
		PathCollector rootPath = new BeastAlgorithm().new PathCollector(root);
		Forward.add(rootPath);

		PathCollector toorPath = new BeastAlgorithm().new PathCollector(toor);
		Backward.add(toorPath);
		
		ArrayList<Path> paths = new ArrayList<Path>();
		
		for (double weight: weights) {
			if (Forward.size() < Backward.size()) {
				tresholdForward += weight;
				Forward = findForwardPaths(Forward, tresholdForward, root, toor, metric, mode);
			} else {
				tresholdBackward += weight;
				Backward = findBackwardPaths(Backward, tresholdBackward, root, toor, metric, mode);
			}
			
			for (PathCollector fpath : Forward) {
				if (Backward.contains(fpath)) {
					PathCollector bpath = Backward.floor(fpath);
					
					if (fpath.path.length() == 1 && bpath.path.length() == 1) {
						// начальная вершина совпала с конечной и мы никуда не пошли.
						continue;
					}
					
					Path newPath = new BeastAlgorithm().new Path(fpath.path);
					newPath.addReversedPath(bpath.path);
					
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
		
		Vertex root = new BeastAlgorithm().new Vertex();
		root.layer = root.index = root.weight = 0;
		root.pathNumber = 1;
		forward.add(root);
		
		Vertex toor = new BeastAlgorithm().new Vertex();
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
						
						Vertex newVertex = new BeastAlgorithm().new Vertex();
						
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
						
						Vertex newVertex = new BeastAlgorithm().new Vertex();
						
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
			double tresholdForward, TrellisIterator root, TrellisIterator toor, int metric, boolean mode) {
		TreeSet<PathCollector> newForward = new TreeSet<PathCollector>();
		
		while (!Forward.isEmpty()) {
			TreeSet<PathCollector> oldForward = new TreeSet<PathCollector>();
			
			for (Iterator<PathCollector> iterator = Forward.iterator(); iterator.hasNext();) {
				PathCollector path = iterator.next();
				iterator.remove();
				
				if (path.path.metric() >= tresholdForward || !path.iterator.hasForward()) {
					addTheLeastPath(newForward, path);
					continue;
				}
				
				for (int i = 0; i < path.iterator.getAccessors().length; ++i) {
					Trellis.Edge edge = path.iterator.getAccessors()[i];
					if (mode == DISTANCE && path.iterator == root && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь из root-а
						continue;
					}

					PathCollector newPath = new BeastAlgorithm().new PathCollector(path);		// копируем путь
					newPath.iterator.moveForward(i);											// двигаемся вперед
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.Metrics[metric]);
										
					if (newPath.path.metric >= tresholdForward) {
						addTheLeastPath(newForward, newPath);
					} else {
						addTheLeastPath(oldForward, newPath);
					}
				}
			}
			
			Forward.addAll(oldForward);
		}
		
		return newForward;
	}

	private static TreeSet<PathCollector> findBackwardPaths(TreeSet<PathCollector> Backward,
			double tresholdBackward, TrellisIterator root, TrellisIterator toor, int metric, boolean mode) {
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
					if (mode == DISTANCE && path.iterator == toor && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь из toor-а
						continue;
					}
					
					PathCollector newPath = new BeastAlgorithm().new PathCollector(path);		// копируем путь
					newPath.iterator.moveBackward(i);											// двигаемся назад
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.Metrics[metric]);
										
					if (newPath.path.metric > tresholdBackward) {
						addTheLeastPath(newBackward, path);
					} else {
						addTheLeastPath(oldBackward, newPath);
					}
				}
			}
			
			Backward.addAll(oldBackward);
		}
		
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
			if (path.equals(path2) && path2.path.metric > path.path.metric) {
				// существующий путь с большей метрикой - удаляем его
				pathes.remove(path2);
			}
		}
		
		pathes.add(path); // если в pathes был путь с меньшей метрикой, то path не добавится.
	}
}
