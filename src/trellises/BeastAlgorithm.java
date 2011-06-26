package trellises;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.TrellisPath;
import trellises.TrellisPath.PathIterator;

/**
 * 
 * @author stas
 *
 */
public class BeastAlgorithm {
	private static Logger logger = LoggerFactory.getLogger(BeastAlgorithm.class);

	public static interface BeastTraversalPicker {
		public void pickForward(PathTracker tracker);
		public void pickBackward(PathTracker tracker);
	}

	public static class SimpleBeastPeaker implements BeastTraversalPicker {
		private Logger logger2 = LoggerFactory.getLogger(this.getClass());

		private TreeSet<PathTracker> Forward, Backward;
		private TreeSet<PathTracker> oldForward, oldBackward;
		private TreeSet<PathTracker> newForward, newBackward;
		double tresholdForward, tresholdBackward;
		double metric;
		ITrellisIterator root, toor;

		PathTracker tracker;
		
		public SimpleBeastPeaker(TreeSet<PathTracker> Forward, TreeSet<PathTracker> Backward,
				ITrellisIterator root, ITrellisIterator toor, int metric) {
			this.Forward = Forward;
			this.Backward = Backward;
			this.metric = metric;
			this.root = root;
			this.toor = toor;
		}
		
		TreeSet<PathTracker> getForwardFront() {
			return Forward;
		}
		
		TreeSet<PathTracker> getBackwardFront() {
			return Backward;
		}

		public void builtForwardFront(int tresholdForward, ITrellisIterator root, int metric) {
			this.tresholdForward = tresholdForward;
			logger2.debug("tresholdForward = " + tresholdForward);
			
			newForward = new TreeSet<PathTracker>();

			while (!Forward.isEmpty()) {
				oldForward = new TreeSet<PathTracker>();
				
				for (Iterator<PathTracker> iterator = Forward.iterator(); iterator.hasNext();) {
					tracker = iterator.next();
					iterator.remove();
					
					if (tracker.weight() >= tresholdForward) {
						addTheLeastPath(newForward, tracker);
						continue;
					}
					if (!tracker.iterator().hasForward()) {
						continue;
					}
					
					buildNextPaths(tracker, this, root, metric);
				}
				
				Forward.addAll(oldForward);
			}
			
			logger2.debug("newForward: "  + newForward);
			Forward = newForward;
			
			oldForward = newForward = null;
		}

		public void builtBackwardFront(int tresholdBackward, int metric) {
			this.tresholdBackward = tresholdBackward;
			logger2.debug("tresholdBackward = " + tresholdBackward);
			
			newBackward = new TreeSet<PathTracker>();

			while (!Backward.isEmpty()) {
				oldBackward = new TreeSet<PathTracker>();
				
				for (Iterator<PathTracker> iterator = Backward.iterator(); iterator.hasNext();) {
					tracker = iterator.next();
					iterator.remove();
					
					if (!tracker.iterator().hasBackward()) {
						continue;
					}
					
					buildPrevPaths(tracker, this, tresholdBackward, metric);
				}
				
				Backward.addAll(oldBackward);
			}
			
			logger2.debug("newBackward: "  + newBackward);
			Backward = newBackward;
			
			oldBackward = newBackward = null;
		}

		@Override
		public void pickForward(PathTracker newTracker) {
			if (newTracker.weight() >= tresholdForward) {
				addTheLeastPath(newForward, newTracker);
			} else {
				addTheLeastPath(oldForward, newTracker);
			}
		}

		@Override
		public void pickBackward(PathTracker newTracker) {
			if (newTracker.weight() > tresholdForward) {
				addTheLeastPath(newBackward, tracker);
			} else {
				addTheLeastPath(oldBackward, newTracker);
			}
		}
	}
	
	/**
	 * Собирает путь, который отслеживает
	 * @author stas
	 *
	 */
	public static class PathPicker implements PathTracker {
		/**
		 * Итератор, указывающий на последнюю вершину в пути
		 */
		private ITrellisIterator iterator;
		/**
		 * Собираемый путь
		 */
		private TrellisPath path;
		
