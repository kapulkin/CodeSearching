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
		
	public static class PathCollector implements Comparable<PathCollector> {
		/**
		 * Итератор, указывающий на последнюю вершину в пути
		 */
		public ITrellisIterator iterator;
		/**
		 * Собираемый путь
		 */
		public TrellisPath path;
		
		public PathCollector() {
			iterator = null;
			path = null;
		}
		
		public PathCollector(ITrellisIterator iterator) {
			this.iterator = iterator;
			this.path = new TrellisPath(iterator.vertexIndex());
		}
		
		public PathCollector(final PathCollector collector) {
			iterator = collector.iterator.clone();
			path = new TrellisPath(collector.path);
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
				if (iterator.vertexIndex() == collector.iterator.vertexIndex()) {
					return 0;
				}
				return (iterator.vertexIndex() < collector.iterator.vertexIndex() ? -1 : 1);
			}
			return iterator.layer() - collector.iterator.layer();
		}
		
		@Override
		public String toString() {
			return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + path.weight() + ")";
		}
	}
	
	static class Vertex implements Comparable<Vertex> {
		ITrellisIterator iterator;
		int weight;
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
		public int compareTo(Vertex vertex) {
			if (iterator.layer() == vertex.iterator.layer()) {
				if (iterator.vertexIndex() == vertex.iterator.vertexIndex()) {
					return 0;
				}
				return (iterator.vertexIndex() < vertex.iterator.vertexIndex() ? -1 : 1);
			}
			return iterator.layer() - vertex.iterator.layer();
		}

		@Override
		public String toString() {
			return "(" + iterator.layer() + ", " + iterator.vertexIndex() + ", w: " + weight + ")";
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
	public static TrellisPath[] findOptimalPaths(ITrellisIterator root, ITrellisIterator toor, int metric, int upperBound) {
		TreeSet<PathCollector> Forward = new TreeSet<PathCollector>(), Backward = new TreeSet<PathCollector>();
		int tresholdForward = 0, tresholdBackward = 0;
	
		PathCollector rootPath = new BeastAlgorithm.PathCollector(root);
		Forward.add(rootPath);

		PathCollector toorPath = new BeastAlgorithm.PathCollector(toor);
		Backward.add(toorPath);

		if (toor.vertexIndex() == 0 && toor.hasBackward()) {
			// добавляем все вершины нулевого пути/цикла в решетке.
			PathCollector zeroPath = new PathCollector(toorPath);
			zeroPath.iterator.moveBackward(0);
			zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
			while (zeroPath.iterator.layer() != toor.layer() && zeroPath.iterator.hasBackward()) {
				zeroPath.iterator.moveBackward(0);
				zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
				Backward.add(new PathCollector(zeroPath));
			}
		}
		
		ArrayList<TrellisPath> paths = new ArrayList<TrellisPath>();
		
		while (tresholdForward + tresholdBackward <= upperBound) {
			if (Forward.size() < Backward.size()) {
				++tresholdForward;
				Forward = findForwardPaths(Forward, tresholdForward, root, metric);
			} else {
				++tresholdBackward;
				Backward = findBackwardPaths(Backward, tresholdBackward, metric);
			}
			
			for (PathCollector fpath : Forward) {
				if (Backward.contains(fpath)) {
					PathCollector bpath = Backward.floor(fpath);
					
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

	private static TreeSet<PathCollector> findForwardPaths(TreeSet<PathCollector> Forward,
			int tresholdForward, ITrellisIterator root, int metric) {
		logger.debug("tresholdForward = " + tresholdForward);
		
		TreeSet<PathCollector> newForward = new TreeSet<PathCollector>();
		
		while (!Forward.isEmpty()) {
			TreeSet<PathCollector> oldForward = new TreeSet<PathCollector>();
			
			for (Iterator<PathCollector> iterator = Forward.iterator(); iterator.hasNext();) {
				PathCollector path = iterator.next();
				iterator.remove();
				
				if (path.path.weight() >= tresholdForward) {
					addTheLeastPath(newForward, path);
					continue;
				}
				if (!path.iterator.hasForward()) {
					continue;
				}
				// вес пути path < tresholdForward
				for (int i = 0; i < path.iterator.getAccessors().length; ++i) {
					ITrellisEdge edge = path.iterator.getAccessors()[i];
					if (path.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
						if (path.iterator == root) {
							continue;
						}
						// проходим по всем вершинам нулевого цикла и сразу переходим из каждой из них по ненулевому пути.
						logger.debug("i = " + i);
						PathCollector zeroPath = new PathCollector(path);
						zeroPath.iterator.moveForward(i);
						zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
						
						while (zeroPath.iterator.layer() != path.iterator.layer() && zeroPath.iterator.hasForward()) {
							ITrellisEdge edges[] = zeroPath.iterator.getAccessors();
							for (int e = 1; e < edges.length; ++e) {
								PathCollector newPath = new PathCollector(zeroPath);
								newPath.iterator.moveForward(e);
								newPath.path.addVertex(newPath.iterator.vertexIndex(), edges[e].metrics()[metric]);
								if (newPath.path.weight() >= tresholdForward) {
									addTheLeastPath(newForward, newPath);
								} else {
									addTheLeastPath(oldForward, newPath);
								}
							}
							zeroPath.iterator.moveForward(0);
							zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
						}
						continue;
					}

					PathCollector newPath = new PathCollector(path);				// копируем путь
					newPath.iterator.moveForward(i);											// двигаемся вперед
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.metrics()[metric]);
										
					if (newPath.path.weight() >= tresholdForward) {
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
			int tresholdBackward, int metric) {
		logger.debug("tresholdBackward = " + tresholdBackward);

		TreeSet<PathCollector> newBackward = new TreeSet<PathCollector>();
		
		while (!Backward.isEmpty()) {
			TreeSet<PathCollector> oldBackward = new TreeSet<PathCollector>();
			
			for (Iterator<PathCollector> iterator = Backward.iterator(); iterator.hasNext();) {
				PathCollector path = iterator.next();
				iterator.remove();
				
				if (!path.iterator.hasBackward()) {
					continue;
				}
				
				for (int i = 0; i < path.iterator.getPredecessors().length; ++i) {
					ITrellisEdge edge = path.iterator.getPredecessors()[i];
					if (path.iterator.vertexIndex() == 0 && edge.metrics()[metric] == 0) {
						// проходим по всем вершинам нулевого цикла.
//						logger.debug("i = " + i);
//						PathCollector zeroPath = new PathCollector(path);
//						zeroPath.iterator.moveBackward(i);
//						zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
//						while (zeroPath.iterator.layer() != path.iterator.layer() && zeroPath.iterator.hasBackward()) {
//							ITrellisEdge edges[] = zeroPath.iterator.getPredecessors();
//							for (int e = 1; e < edges.length; ++e) {
//								PathCollector newPath = new PathCollector(zeroPath);
//								newPath.iterator.moveBackward(e);
//								newPath.path.addVertex(newPath.iterator.vertexIndex(), edges[e].metrics()[metric]);
//
//								if (newPath.path.weight > tresholdBackward) {
//									addTheLeastPath(newBackward, new PathCollector(zeroPath));
//								} else {
//									addTheLeastPath(oldBackward, newPath);
//								}
//							}
//							zeroPath.iterator.moveBackward(0);
//							zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
//						}
						
						continue;
					}
					
					PathCollector newPath = new BeastAlgorithm.PathCollector(path);				// копируем путь
					newPath.iterator.moveBackward(i);											// двигаемся назад
					newPath.path.addVertex(newPath.iterator.vertexIndex(), edge.metrics()[metric]);
					
					if (newPath.path.weight() > tresholdBackward) {
						addTheLeastPath(newBackward, path);
					} else {
						addTheLeastPath(oldBackward, newPath);
						
						if (newPath.iterator.vertexIndex() == 0 && newPath.iterator.hasBackward()) {
							// пришли в вершину нулевого цикла, пройдемся по нему.
							PathCollector zeroPath = new PathCollector(newPath);
							zeroPath.iterator.moveBackward(0);
							zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
							
							while (zeroPath.iterator.layer() != newPath.iterator.layer()) {
								addTheLeastPath(oldBackward, new PathCollector(zeroPath));
								if (zeroPath.iterator.hasBackward()) {
									zeroPath.iterator.moveBackward(0);
									zeroPath.path.addVertex(zeroPath.iterator.vertexIndex(), 0);
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
	private static void addTheLeastPath(TreeSet<PathCollector> pathes, PathCollector path) {
		if (pathes.contains(path)) {
			PathCollector path2 = pathes.floor(path);
			if (path.equals(path2) && path2.path.weight() > path.path.weight()) {
				// существующий путь с большей метрикой - удаляем его
				pathes.remove(path2);
			}
		}
		
		pathes.add(path); // если в pathes был путь с меньшей метрикой, то path не добавится.
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
