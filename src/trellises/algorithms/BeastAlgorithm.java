package trellises.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.ITrellisEdge;
import trellises.ITrellisIterator;

/**
 * 
 * @author stas
 *
 */
public class BeastAlgorithm {
	private static Logger logger = LoggerFactory.getLogger(BeastAlgorithm.class);

	static public int comparePathTrackers(PathTracker<?> a, PathTracker<?> b) {
		if (a.layer() == b.layer()) {
			return (int) (a.vertexIndex() - b.vertexIndex());
		}
		return a.layer() - b.layer();
	}

	static public <T extends PathTracker<T>> Front<T> getBestFront() {
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
	 */
	public static int countMinDist(final ITrellisIterator root, final ITrellisIterator toor, int metric, int upperBound) {
		final PathWeightCounter rootPath = new PathWeightCounter(root, metric);
		final PathWeightCounter toorPath = new PathWeightCounter(toor, metric);

		MinWeightCounter<PathWeightCounter> trackProcessor = new MinWeightCounter<PathWeightCounter>(); 
		findOptimalTrack(rootPath, toorPath, upperBound, trackProcessor);
		int minDist = trackProcessor.getMinWeight();
		int minLayer = trackProcessor.getMinLayer();
		long minVertexIndex = trackProcessor.getMinVertexIndex();
		
		logger.debug("dist = " + minDist + ", layer = " + minLayer + ", vertexIndex = " + minVertexIndex);

		return minDist;
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
	public static ArrayList<TrellisPath> findOptimalPaths(final ITrellisIterator root, final ITrellisIterator toor, int metric, int upperBound) {
		final PathPicker rootPath = new PathPicker(root, metric);
		final PathPicker toorPath = new PathPicker(toor, metric);

		MinPathPicker trackProcessor = new MinPathPicker();
		
		findOptimalTrack(rootPath, toorPath, upperBound, trackProcessor);

		return trackProcessor.getPaths();
	}

	/**
	/**
	 * Алгоритм BEAST для поиска минимального/свободного расстояния кода в решетке.
	 * 
	 * Метод ищет произвольный путь(трек) минимального веса в заданной метрике между вершинами 
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
	 * @param upperBound верхняя оценка на минимальный вес, может быть 
	 * сколь угодно большой, используется лишь для контроля на случай возможной ошбики в коде.
	 * @param trackProcessor стратегия обработки найденных путей (треков) 
	 * @return произвольный один из кратчайших ненулевых путей между вершинами root и toor
	 */
	public static <T extends PathTracker<T>> void findOptimalTrack(final T root, final T toor, final int upperBound, final TrackProcessor<T> trackProcessor) {
		Front<T> Forward = getBestFront(), Backward = getBestFront();
		int tresholdForward = 0, tresholdBackward = 0;
	
		Forward.add(root);

		Backward.add(toor);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			T zeroPath = toor.backwardIterator().next();
			while (zeroPath.layer() != toor.layer()) {
				Backward.add(zeroPath);
				if (zeroPath.hasBackward()) {
					zeroPath = zeroPath.backwardIterator().next();
				} else {
					break;
				}
			}
		}
		
		boolean trackFound = false;
		while (tresholdForward + tresholdBackward <= upperBound && !trackFound) {
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				Forward = findForwardFront(Forward, tresholdForward, root);
			} else {
				++tresholdBackward;
				Backward = findBackwardFront(Backward, tresholdBackward);
			}

			logger.debug("Forward size = {}, Backward size = {}", Forward.size(), Backward.size());
			
			for (T fpath : Forward) {
				T bpath = Backward.get(fpath.layer(), fpath.vertexIndex());
				if (bpath != null) {
					if (fpath.weight() == 0 && bpath.weight() == 0) {
						// нулевой путь, пропускаем
						continue;
					}

					trackProcessor.process(fpath, bpath);
					trackFound = true;
				}
			}
		}
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
	
	private static <T extends PathTracker<T>> Front<T> findForwardFront(Front<T> Forward,
			final int tresholdForward, final PathTracker<T> root) {
		logger.debug("tresholdForward = {}", tresholdForward);
		
		Front<T> newForward = getBestFront();
		
		while (!Forward.isEmpty()) {
			final Front<T> oldForward = getBestFront();
			
			for (Iterator<T> iterator = Forward.iterator(); iterator.hasNext();) {
				final T vertex = iterator.next();
				iterator.remove();
				
				if (vertex.weight() >= tresholdForward) {
					addTheLeastPath(newForward, vertex);
					continue;
				}
				
				if (!vertex.hasForward()) {
					continue;
				}
				
				// вес пути path < tresholdForward
				Iterator<T> iter = vertex.forwardIterator(); 
				if (vertex.vertexIndex() == 0 && vertex != root) {
					T nextVertex = iter.next();
					if (nextVertex.weight() == vertex.weight()) { // переход по нулевому ребру из нулевой вершины яруса - нулевой цикл
						// проходим по всем вершинам нулевого цикла и сразу переходим из каждой из них по ненулевому пути.					
						T zeroPath = nextVertex;
						
						while (zeroPath.layer() != vertex.layer() && zeroPath.hasForward()) {
							Iterator<T> iter2 = zeroPath.forwardIterator();
							T nextZeroPath = iter2.next();
							while (iter2.hasNext()) {
								T nonZeroVertex = iter2.next();
								addToForwardFrontByTreshold(nonZeroVertex, tresholdForward, newForward, oldForward);
							}
							zeroPath = nextZeroPath;
						}
					} else {
						addToForwardFrontByTreshold(nextVertex, tresholdForward, newForward, oldForward);
					}
				}
				
				while (iter.hasNext()) {
					T nextVertex = iter.next();
										
					addToForwardFrontByTreshold(nextVertex, tresholdForward, newForward, oldForward);
				}
			}
			
			Forward.addAll(oldForward);
		}
		
		logger.debug("newForward: size = {}, {}", newForward.size(), newForward);
		return newForward;
	}

	/**
	 * @param vertex
	 * @param tresholdForward
	 * @param newForward
	 * @param oldForward
	 */
	private static <T extends PathTracker<T>> void addToForwardFrontByTreshold(final T vertex,
			final int tresholdForward, final Front<T> newForward, final Front<T> oldForward) {
		if (vertex.weight() >= tresholdForward) {
			addTheLeastPath(newForward, vertex);
		} else {
			addTheLeastPath(oldForward, vertex);
		}
	}

	private static <T extends PathTracker<T>> Front<T> findBackwardFront(Front<T> Backward,
			final int tresholdBackward) {
		logger.debug("tresholdBackward = {}", tresholdBackward);

		Front<T> newBackward = getBestFront();
		
		while (!Backward.isEmpty()) {
			final Front<T> oldBackward = getBestFront();
			
			for (Iterator<T> iterator = Backward.iterator(); iterator.hasNext();) {
				final T vertex = iterator.next();
				iterator.remove();
				
				if (!vertex.hasBackward()) {
					continue;
				}
				
				Iterator<T> iter = vertex.backwardIterator();
				if (vertex.vertexIndex() == 0) {
					T nextVertex = iter.next();
					if (nextVertex.weight() > vertex.weight()) {
						addToBackwardByTreshold(tresholdBackward, newBackward,
								oldBackward, vertex, nextVertex);
					}
				}
				while (iter.hasNext()) {
					T nextVertex = iter.next();
					
					addToBackwardByTreshold(tresholdBackward, newBackward,
							oldBackward, vertex, nextVertex);
				}
			}
			
			Backward.addAll(oldBackward);
		}
		
		logger.debug("newBackward: size = {}, {}",  newBackward.size(), newBackward);
		return newBackward;
	}

	/**
	 * @param tresholdBackward
	 * @param newBackward
	 * @param oldBackward
	 * @param vertex
	 * @param nextVertex
	 */
	public static <T extends PathTracker<T>> void addToBackwardByTreshold(final int tresholdBackward,
			final Front<T> newBackward, final Front<T> oldBackward, final T vertex, final T nextVertex) {
		if (nextVertex.weight() > tresholdBackward) {
			addTheLeastPath(newBackward, vertex);
		} else {
			addTheLeastPath(oldBackward, nextVertex);
			
			if (nextVertex.vertexIndex() == 0 && nextVertex.hasBackward()) {
				// пришли в вершину нулевого цикла, пройдемся по нему.
				T zeroPath = nextVertex.backwardIterator().next();
				
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

	/**
	 * Метод добавляет <code>path</code> в множество <code>pathes</code>, если в нем не содержится путь, эквивалентный <code>path</code>,
	 * с меньшей метрикой. Более строго, <code>path</code> не добавляется тогда, когда в <code>pathes</code> уже есть путь <code>path2</code>
	 * такой, что <code>path2.equals(path) && path2.metric <= path.metric</code>.  
	 * @param pathes множество путей
	 * @param path добавляемый путь
	 */
	private static <T extends PathTracker<T>> void addTheLeastPath(final Front<T> trackers, final T tracker) {
		T tracker2 = trackers.get(tracker.layer(), tracker.vertexIndex());
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