		public PathPicker(ITrellisIterator iterator) {
			this.iterator = iterator;
			this.path = new TrellisPath(iterator.vertexIndex());
		}
		
		public PathPicker(final PathPicker tracker) {
			iterator = tracker.iterator.clone();
			path = new TrellisPath(tracker.path);
		}

		public void moveForward(int edgeIndex, double edgeWeight) {
			iterator.moveForward(edgeIndex);
			path.addVertex(iterator.vertexIndex(), edgeWeight);
		}

		public void moveBackward(int edgeIndex, double edgeWeight) {
			iterator.moveBackward(edgeIndex);
			path.addVertex(iterator.vertexIndex(), edgeWeight);
		}

		public ITrellisIterator iterator() {
			return iterator; 
		}

		public double weight() {
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
			
			PathPicker picker = (PathPicker) obj;
			
			return iterator.layer() == picker.iterator.layer() &&
				iterator.vertexIndex() == picker.iterator.vertexIndex();
		}

		public int compareTo(PathTracker tracker) {
			if (!(tracker instanceof PathPicker)) {
				throw new IllegalArgumentException("Can compare only with " + this.getClass());
			}
			PathPicker pathPicker = (PathPicker) tracker;
			
			return compareTrellisIterators(iterator, pathPicker.iterator);
		}
		
		@Override
		public String toString() {
			return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + path.weight() + ")";
		}
	}
		
	static class Vertex implements PathTracker {
		private ITrellisIterator iterator;
		private int weight;
		long pathNumber;
		
		public Vertex(ITrellisIterator iterator) {
			this.iterator = iterator;
			this.weight = 0;
			this.pathNumber = 1;
		}
		
		public Vertex(Vertex vertex) {
			iterator = vertex.iterator.clone();
			weight = vertex.weight;
			pathNumber = vertex.pathNumber;
		}
		
		@Override
		public void moveForward(int edgeIndex, double edgeWeight) {
			iterator.moveForward(edgeIndex);
			weight += edgeWeight;
		}

		@Override
		public void moveBackward(int edgeIndex, double edgeWeight) {
			iterator.moveBackward(edgeIndex);
			weight += edgeWeight;
		}

		public ITrellisIterator iterator() {
			return iterator;
		}
		
		public double weight() {
			return (double) weight;
		}

		public Vertex clone() {
			return new Vertex(this);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			Vertex vertex = (Vertex) obj;
			if (vertex == null) {
				return false;
			}

			return iterator.layer() == vertex.iterator.layer() &&
				iterator.vertexIndex() == vertex.iterator.vertexIndex();
		}

		@Override
		public int compareTo(PathTracker tracker) {
			if (!(tracker instanceof Vertex)) {
				throw new IllegalArgumentException("Can compare only with " + this.getClass());
			}

			Vertex vertex = (Vertex) tracker;
			return compareTrellisIterators(iterator, vertex.iterator);
		}

		@Override
		public String toString() {
			return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + weight + ")";
		}
	}

