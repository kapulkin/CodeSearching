package search_heuristics;

import math.Poly;
import math.PolyMatrix;
import codes.Code;
import codes.ConvCode;

public class CCWeightsDistHeur implements IHeuristic {
	private int freeDist;
	
	public CCWeightsDistHeur(int freeDist) {
		this.freeDist = freeDist;
	}
	
	@Override
	public boolean check(Code code) {
		ConvCode convCode = (ConvCode)code;
		PolyMatrix genMatr = convCode.generator();		
		
		for (int i = 0;i < genMatr.getRowCount(); ++i) {
			int weight = 0;
			
			for (int j = 0;j < genMatr.getColumnCount(); ++j) {
				Poly g = genMatr.get(i, j);
				weight += g.getBitSet().cardinality();
//				for (int k = 0;k < g.getDegree(); ++k) {
//					if (g.getCoeff(k)) {
//						++weight;
//					}
//				}
			}
			
			if (weight < freeDist) {
				return false;
			}
		}
		
		return true;
	}

}
