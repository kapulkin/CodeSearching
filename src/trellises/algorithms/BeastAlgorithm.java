package trellises.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;
import trellises.algorithms.TrellisPath.PathIterator;

/**
 * 
 * @author stas
 *
 */
public class BeastAlgorithm {
	private static Logger logger = LoggerFactory.getLogger(BeastAlgorithm.class);

	/**
	 * Стратегия продвижения по вершинам при обходе в BEAST 
	 * @author Stas
	 *
	 */
	public static interface BeastPropagationStrategy {
		/**
		 * Метод возвращает вершины, полученные продвижением из входной <code>pathTracker</code> при проходе вперед.
		 * @param pathTracker вершина, из которой выполняется продвижение.
		 * @return вершины, полученные продвижением из входной <code>pathTracker</code>.
		 */
		Iterable<PathTracker> propagateForward(PathTracker pathTracker);
		/**
		 * Метод возврщает вершины, полученные продвижением из входной <code>pathTracker</code> при проходе назад.
		 * @param pathTracker вершина, из которой выполняется продвижение.
		 * @return вершины, полученные продвижением из входной <code>pathTracker</code>.
		 */
		Iterable<PathTracker> propagateBackward(PathTracker pathTracker);
	}
	
	/**
	 * Универсальная стратегия продвижения вершин, которая подходит для блокового, сверточного и усеченного кода.
	 * @author Stas
	 *
	 */
	public static class UniversalPropagationStrategy implements BeastPropagationStrategy {
		private PathTracker root;
		private double tresholdBackward;
		
		public UniversalPropagationStrategy(PathTracker root, int metric, double tresholdBackward) {
			this.root = root;
			this.tresholdBackward = tresholdBackward;
		}
		
		@Override
		public Iterable<PathTracker> propagateForward(PathTracker tracker) {
			if (!tracker.hasForward()) {
				return new ArrayList<PathTracker>();
			}
			
			Collection<PathTracker> trackers = new ArrayList<PathTracker>(2);
			
			Iterator<PathTracker> forwardTrackers = tracker.forwardIterator();

			if (forwardTrackers.hasNext()) {
				if (tracker.vertexIndex() == 0 && tracker != root) {
					// tracker - вершина нулевого пути, обрабатываем переход по нулевому ребру отдельно
					PathTracker zeroPathTracker = forwardTrackers.next();	// переход по нулевому ребру
					
					Iterator<PathTracker> zeroPathForward = zeroPathTracker.forwardIterator();
					while (zeroPathTracker.layer() != tracker.layer() && zeroPathForward.hasNext()) {
						zeroPathTracker = zeroPathForward.next();			// переход по нулевому ребру

						for (; zeroPathForward.hasNext(); ) {
							trackers.add(zeroPathForward.next());			// переход по ненулевым ребрам
						}
						
						zeroPathForward = zeroPathTracker.forwardIterator();
					}
				}
			}
			
			for (; forwardTrackers.hasNext(); ) {
				trackers.add(forwardTrackers.next());
			}
			
			return trackers;
		}

		@Override
		public Iterable<PathTracker> propagateBackward(PathTracker tracker) {
			Collection<PathTracker> trackers = new ArrayList<PathTracker>(2);

			Iterator<PathTracker> backwardTrackers = tracker.backwardIterator();
			
			if (backwardTrackers.hasNext()) {
				if (tracker.vertexIndex() == 0) {
					backwardTrackers.next();		// пропускаем переход по нулевому ребру
				}
				
				for (; backwardTrackers.hasNext(); ) {
					PathTracker newTracker = backwardTrackers.next();
					trackers.add(newTracker);
					
					if (newTracker.weight() <= tresholdBackward && newTracker.vertexIndex() == 0) {
						// пришли в вершину нулевого цикла, пройдемся по нему.
						
						Iterator<PathTracker> zeroPathBackward = newTracker.backwardIterator();
						
						while (zeroPathBackward.hasNext()) {
							PathTracker zeroPathTracker = zeroPathBackward.next();
							if (zeroPathTracker.layer() == newTracker.layer()) {
								break;
							}
							trackers.add(zeroPathTracker);
							zeroPathBackward = zeroPathTracker.backwardIterator();
						}
					}
				}
			}
			
			return trackers;
		}
	}
	
