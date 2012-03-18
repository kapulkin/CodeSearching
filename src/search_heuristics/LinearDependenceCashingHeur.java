package search_heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import math.Poly;
import search_tools.CEnumerator;
import codes.Code;
import codes.ConvCode;

public class LinearDependenceCashingHeur implements IHeuristic {
	private int freeDist;
	private int maxDelay;
	private PolyLinearDependenceDataBase dataBase;
	
	public static class PolyLinearDependenceDataBase {
		
		static class PolyComparator implements Comparator<Poly> {

			@Override
			public int compare(Poly p1, Poly p2) {
				
				if (p1.getDegree() != p2.getDegree()) {
					return p1.getDegree() - p2.getDegree();
				}
				
				for (int j = 0;j < p1.getDegree(); ++j) {
					if (p1.getCoeff(j) != p2.getCoeff(j)) {
						return p1.getCoeff(j) ? 1 : -1;
					}
				}
				
				return 0;
			}
			
		}
		
		static class PolyArrayComparator implements Comparator<ArrayList<Poly>> {

			@Override
			public int compare(ArrayList<Poly> list1, ArrayList<Poly> list2) {
				if (list1.size() != list2.size()) {
					return list1.size() - list2.size();
				}
				
				PolyComparator polyComparator = new PolyComparator();
				for (int i = 0;i < list1.size(); ++i) {
					Poly p1 = list1.get(i);
					Poly p2 = list2.get(i);
					int cmp = polyComparator.compare(p1, p2);
					
					if (cmp != 0) {
						return cmp;
					}
				}
				
				return 0;
			}

			
		}
		
		private TreeMap<ArrayList<Poly>, Integer> data = new TreeMap<ArrayList<Poly>, Integer>(new PolyArrayComparator());
		
		public PolyLinearDependenceDataBase() {
			
		}
		
		public int checkPolys(ArrayList<Poly> polys) {
			Collections.sort(polys, new PolyComparator());
			
			Integer value = data.get(polys);
			
			if (value == null) {
				return -1;
			}
			
			return value;
		}
		
		public void addPolysCombination(ArrayList<Poly> polys, int weight) {
			Collections.sort(polys, new PolyComparator());
			
			data.put(polys, weight);
		}
	}
	
	public LinearDependenceCashingHeur(int freeDist, int maxDelay, PolyLinearDependenceDataBase dataBase) {
		this.freeDist = freeDist;
		this.dataBase = dataBase;
		this.maxDelay = maxDelay;
	}
	
	private int checkWeightOfCombination(ArrayList<Poly> polies, int weightUpperBound) {			
		for (int weight = 1; weight <= weightUpperBound; ++weight) {
			CEnumerator weightDistrEnum = new CEnumerator(polies.size() * (maxDelay + 1), weight);
			
			while (weightDistrEnum.hasNext()) {
				long[] weightDistr = weightDistrEnum.next();
				ArrayList<Poly> coeffs = new ArrayList<Poly>();
				
				for (int i = 0; i < polies.size(); ++i) {
					coeffs.add(new Poly());
				}
				
				for (int i = 0; i < weight; ++i) {
					int polyInd = (int)(weightDistr[i] / (maxDelay + 1));
					int coeff = (int)(weightDistr[i] % (maxDelay + 1));
					
					coeffs.get(polyInd).setCoeff(coeff, true);
				}
				
				Poly sum = new Poly();
				
				for (int i = 0; i < polies.size(); ++i) {
					sum.add(polies.get(i).mul(coeffs.get(i)));
				}
				
				if (sum.isZero()) {
					return weight;
				}
			}
		}
		
		return -1;
	}

	@Override
	public boolean check(Code code) {
		ConvCode _code = (ConvCode)code;
		ArrayList<Poly> parityCheckPolies = new ArrayList<Poly>();
		
		for (int i = 0;i < _code.getN(); ++i) {
			Poly poly = _code.parityCheck().get(0, i).clone();
			
			for (int j = maxDelay;j < poly.getDegree(); ++j) {
				poly.setCoeff(j, false);
			}				
			
			parityCheckPolies.add(poly);
			
			if (parityCheckPolies.size() >= 2) {
				int cashedWeight = dataBase.checkPolys(parityCheckPolies);
				
				if (cashedWeight == -1) {
					int weight = checkWeightOfCombination(parityCheckPolies, freeDist - 1);
					
					if (weight == -1) {
						continue;
					}
					
					dataBase.addPolysCombination(parityCheckPolies, weight);
					return false;
				}
				
				if (cashedWeight < freeDist) {
					return false;
				}
			}
		}
		
		
		
		return true;
	}
	

}