	static private int compareTrellisIterators(ITrellisIterator a, ITrellisIterator b) {
		if (a.layer() == b.layer()) {
			return (int) (a.vertexIndex() - b.vertexIndex());
		}
		return a.layer() - b.layer();
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
	 * 
	 * @param root вершина, из которой ведется обход вперед
	 * @param toor вершина, из которой ведется обход назад
	 * @param metric номер метрики на ребрах, используемой для вычисления длины пути
	 * @param upperBound верхняя оценка на минимальное расстояние, может быть 
	 * сколь угодно большой, используется лишь для контроля на случай возможной ошбики в коде. 
	 * @return множество путей, содержащее кратчайший ненулевой путь между вершинами root и toor
	 */
	public static TrellisPath[] findOptimalPaths(ITrellisIterator root, ITrellisIterator toor, int metric, int upperBound) {
		TreeSet<PathTracker> Forward = new TreeSet<PathTracker>(), Backward = new TreeSet<PathTracker>();
 		int tresholdForward = 0, tresholdBackward = 0;
	
		PathPicker rootPath = new PathPicker(root);
		Forward.add(rootPath);

		PathPicker toorPath = new BeastAlgorithm.PathPicker(toor);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			PathPicker zeroPath = toorPath.clone();
			zeroPath.moveBackward(0, 0);
			while (zeroPath.iterator().layer() != toor.layer() && zeroPath.iterator().hasBackward()) {
				zeroPath.moveBackward(0, 0);
				Backward.add((PathTracker) zeroPath.clone());
			}
		}

		SimpleBeastPeaker peaker = new SimpleBeastPeaker(Forward, Backward, root, toor, metric);
		
		ArrayList<TrellisPath> paths = new ArrayList<TrellisPath>();
		
		while (tresholdForward + tresholdBackward <= upperBound) {
			
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				peaker.builtForwardFront(tresholdForward, root, metric);
//TODO:				Forward = findForwardPaths(Forward, tresholdForward, root, metric);
			} else {
				++tresholdBackward;
				peaker.builtBackwardFront(tresholdBackward, metric);
//TODO:				Backward = findBackwardPaths(Backward, tresholdBackward, metric);
			}

			Forward = peaker.getForwardFront();
			Backward = peaker.getBackwardFront();
			
			for (PathTracker tracker : Forward) {
				if (Backward.contains(tracker)) {
					PathPicker fpath = (PathPicker) tracker;
					PathPicker bpath = (PathPicker) Backward.floor(fpath);
					
					if (fpath.path.weight() == 0 && bpath.path.weight() == 0) {
						// нулевой путь, пропускаем
						continue;
					}
					
					TrellisPath newPath = new TrellisPath(fpath.path);
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

		return paths.toArray(new TrellisPath[0]);
	}/**/
	
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
	public static int countMinDist(ITrellisIterator root, ITrellisIterator toor, int metric, int upperBound) {
		TreeSet<PathTracker> Forward = new TreeSet<PathTracker>(), Backward = new TreeSet<PathTracker>();
		int tresholdForward = 0, tresholdBackward = 0;
	
		Vertex rootPath = new Vertex(root);
		Forward.add(rootPath);

		Vertex toorPath = new Vertex(toor);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			Vertex zeroPath = new Vertex(toorPath);
			zeroPath.iterator.moveBackward(0);
			while (zeroPath.iterator.layer() != toor.layer() && zeroPath.iterator.hasBackward()) {
				zeroPath.iterator.moveBackward(0);
				Backward.add(new Vertex(zeroPath));
			}
		}
		
		int minDist = -1;
		
		while (tresholdForward + tresholdBackward <= upperBound) {
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				Forward = findForwardFront(Forward, tresholdForward, root, metric);
			} else {
				++tresholdBackward;
				Backward = findBackwardPaths(Backward, tresholdBackward, metric);
			}
			
			for (PathTracker fpath : Forward) {
				if (Backward.contains(fpath)) {
					PathTracker bpath = Backward.floor(fpath);
					
					if (fpath.weight() == 0 && bpath.weight() == 0) {
						// нулевой путь, пропускаем
						continue;
					}
		
					if (minDist > fpath.weight() + bpath.weight() || minDist < 0) {
						minDist = (int) (fpath.weight() + bpath.weight());
					}
				}
			}
			
			if (minDist > 0) {
				break;
			}
		}

		return minDist;
	}
	
	public static long[] findSpectrum(ITrellisIterator root, ITrellisIterator toor, int maxWeight, int metric) {
		TreeMap<Integer, TreeSet<Vertex>> forwards = new TreeMap<Integer, TreeSet<Vertex>>();
		TreeMap<Integer, TreeSet<Vertex>> backwards = new TreeMap<Integer, TreeSet<Vertex>>();
		
		Vertex rootVertex = new Vertex(root);
		forwards.put(0, new TreeSet<Vertex>());
		forwards.get(0).add(rootVertex);
		
		Vertex toorVertex = new Vertex(toor);
		backwards.put(0, new TreeSet<Vertex>());
		backwards.get(0).add(toorVertex);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			Vertex zeroVertex = new Vertex(toorVertex);
			do {
				zeroVertex.iterator.moveBackward(0);
				backwards.get(0).add(new Vertex(zeroVertex));
			} while (zeroVertex.iterator.hasBackward() && zeroVertex.iterator.layer() != toor.layer());
		}
		
