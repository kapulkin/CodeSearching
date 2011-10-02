package search_heuristics;

import java.util.Hashtable;
import java.util.Map.Entry;

import codes.Code;

public class CombinedHeuristic implements IHeuristic {
	private Hashtable<Integer, IHeuristic> heuristics = new Hashtable<Integer, IHeuristic>(); 
	
	public void addHeuristic(int priority, IHeuristic heuristic) {
		heuristics.put(priority, heuristic);
	}
	
	@Override
	public boolean check(Code code) {
		for(Entry<Integer, IHeuristic> entry : heuristics.entrySet()) {
			IHeuristic heuristic = entry.getValue();
			
			if (!heuristic.check(code)) {
				return false;
			}
		}
		
		return true;
	}

}