	public static class SimpleFrontBuilder {
		private Logger logger2 = LoggerFactory.getLogger(this.getClass());

		private Front<PathTracker> Forward, Backward;
		double tresholdForward, tresholdBackward;
		int metric;

		PathTracker root;
		PathTracker toor;
		
		public SimpleFrontBuilder(Front<PathTracker> Forward, Front<PathTracker> Backward,
				PathTracker root, PathTracker toor, int metric) {
			this.Forward = Forward;
			this.Backward = Backward;
			this.metric = metric;
			this.root = root;
			this.toor = toor;
		}
		
		Front<PathTracker> getForwardFront() {
			return Forward;
		}
		
		Front<PathTracker> getBackwardFront() {
			return Backward;
		}

		public void builtForwardFront(int tresholdForward, PathTracker root) {
			this.tresholdForward = tresholdForward;
			logger2.debug("tresholdForward = " + tresholdForward);
			
			Front<PathTracker> newForward = new ArrayLayeredFront<PathTracker>();

			BeastPropagationStrategy strategy = new UniversalPropagationStrategy(root, metric, tresholdBackward);

			while (!Forward.isEmpty()) {
				Front<PathTracker> oldForward = new ArrayLayeredFront<PathTracker>();
				
				for (Iterator<PathTracker> iterator = Forward.iterator(); iterator.hasNext();) {
					PathTracker tracker = iterator.next();
					iterator.remove();
					
					if (tracker.weight() >= tresholdForward) {
						addTheLeastPath(newForward, tracker);
						continue;
					}
					if (!tracker.hasForward()) {
						continue;
					}

					Iterable<PathTracker> trackers = strategy.propagateForward(tracker);
					for (PathTracker newTracker : trackers) {
						if (newTracker.weight() >= tresholdForward) {
							addTheLeastPath(newForward, newTracker);
						} else {
							addTheLeastPath(oldForward, newTracker);
						}
					}
				}
				
				Forward.addAll(oldForward);
			}
			
			logger2.debug("newForward: "  + newForward);
			Forward = newForward;
		}

		public void builtBackwardFront(int tresholdBackward) {
			this.tresholdBackward = tresholdBackward;
			logger2.debug("tresholdBackward = " + tresholdBackward);
			
			Front<PathTracker> newBackward = new ArrayLayeredFront<PathTracker>();

			BeastPropagationStrategy strategy = new UniversalPropagationStrategy(root, metric, tresholdBackward);

			while (!Backward.isEmpty()) {
				Front<PathTracker> oldBackward = new ArrayLayeredFront<PathTracker>();
				
				for (Iterator<PathTracker> iterator = Backward.iterator(); iterator.hasNext();) {
					PathTracker tracker = iterator.next();
					iterator.remove();
					
					if (!tracker.hasBackward()) {
						continue;
					}
			
					Iterable<PathTracker> trackers = strategy.propagateBackward(tracker);
					for (PathTracker newTracker : trackers) {
						if (newTracker.weight() > tresholdForward) {
							addTheLeastPath(newBackward, tracker);
						} else {
							addTheLeastPath(oldBackward, newTracker);
						}
					}
				}
				
				Backward.addAll(oldBackward);
			}
			
			logger2.debug("newBackward: "  + newBackward);
			Backward = newBackward;
		}
	}
	
	static public int comparePathTrackers(PathTracker a, PathTracker b) {
		if (a.layer() == b.layer()) {
			return (int) (a.vertexIndex() - b.vertexIndex());
		}
		return a.layer() - b.layer();
	}

	static public <T extends PathTracker> Front<T> getBestFront() {
		return new ArrayLayeredFront<T>();
//		return new HashMapLayeredFront<T>();
//		return new TreeSetFront<T>();
	}
	