		long spectrum[] = new long[maxWeight];
		for (int i = 0; i < spectrum.length; ++i) {
			spectrum[i] = 0;
		}

		for (int tresholdForward = 0, tresholdBackward = 0; tresholdForward + tresholdBackward < maxWeight;) {
			int forwardSize = 0;
			for (int wi : forwards.keySet()) {
				if (forwards.get(wi) != null) {
					forwardSize += forwards.get(wi).size();
				}
			}
			int backwardSize = 0;
			for (int wi : backwards.keySet()) {
				if (backwards.get(wi) != null) {
					backwardSize += backwards.get(wi).size();
				}
			}
			
			if (forwardSize < backwardSize) {
				forwards = countForwardPath(forwards, ++tresholdForward, metric);
			} else {
				backwards = countBackwardPath(backwards, ++tresholdBackward, metric);
			}

			for (int wi = 0; wi <= tresholdBackward; ++wi) {
				TreeSet<Vertex> forward = forwards.get(tresholdForward + wi);
				TreeSet<Vertex> backward = backwards.get(tresholdBackward - wi);
	
				if (forward == null || backward == null) {
					continue;
				}
				
				for (Vertex fvertex : forward) {
					if (backward.contains(fvertex)) {
						Vertex bvertex = backward.floor(fvertex);
	
						if (fvertex.weight + bvertex.weight == 0) {
							continue;
						}
						
						spectrum[tresholdForward + tresholdBackward - 1] += fvertex.pathNumber * bvertex.pathNumber;
					}
				}
			}
		}
		
