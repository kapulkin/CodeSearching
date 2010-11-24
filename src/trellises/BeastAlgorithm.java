package trellises;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;

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
	
	public class Path implements Comparable<Path> {
		/**
		 * Индекс яруса, на котором заканчивается путь
		 */
		int layer;
		/**
		 * Индексы вершин в ярусах в пути
		 */
		int vertexIndices[];
		/**
		 * Метрика лучшего пути до вершины
		 */
		public double metric;

		/**
		 * Пути равны, если идут к одной вершине
		 */
		@Override
		public boolean equals(Object obj) {
			Path path = (Path) obj;
			if (path == null) {
				return false;
			}
			
			int index1 = vertexIndices.length - 1, index2 = path.vertexIndices.length - 1;
			
			return layer == path.layer
				&& vertexIndices[index1] == path.vertexIndices[index2];
		}

		@Override
		public int compareTo(Path path) {
			if (layer == path.layer) {
				int index1 = vertexIndices.length - 1, index2 = path.vertexIndices.length - 1;
				return vertexIndices[index1] - path.vertexIndices[index2];
			}
			return layer - path.layer;
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

	// поиск кратчайших путей с помощью BEAST работает эффективно только для блоковых кодов
	public static Path[] findOptimalPaths(Trellis trellis, int toorLayer, int initialVertex, int metric, double weights[],
			boolean mode, boolean code) {
		TreeSet<Path> Forward = new TreeSet<Path>(), Backward = new TreeSet<Path>();
		double tresholdForward = 0, tresholdBackward = 0;
		
		Path initialPath = new BeastAlgorithm().new Path();
		initialPath.layer = 0;
		initialPath.vertexIndices = new int[1];
		initialPath.vertexIndices[0] = initialVertex;
		initialPath.metric = 0;
		Forward.add(initialPath) ;

		initialPath = new BeastAlgorithm().new Path();
		initialPath.layer = (code == BLOCK) ? toorLayer : (toorLayer % trellis.Layers.length);
		initialPath.vertexIndices = new int[1];
		initialPath.vertexIndices[0] = initialVertex;
		initialPath.metric = 0;
		Backward.add(initialPath);
		
		TreeSet<Path> paths = new TreeSet<Path>();
		
		for (double weight: weights) {
			if (Forward.size() < Backward.size()) {
				tresholdForward += weight;
				Forward = findForwardPaths(Forward, tresholdForward, trellis, metric, toorLayer, mode, code);
			} else {
				tresholdBackward += weight;
				Backward = findBackwardPaths(Backward, tresholdBackward, trellis, metric, toorLayer, mode, code);
			}
			
			for (Path fpath : Forward) {
				if (Backward.contains(fpath)) {
					Path bpath = Backward.floor(fpath);
					
					if (fpath.metric + bpath.metric == 0) {
						continue;
					}
					
					Path newPath = new BeastAlgorithm().new Path();
					newPath.layer = toorLayer;

					newPath.vertexIndices = new int[fpath.vertexIndices.length + bpath.vertexIndices.length];
					copyForwardPath(newPath, fpath, 0);
					copyBackwardPath(newPath, bpath, fpath.vertexIndices.length - 1);
					newPath.metric = fpath.metric + bpath.metric;
					
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

	private static TreeSet<Path> findForwardPaths(TreeSet<Path> Forward,
			double tresholdForward, Trellis trellis, int metric, int toorLayer, boolean mode, boolean code) {
		TreeSet<Path> newForward = new TreeSet<Path>();
		
		while (!Forward.isEmpty()) {
			TreeSet<Path> oldForward = new TreeSet<Path>();
			
			for (Iterator<Path> iterator = Forward.iterator(); iterator.hasNext();) {
				Path path = iterator.next();
				iterator.remove();
				
				if (path.metric >= tresholdForward || (path.layer == toorLayer && code == BLOCK)) {
					addTheLeastPath(newForward, path);
					continue;
				}
				
				int layer = path.layer % trellis.Layers.length;
				int vertex = path.vertexIndices[path.vertexIndices.length - 1];
				for (Trellis.Edge edge : trellis.Layers[layer][vertex].Accessors) {
					if (mode == DISTANCE && path.layer == 0 && vertex == 0 && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь
						continue;
					}
					
					Path newPath = new BeastAlgorithm().new Path();
					newPath.layer = (code == BLOCK) ? (path.layer + 1) : ((path.layer + 1) % trellis.Layers.length);
					
					int length = path.vertexIndices.length;
					newPath.vertexIndices = new int[length + 1];
					System.arraycopy(path.vertexIndices, 0, newPath.vertexIndices, 0, length);
					newPath.vertexIndices[length] = edge.Dst;
					newPath.metric = path.metric + edge.Metrics[metric];
					
					if (newPath.metric >= tresholdForward) {
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

	private static TreeSet<Path> findBackwardPaths(TreeSet<Path> Backward,
			double tresholdBackward, Trellis trellis, int metric, int toorLayer, boolean mode, boolean code) {
		TreeSet<Path> newBackward = new TreeSet<Path>();
		
		while (!Backward.isEmpty()) {
			TreeSet<Path> oldBackward = new TreeSet<Path>();
			
			for (Iterator<Path> iterator = Backward.iterator(); iterator.hasNext();) {
				Path path = iterator.next();
				iterator.remove();
				
				if (path.layer == 0 && code == BLOCK) {
					addTheLeastPath(newBackward, path);
					continue;
				}
				
				int layer = reminder(path.layer, trellis.Layers.length);
				int vertex = path.vertexIndices[path.vertexIndices.length - 1];
				for (Trellis.Edge edge : trellis.Layers[layer][vertex].Predecessors) {
					if (mode == DISTANCE && path.layer == toorLayer && vertex == 0 && edge.Metrics[metric] == 0) {
						// Запрещаем нулевой путь
						continue;
					}
					
					Path newPath = new BeastAlgorithm().new Path();
					newPath.layer = (code == BLOCK) ? (path.layer - 1) : reminder(path.layer - 1, trellis.Layers.length);
					int length = path.vertexIndices.length;
					newPath.vertexIndices = new int[length + 1];
					System.arraycopy(path.vertexIndices, 0, newPath.vertexIndices, 0, length);
					newPath.vertexIndices[length] = edge.Src;
					newPath.metric = path.metric + edge.Metrics[metric];
					
					if (newPath.metric > tresholdBackward) {
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

	private static void copyBackwardPath(Path newPath, Path bpath, int position) {
		for (int i = bpath.vertexIndices.length - 1; i >= 0; --i, ++position) {
			newPath.vertexIndices[position] = bpath.vertexIndices[i];
		}
	}

	private static void copyForwardPath(Path newPath, Path fpath, int position) {
		for (int i = 0; i < fpath.vertexIndices.length; ++i) {
			newPath.vertexIndices[position + i] = fpath.vertexIndices[i];
		}
	}

	/**
	 * Метод добавляет <code>path</code> в множество <code>pathes</code>, если в нем не содержится путь, эквивалентный <code>path</code>,
	 * с меньшей метрикой. Более строго, <code>path</code> не добавляется тогда, когда в <code>pathes</code> уже есть путь <code>path2</code>
	 * такой, что <code>path.equals(path2) && path.metric <= path2.metric</code>.  
	 * @param pathes множество путей
	 * @param path добавляемый путь
	 */
	private static void addTheLeastPath(TreeSet<Path> pathes, Path path) {
		if (pathes.contains(path)) {
			Path path2 = pathes.floor(path);
			if (path.equals(path2) && path2.metric > path.metric) {
				// существующий путь с большей метрикой - удаляем его
				pathes.remove(path2);
			}
		}
		
		pathes.add(path); // если в pathes был путь с меньшей метрикой, то path не добавится.
	}
	
	private static int reminder(int value, int module) {
		if (module < 1) {
			throw new InvalidParameterException("module must be at least 1!");
		}
		
		int rem = value % module;
		if (rem < 0) {
			rem += module;
		}
		
		return rem;
	}
}
