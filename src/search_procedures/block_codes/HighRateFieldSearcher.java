package search_procedures.block_codes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import math.Poly;

import search_heuristics.BCPreciseMinDist;
import search_heuristics.BCPreciseStateComplexity;
import search_heuristics.CCPreciseFreeDist;
import search_heuristics.CombinedHeuristic;
import search_heuristics.IHeuristic;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import search_procedures.conv_codes.SiftingCCEnumerator;
import search_tools.CEnumerator;
import trellises.BlockCodeTrellis;
import trellises.TrellisUtils;
import codes.BlockCode;
import codes.Code;
import codes.ConvCode;

public class HighRateFieldSearcher {
	
	static class NotTBStateComplexity implements IHeuristic {
		private int stateComplexity;
		
		public NotTBStateComplexity(int stateComplexity) {
			this.stateComplexity = stateComplexity;
		}

		@Override
		public boolean check(Code code) {
			BlockCode _code = (BlockCode)code;
			
			try {
				_code.setTrellis(new BlockCodeTrellis(_code.getGeneratorSpanForm()));
			}catch(Exception e) {
				return false;
			}
			
			return TrellisUtils.stateComplexity(_code.getTrellis()) <= stateComplexity;
		}
	} 
	
	static class PolyLinearDependenceDataBase {
		
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
	
	static class LinearDependenceCashingHeur implements IHeuristic {
		private int freeDist;
		private int maxDelay;
		private PolyLinearDependenceDataBase dataBase;
		
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
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BlockCodesTable.initDesiredParameters("tb_code_paramsHR.txt");
		
		int min_k = 3, max_k = 60;
		
		BlockCodesSearcher searcher = new BlockCodesSearcher();
		BlockCodesSearcher.TaskPool[] pools = new BlockCodesSearcher.TaskPool[(max_k - min_k) + 1];
		PolyLinearDependenceDataBase parityCheckDataBase = new PolyLinearDependenceDataBase();
		
		for (int k = min_k;k <= max_k; k += 3) {						
			ArrayList<BlockCodesSearcher.SearchTask> tasks = new ArrayList<BlockCodesSearcher.SearchTask>();
			
			for (int ddelta = 0;ddelta < 3; ++ddelta) {
				for (int sdelta = 0;sdelta < 3; ++sdelta){					
					BlockCodesSearcher.SearchTask task = new BlockCodesSearcher.SearchTask();
					int n = 4 * (k / 3);
					
					task.K = k;
					task.N = n;
					task.MinDist = BlockCodesTable.distanceUpperBounds[k][n] - ddelta;
					task.StateComplexity = Math.max(BlockCodesTable.complexityLowerBounds[k][n] / 2, 1) + sdelta;
			
					CombinedHeuristic heuristic = new CombinedHeuristic();
			
					heuristic.addHeuristic(1, new LinearDependenceCashingHeur(task.MinDist, Math.min(task.StateComplexity, 4), parityCheckDataBase));
					heuristic.addHeuristic(0, new CCPreciseFreeDist(task.MinDist));
			
					task.ConvCodeEnum = new SiftingCCEnumerator(new ExhaustiveHRCCEnumByCheckMatr(3, task.StateComplexity), heuristic);
					
					CombinedHeuristic tb_heuristic = new CombinedHeuristic();
										
					tb_heuristic.addHeuristic(1, new BCPreciseStateComplexity(task.StateComplexity, false));
					//tb_heuristic.addHeuristic(0, new NotTBStateComplexity(task.StateComplexity));
					tb_heuristic.addHeuristic(0, new BCPreciseMinDist(task.MinDist, false));
					
					task.Heuristic = tb_heuristic;
					tasks.add(task);
				}
			}
			
			pools[(k - min_k) / 3] = new BlockCodesSearcher.TaskPool(tasks);
		}
				
		BlockCode[] codes = searcher.searchTruncatedCodes(pools);
		
		BlockCodesTable.writeCodes(codes, new BufferedWriter(new FileWriter(new File("TBCodes.txt"))));
	}

}