	/**
	 * Алгоритм BEAST для поиска минимального/свободного расстояния кода в решетке.
	 * 
	 * Метод ищет кратчайшие ненулевые пути в заданной метрике между вершинами 
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
		Front<PathTracker> Forward = getBestFront(), Backward = getBestFront();
 		int tresholdForward = 0, tresholdBackward = 0;
	
		PathPicker rootPath = new PathPicker(root, metric);
		Forward.add(rootPath);

		PathPicker toorPath = new PathPicker(toor, metric);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			PathPicker zeroPath = toorPath.clone();
			zeroPath.iterator.moveBackward(0);
			zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
			while (zeroPath.iterator.layer() != toor.layer() && zeroPath.iterator.hasBackward()) {
				zeroPath.iterator.moveBackward(0);
				zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
				Backward.add((PathTracker) zeroPath.clone());
			}
		}

		SimpleFrontBuilder frontBuilder = new SimpleFrontBuilder(Forward, Backward, rootPath, toorPath, metric);
		
		ArrayList<TrellisPath> paths = new ArrayList<TrellisPath>();
		
		while (tresholdForward + tresholdBackward <= upperBound) {
			
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				frontBuilder.builtForwardFront(tresholdForward, rootPath);
//TODO:				Forward = findForwardPaths(Forward, tresholdForward, root, metric);
			} else {
				++tresholdBackward;
				frontBuilder.builtBackwardFront(tresholdBackward);
//TODO:				Backward = findBackwardPaths(Backward, tresholdBackward, metric);
			}

			Forward = frontBuilder.getForwardFront();
			Backward = frontBuilder.getBackwardFront();
			
			for (PathTracker tracker : Forward) {
				if (Backward.contains(tracker)) {
					PathPicker fpath = (PathPicker) tracker;
					PathPicker bpath = (PathPicker) Backward.get(fpath.layer(), fpath.vertexIndex());
					
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
	}
	
	/**
	 * Алгоритм BEAST для поиска минимального/свободного расстояния кода в решетке.
	 * 
	 * Метод ищет кратчайшие ненулевые пути в заданной метрике между вершинами 
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
		Front<PathTracker> Forward = getBestFront(), Backward = getBestFront();
		int tresholdForward = 0, tresholdBackward = 0;
	
		PathCounter rootPath = new PathCounter(root, metric);
		Forward.add(rootPath);

		PathCounter toorPath = new PathCounter(toor, metric);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			PathCounter zeroPath = new PathCounter(toorPath);
			zeroPath.iterator.moveBackward(0);
			while (zeroPath.iterator.layer() != toor.layer()) {
				Backward.add(new PathCounter(zeroPath));
				if (zeroPath.iterator.hasBackward()) {
					zeroPath.iterator.moveBackward(0);
				} else {
					break;
				}
			}
		}
		
		int minDist = -1;
		int minLayer = -1;
		long minVertexIndex = -1;
		
		while (tresholdForward + tresholdBackward <= upperBound) {
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				Forward = findForwardFront(Forward, tresholdForward, rootPath, metric);
			} else {
				++tresholdBackward;
				Backward = findBackwardFront(Backward, tresholdBackward, metric);
			}

			logger.debug("Forward size = {}, Backward size = {}", Forward.size(), Backward.size());
			
			for (PathTracker fpath : Forward) {
				PathTracker bpath = Backward.get(fpath.layer(), fpath.vertexIndex());
				if (bpath != null) {
					if (fpath.weight() == 0 && bpath.weight() == 0) {
						// нулевой путь, пропускаем
						continue;
					}
		
					if (minDist > fpath.weight() + bpath.weight() || minDist < 0) {
						minDist = (int) (fpath.weight() + bpath.weight());
						minLayer = fpath.layer();
						minVertexIndex = fpath.vertexIndex();
					}
				}
			}
			
			if (minDist > 0) {
				break;
			}
		}

		logger.debug("dist = " + minDist + ", layer = " + minLayer + ", vertexIndex = " + minVertexIndex);
		
		return minDist;
	}
	
	public static long[] findSpectrum(ITrellisIterator root, ITrellisIterator toor, int maxWeight, int metric) {
		TreeMap<Integer, TreeSet<PathCounter>> forwards = new TreeMap<Integer, TreeSet<PathCounter>>();
		TreeMap<Integer, TreeSet<PathCounter>> backwards = new TreeMap<Integer, TreeSet<PathCounter>>();
		
		PathCounter rootVertex = new PathCounter(root, metric);
		forwards.put(0, new TreeSet<PathCounter>());
		forwards.get(0).add(rootVertex);
		
		PathCounter toorVertex = new PathCounter(toor, metric);
		backwards.put(0, new TreeSet<PathCounter>());
		backwards.get(0).add(toorVertex);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			PathCounter zeroVertex = new PathCounter(toorVertex);
			do {
				zeroVertex.iterator.moveBackward(0);
				backwards.get(0).add(new PathCounter(zeroVertex));
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
				TreeSet<PathCounter> forward = forwards.get(tresholdForward + wi);
				TreeSet<PathCounter> backward = backwards.get(tresholdBackward - wi);
	
				if (forward == null || backward == null) {
					continue;
				}
				
				for (PathCounter fvertex : forward) {
					if (backward.contains(fvertex)) {
						PathCounter bvertex = backward.floor(fvertex);
	
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
	
	private static TreeMap<Integer,TreeSet<PathCounter>> countForwardPath(TreeMap<Integer,TreeSet<PathCounter>> forwards, int weight, int metric) {
		logger.debug("forward = " + forwards);
		
		/**
		 * Сюда пойдут все вершины веса >= weight
		 */
		TreeMap<Integer,TreeSet<PathCounter>> newForwards = new TreeMap<Integer, TreeSet<PathCounter>>();
		while (!forwards.isEmpty()) {
			/**
			 * Сюда будут идти все вершины веса < weight, пока мы от них наконец не дойдем до вершин большего веса. 
			 */
			TreeMap<Integer,TreeSet<PathCounter>> oldForwards = new TreeMap<Integer, TreeSet<PathCounter>>();

			for (Map.Entry<Integer,TreeSet<PathCounter>> entry = forwards.pollFirstEntry(); entry != null; entry = forwards.pollFirstEntry()) {
				if (entry.getKey() >= weight) {
					// быстро перекидываем все вершины веса >= weight в newForwards
					if (newForwards.get(entry.getKey()) == null) {
						newForwards.put(entry.getKey(), entry.getValue());
					} else {
						for (PathCounter vertex : entry.getValue()) {
							addVertex(newForwards.get(entry.getKey()), vertex);
						}
					}
					continue;
				}
				
				TreeSet<PathCounter> forward = entry.getValue();
				for (Iterator<PathCounter> iterator = forward.iterator(); iterator.hasNext();) {
					PathCounter vertex = iterator.next();
					iterator.remove();
					
					if (!vertex.iterator.hasForward()) {
						continue;
					}
					// идем из вершины вперед.
					for (int i = 0; i < vertex.iterator.getAccessors().length; ++i) {
						ITrellisEdge edge = vertex.iterator.getAccessors()[i];
						if (vertex.iterator.vertexIndex() == 0 && edge.metric(metric) == 0) {
							// Запрещаем нулевой путь
							continue;
						}

						PathCounter newVertex = new PathCounter(vertex);
						newVertex.iterator.moveForward(i);
						newVertex.weight += edge.metric(metric);

						if (newVertex.weight >= weight) {
							if (newForwards.get(newVertex.weight) == null) {
								newForwards.put(newVertex.weight, new TreeSet<PathCounter>());
							}
							addVertex(newForwards.get(newVertex.weight), newVertex);
						} else {
							if (oldForwards.get(newVertex.weight) == null) {
								oldForwards.put(newVertex.weight, new TreeSet<PathCounter>());
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
					for (PathCounter vertex : oldForwards.get(wi)) {
						addVertex(forwards.get(wi), vertex);
					}
				}
			}
		}
		
		logger.debug("newForwards: "  + newForwards);
		return newForwards;
	}

	private static TreeMap<Integer,TreeSet<PathCounter>> countBackwardPath(TreeMap<Integer,TreeSet<PathCounter>> backwards, int weight, int metric) {
		logger.debug("backward = " + backwards);
		
		/**
		 * Сюда пойдут все вершины веса <= weight, имеющие соседа > weight
		 */
		TreeMap<Integer,TreeSet<PathCounter>> newBackwards = new TreeMap<Integer, TreeSet<PathCounter>>();
		while (!backwards.isEmpty()) {
			/**
			 * Сюда будут идти все вершины веса <= weight с такими же соседями, пока мы от них наконец не дойдем до вершин с соседями большего веса. 
			 */
			TreeMap<Integer,TreeSet<PathCounter>> oldBackwards= new TreeMap<Integer, TreeSet<PathCounter>>();

			for (Map.Entry<Integer,TreeSet<PathCounter>> entry = backwards.pollFirstEntry(); entry != null; entry = backwards.pollFirstEntry()) {
				if (entry.getKey() > weight) {
					// странно, выкидываем эти вершины
					continue;
				}
				
				TreeSet<PathCounter> backward = entry.getValue();
				for (Iterator<PathCounter> iterator = backward.iterator(); iterator.hasNext();) {
					PathCounter vertex = iterator.next();
					iterator.remove();
					
					if (!vertex.iterator.hasBackward()) {
						continue;
					}
					// идем из вершины назад.
					for (int i = 0; i < vertex.iterator.getPredecessors().length; ++i) {
						ITrellisEdge edge = vertex.iterator.getPredecessors()[i];
						if (vertex.iterator.vertexIndex() == 0 && edge.metric(metric) == 0) {
							// Запрещаем нулевой путь
							continue;
						}

						PathCounter newVertex = new PathCounter(vertex);
						newVertex.iterator.moveBackward(i);
						newVertex.weight += edge.metric(metric);

						if (newVertex.weight > weight) {
							if (newBackwards.get(vertex.weight) == null) {
								newBackwards.put(vertex.weight, new TreeSet<PathCounter>());
							}
							addVertex(newBackwards.get(vertex.weight), vertex);
						} else {
							if (oldBackwards.get(newVertex.weight) == null) {
								oldBackwards.put(newVertex.weight, new TreeSet<PathCounter>());
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
					for (PathCounter vertex : oldBackwards.get(wi)) {
						addVertex(backwards.get(wi), vertex);
					}
				}
			}
		}
		
		logger.debug("newBackwards: {}",  newBackwards);
		return newBackwards;
	}
	
	/*
	 * TODO: проверить и устранить потенциальную багу в BEAST:
	 * пусть мы пришли в вершину, вес пройденного пути >= tresholdForward, добавляем вершину в newForward.
	 * теперь если мы придем в эту же вершину по пути меньшего веса, то вершина добавится в oldForward, и по-прежнему останется в newForward.
	 * сейчас это не проблема, т.к. в этом случае мы обязательно пойдем из вершины дальше.
	 * 
	 * Проверить, верно ли, что мы точно не попадем в эту вершину при обратном обходе.
	 * 
	 * Альтернатива: сначала все вершины помещать в oldForward. Затем перемещать подходящие в newForward 
	 */
	
	private static Front<PathTracker> findForwardFront(Front<PathTracker> Forward,
			int tresholdForward, PathCounter root, int metric) {
		logger.debug("tresholdForward = {}", tresholdForward);
		
		Front<PathTracker> newForward = getBestFront();
		
		while (!Forward.isEmpty()) {
			Front<PathTracker> oldForward = getBestFront();
			
			for (Iterator<PathTracker> iterator = Forward.iterator(); iterator.hasNext();) {
				PathCounter vertex = (PathCounter) iterator.next();
				iterator.remove();
				
				if (vertex.weight >= tresholdForward) {
					addTheLeastPath(newForward, vertex);
					continue;
				}
				
				if (!vertex.hasForward()) {
					continue;
				}
				
				// вес пути path < tresholdForward
				int i = 0;
				ITrellisEdge edges[] = vertex.iterator.getAccessors();
				if (vertex.vertexIndex() == 0 && vertex != root && edges[0].metric(metric) == 0) {
					// проходим по всем вершинам нулевого цикла и сразу переходим из каждой из них по ненулевому пути.					
//					assert(edges[0].metric(metric) == 0);

					PathTracker zeroPath = vertex.forwardIterator().next();
					
					while (zeroPath.layer() != vertex.layer() && zeroPath.hasForward()) {
						Iterator<PathTracker> iter = zeroPath.forwardIterator();
						PathTracker nextZeroPath = iter.next();
						while (iter.hasNext()) {
							PathTracker nextVertex = iter.next();
							if (nextVertex.weight() >= tresholdForward) {
								addTheLeastPath(newForward, nextVertex);
							} else {
								addTheLeastPath(oldForward, nextVertex);
							}
						}
						zeroPath = nextZeroPath;
					}
					++i;
				}
				
				for (; i < edges.length; ++i) {
					ITrellisEdge edge = edges[i];

					PathCounter nextVertex = new PathCounter(vertex);	// копируем путь
					nextVertex.iterator.moveForward(i);					// двигаемся вперед
					nextVertex.weight += edge.metric(metric);
										
					if (nextVertex.weight >= tresholdForward) {
						addTheLeastPath(newForward, nextVertex);
					} else {
						addTheLeastPath(oldForward, nextVertex);
					}
				}
			}
			
			Forward.addAll(oldForward);
		}
		
		logger.debug("newForward: size = {}, {}", newForward.size(), newForward);
		return newForward;
	}

	private static Front<PathTracker> findBackwardFront(Front<PathTracker> Backward,
			int tresholdBackward, int metric) {
		logger.debug("tresholdBackward = {}", tresholdBackward);

		Front<PathTracker> newBackward = getBestFront();
		
		while (!Backward.isEmpty()) {
			Front<PathTracker> oldBackward = getBestFront();
			
			for (Iterator<PathTracker> iterator = Backward.iterator(); iterator.hasNext();) {
				PathCounter vertex = (PathCounter) iterator.next();
				iterator.remove();
				
				if (!vertex.hasBackward()) {
					continue;
				}
				
				int i = 0;
				ITrellisEdge edges[] = vertex.iterator.getPredecessors();
				if (vertex.vertexIndex() == 0 && edges[0].metric(metric) == 0) {
//					assert(edges[0].metric(metric) == 0);
					++i;
				}
				for (; i < edges.length; ++i) {
					ITrellisEdge edge = edges[i];
					
					PathCounter nextVertex = new PathCounter(vertex);		// копируем путь
					nextVertex.iterator.moveBackward(i);					// двигаемся назад
					nextVertex.weight += edge.metric(metric);
					
					if (nextVertex.weight > tresholdBackward) {
						addTheLeastPath(newBackward, vertex);
					} else {
						addTheLeastPath(oldBackward, nextVertex);
						
						if (nextVertex.vertexIndex() == 0 && nextVertex.hasBackward()) {
							// пришли в вершину нулевого цикла, пройдемся по нему.
							PathTracker zeroPath = nextVertex.backwardIterator().next();
							
							while (zeroPath.layer() != nextVertex.layer()) {
								addTheLeastPath(oldBackward, zeroPath);
								if (zeroPath.hasBackward()) {
									zeroPath = zeroPath.backwardIterator().next();
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
		
		logger.debug("newBackward: size = {}, {}",  newBackward.size(), newBackward);
		return newBackward;
	}

	/**
	 * Метод добавляет <code>path</code> в множество <code>pathes</code>, если в нем не содержится путь, эквивалентный <code>path</code>,
	 * с меньшей метрикой. Более строго, <code>path</code> не добавляется тогда, когда в <code>pathes</code> уже есть путь <code>path2</code>
	 * такой, что <code>path2.equals(path) && path2.metric <= path.metric</code>.  
	 * @param pathes множество путей
	 * @param path добавляемый путь
	 */
	private static void addTheLeastPath(Front<PathTracker> trackers, PathTracker tracker) {
		PathTracker tracker2 = trackers.get(tracker.layer(), tracker.vertexIndex());
		if (tracker2 != null) {
			if (tracker2.weight() > tracker.weight()) {
				// существующий путь с большей метрикой - удаляем его
				trackers.remove(tracker2);
			}
		}
		
		trackers.add(tracker); // если в pathes был путь с меньшей метрикой, то path не добавится.
	}

	private static void addVertex(TreeSet<PathCounter> vertices, PathCounter vertex) {
		if (vertices.contains(vertex)) {
			PathCounter vertex2 = vertices.floor(vertex);
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
