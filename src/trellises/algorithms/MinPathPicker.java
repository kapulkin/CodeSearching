package trellises.algorithms;

import java.util.ArrayList;

import trellises.algorithms.TrellisPath.PathIterator;

public class MinPathPicker implements TrackProcessor<PathPicker> {
	private ArrayList<TrellisPath> paths = new ArrayList<TrellisPath>();
	
	@Override
	public void process(PathPicker fpath, PathPicker bpath) {
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
	
	public ArrayList<TrellisPath> getPaths() {
		return paths;
	}

}