		return spectrum;
	}
	
	private static TreeMap<Integer,TreeSet<Vertex>> countForwardPath(TreeMap<Integer,TreeSet<Vertex>> forwards, int weight, int metric) {
		logger.debug("forward = " + forwards);
		
		/**
		 * Сюда пойдут все вершины веса >= weight
		 */
		TreeMap<Integer,TreeSet<Vertex>> newForwards = new TreeMap<Integer, TreeSet<Vertex>>();
		while (!forwards.isEmpty()) {
			/**
			 * Сюда будут идти все вершины веса < weight, пока мы от них наконец не дойдем до вершин большего веса. 
			 */
			TreeMap<Integer,TreeSet<Vertex>> oldForwards = new TreeMap<Integer, TreeSet<Vertex>>();

			for (Map.Entry<Integer,TreeSet<Vertex>> entry = forwards.pollFirstEntry(); entry != null; entry = forwards.pollFirstEntry()) {
				if (entry.getKey() >= weight) {
					// быстро перекидываем все вершины веса >= weight в newForwards
					if (newForwards.get(entry.getKey()) == null) {
						newForwards.put(entry.getKey(), entry.getValue());
					} else {
						for (Vertex vertex : entry.getValue()) {
							addVertex(newForwards.get(entry.getKey()), vertex);
						}
					}
					continue;
				}
				
				TreeSet<Vertex> forward = entry.getValue();
				for (Iterator<Vertex> iterator = forward.iterator(); iterator.hasNext();) {
					Vertex vertex = iterator.next();
					iterator.remove();
					
					if (!vertex.iterator.hasForward()) {
						continue;
					}
					// идем из вершины вперед.
					for (int i = 0; i < vertex.iterator.getAccessors().length; ++i) {
						ITrellisEdge edge = vertex.iterator.getAccessors()[i];
						if (vertex.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
							// Запрещаем нулевой путь
							continue;
						}

						Vertex newVertex = new Vertex(vertex);
						newVertex.iterator.moveForward(i);
						newVertex.weight += edge.metrics()[metric];

						if (newVertex.weight >= weight) {
							if (newForwards.get(newVertex.weight) == null) {
								newForwards.put(newVertex.weight, new TreeSet<Vertex>());
							}
							addVertex(newForwards.get(newVertex.weight), newVertex);
						} else {
							if (oldForwards.get(newVertex.weight) == null) {
								oldForwards.put(newVertex.weight, new TreeSet<Vertex>());
							}
							addVertex(oldForwards.get(newVertex.weight), newVertex);
						}
					}
				}
				
			}
			// перекидываем oldForwards в forwards, повторим для них цикл.
			for (int wi : oldForwards.keySet()) {
				if (forwards.get(wi) == null) {
					forwards.put(wi, oldForwards.get(wi));
				} else {
					for (Vertex vertex : oldForwards.get(wi)) {
						addVertex(forwards.get(wi), vertex);
					}
				}
			}
		}
		
		logger.debug("newForwards: "  + newForwards);
		return newForwards;
	}

	private static TreeMap<Integer,TreeSet<Vertex>> countBackwardPath(TreeMap<Integer,TreeSet<Vertex>> backwards, int weight, int metric) {
		logger.debug("backward = " + backwards);
		
		/**
		 * Сюда пойдут все вершины веса <= weight, имеющие соседа > weight
		 */
		TreeMap<Integer,TreeSet<Vertex>> newBackwards = new TreeMap<Integer, TreeSet<Vertex>>();
		while (!backwards.isEmpty()) {
			/**
			 * Сюда будут идти все вершины веса <= weight с такими же соседями, пока мы от них наконец не дойдем до вершин с соседями большего веса. 
			 */
			TreeMap<Integer,TreeSet<Vertex>> oldBackwards= new TreeMap<Integer, TreeSet<Vertex>>();

			for (Map.Entry<Integer,TreeSet<Vertex>> entry = backwards.pollFirstEntry(); entry != null; entry = backwards.pollFirstEntry()) {
				if (entry.getKey() > weight) {
					// странно, выкидываем эти вершины
					continue;
				}
				
				TreeSet<Vertex> backward = entry.getValue();
				for (Iterator<Vertex> iterator = backward.iterator(); iterator.hasNext();) {
					Vertex vertex = iterator.next();
					iterator.remove();
					
					if (!vertex.iterator.hasBackward()) {
						continue;
					}
					// идем из вершины назад.
					for (int i = 0; i < vertex.iterator.getPredecessors().length; ++i) {
						ITrellisEdge edge = vertex.iterator.getPredecessors()[i];
						if (vertex.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
							// Запрещаем нулевой путь
							continue;
						}

						Vertex newVertex = new Vertex(vertex);
						newVertex.iterator.moveBackward(i);
						newVertex.weight += edge.metrics()[metric];

						if (newVertex.weight > weight) {
							if (newBackwards.get(vertex.weight) == null) {
								newBackwards.put(vertex.weight, new TreeSet<Vertex>());
							}
							addVertex(newBackwards.get(vertex.weight), vertex);
						} else {
							if (oldBackwards.get(newVertex.weight) == null) {
								oldBackwards.put(newVertex.weight, new TreeSet<Vertex>());
							}
							addVertex(oldBackwards.get(newVertex.weight), newVertex);
						}
					}
				}
				
			}
			// перекидываем oldBackwards в backwards, повторим для них цикл.
			for (int wi : oldBackwards.keySet()) {
				if (backwards.get(wi) == null) {
					backwards.put(wi, oldBackwards.get(wi));
				} else {
					for (Vertex vertex : oldBackwards.get(wi)) {
						addVertex(backwards.get(wi), vertex);
					}
				}
			}
		}
		
		logger.debug("newBackwards: "  + newBackwards);
		return newBackwards;
	}

	/**
	 * Продвигает по решетке вперед на минимальный шаг 
	 * @param tracker объект, который продвигается по решетке  
	 * @param picker 
	 * @param root
	 * @param metric
	 */
	public static void buildNextPaths(PathTracker tracker, BeastTraversalPicker picker, ITrellisIterator root, int metric) {
		ITrellisEdge edges[] = tracker.iterator().getAccessors();
		for (int i = 0; i < edges.length; ++i) {
			if (tracker.iterator().vertexIndex() == 0 && i == 0) {
				if (tracker.iterator() == root) {
					continue;
				}
				// проходим по всем вершинам нулевого цикла и сразу переходим из каждой из них по ненулевому пути.
				PathTracker zeroPathTracker = (PathTracker) tracker.clone();
				zeroPathTracker.moveForward(i, 0);
				
				while (zeroPathTracker.iterator().layer() != tracker.iterator().layer() && zeroPathTracker.iterator().hasForward()) {
					ITrellisEdge zeroEdges[] = zeroPathTracker.iterator().getAccessors();
					for (int edgeIndex = 1; edgeIndex < zeroEdges.length; ++edgeIndex) {
						PathTracker newTracker = (PathTracker) zeroPathTracker.clone();
						newTracker.moveForward(edgeIndex, zeroEdges[edgeIndex].metrics()[metric]);

						picker.pickForward(newTracker);
					}
					zeroPathTracker.moveForward(0, 0);
				}
				continue;
			}

			PathTracker newTracker = (PathTracker) tracker.clone();				// копируем путь
			newTracker.moveForward(i, edges[i].metrics()[metric]);					// двигаемся вперед

			picker.pickForward(newTracker);
		}
	}
	
	
	/**
	 * Продвигает по решетке назад на минимальный шаг 
	 * @param tracker объект, который продвигается по решетке  
	 * @param picker 
	 * @param root
	 * @param metric
	 */
	public static void buildPrevPaths(PathTracker tracker, BeastTraversalPicker picker, double tresholdBackward, int metric) {
		ITrellisEdge edges[] = tracker.iterator().getPredecessors();
		for (int i = 0; i < edges.length; ++i) {
			if (tracker.iterator().vertexIndex() == 0 && i == 0) {
				continue;
			}
			
			PathTracker newTracker = (PathTracker) tracker.clone();		// копируем путь
			newTracker.moveBackward(i, edges[i].metrics()[metric]);		// двигаемся назад

			picker.pickBackward(newTracker);
			
			if (newTracker.weight() <= tresholdBackward) {
				if (newTracker.iterator().vertexIndex() == 0 && newTracker.iterator().hasBackward()) {
					// пришли в вершину нулевого цикла, пройдемся по нему.
					PathTracker zeroPathTracker = (PathTracker) newTracker.clone();
					zeroPathTracker.moveBackward(0, 0);
					
					while (zeroPathTracker.iterator().layer() != newTracker.iterator().layer()) {
						picker.pickBackward((PathTracker) zeroPathTracker.clone());
						if (zeroPathTracker.iterator().hasBackward()) {
							zeroPathTracker.moveBackward(0, 0);
						} else {
							break;
						}
					}
				}
			}
		}
	}
	
	private static TreeSet<PathTracker> findForwardFront(TreeSet<PathTracker> Forward,
			int tresholdForward, ITrellisIterator root, int metric) {
		logger.debug("tresholdForward = " + tresholdForward);
		
		TreeSet<PathTracker> newForward = new TreeSet<PathTracker>();
		
		while (!Forward.isEmpty()) {
			TreeSet<PathTracker> oldForward = new TreeSet<PathTracker>();
			
			for (Iterator<PathTracker> iterator = Forward.iterator(); iterator.hasNext();) {
				Vertex vertex = (Vertex) iterator.next();
				iterator.remove();
				
				if (vertex.weight >= tresholdForward) {
					addTheLeastPath(newForward, vertex);
					continue;
				}
				if (!vertex.iterator.hasForward()) {
					continue;
				}
				// вес пути path < tresholdForward
				for (int i = 0; i < vertex.iterator.getAccessors().length; ++i) {
					ITrellisEdge edge = vertex.iterator.getAccessors()[i];
					if (vertex.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
						if (vertex.iterator == root) {
							continue;
						}
						// проходим по всем вершинам нулевого цикла и сразу переходим из каждой из них по ненулевому пути.
						logger.debug("i = " + i);
						Vertex zeroPath = new Vertex(vertex);
						zeroPath.iterator.moveForward(i);
						
						while (zeroPath.iterator.layer() != vertex.iterator.layer() && zeroPath.iterator.hasForward()) {
							ITrellisEdge edges[] = zeroPath.iterator.getAccessors();
							for (int e = 1; e < edges.length; ++e) {
								Vertex nextVertex = new Vertex(zeroPath);
								nextVertex.iterator.moveForward(e);
								nextVertex.weight += edges[e].metrics()[metric];
								if (nextVertex.weight >= tresholdForward) {
									addTheLeastPath(newForward, nextVertex);
								} else {
									addTheLeastPath(oldForward, nextVertex);
								}
							}
							zeroPath.iterator.moveForward(0);
						}
						continue;
					}

					Vertex nextVertex = new Vertex(vertex);				// копируем путь
					nextVertex.iterator.moveForward(i);					// двигаемся вперед
					nextVertex.weight += edge.metrics()[metric];
										
					if (nextVertex.weight >= tresholdForward) {
						addTheLeastPath(newForward, nextVertex);
					} else {
						addTheLeastPath(oldForward, nextVertex);
					}
				}
			}
			
			Forward.addAll(oldForward);
		}
		
		logger.debug("newForward: "  + newForward);
		return newForward;
	}

	private static TreeSet<PathTracker> findBackwardPaths(TreeSet<PathTracker> Backward,
			int tresholdBackward, int metric) {
		logger.debug("tresholdBackward = " + tresholdBackward);

		TreeSet<PathTracker> newBackward = new TreeSet<PathTracker>();
		
		while (!Backward.isEmpty()) {
			TreeSet<PathTracker> oldBackward = new TreeSet<PathTracker>();
			
			for (Iterator<PathTracker> iterator = Backward.iterator(); iterator.hasNext();) {
				Vertex path = (Vertex) iterator.next();
				iterator.remove();
				
				if (!path.iterator.hasBackward()) {
					continue;
				}
				
				for (int i = 0; i < path.iterator.getPredecessors().length; ++i) {
					ITrellisEdge edge = path.iterator.getPredecessors()[i];
					if (path.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
						continue;
					}
					
					Vertex nextVertex = new Vertex(path);				// копируем путь
					nextVertex.iterator.moveBackward(i);				// двигаемся назад
					nextVertex.weight += edge.metrics()[metric];
					
					if (nextVertex.weight > tresholdBackward) {
						addTheLeastPath(newBackward, path);
					} else {
						addTheLeastPath(oldBackward, nextVertex);
						
						if (nextVertex.iterator.vertexIndex() == 0 && nextVertex.iterator.hasBackward()) {
							// пришли в вершину нулевого цикла, пройдемся по нему.
							Vertex zeroPath = new Vertex(nextVertex);
							zeroPath.iterator.moveBackward(0);
							
							while (zeroPath.iterator.layer() != nextVertex.iterator.layer()) {
								addTheLeastPath(oldBackward, new Vertex(zeroPath));
								if (zeroPath.iterator.hasBackward()) {
									zeroPath.iterator.moveBackward(0);
								} else {
									break;
								}
							}
						}
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
	private static void addTheLeastPath(TreeSet<PathTracker> trackers, PathTracker tracker) {
		if (trackers.contains(tracker)) {
			PathTracker tracker2 = trackers.floor(tracker);
			if (tracker.equals(tracker2) && tracker2.weight() > tracker.weight()) {
				// существующий путь с большей метрикой - удаляем его
				trackers.remove(tracker2);
			}
		}
		
		trackers.add(tracker); // если в pathes был путь с меньшей метрикой, то path не добавится.
	}

	private static void addVertex(TreeSet<Vertex> vertices, Vertex vertex) {
		if (vertices.contains(vertex)) {
			Vertex vertex2 = vertices.floor(vertex);
			if (vertex2 == vertex) {
				return ;
			}
			// уже пришли в эту вершину, добавляем к ней кол-во путей.
			vertex2.pathNumber += vertex.pathNumber;
		} else {
			vertices.add(vertex); // пришли в новую вершину, добавляем
		}
	}
}
