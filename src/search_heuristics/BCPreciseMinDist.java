package search_heuristics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import in_out_interfaces.IOMatrix;
import codes.BlockCode;
import codes.Code;

public class BCPreciseMinDist implements IHeuristic {
	private int minDist;
	private boolean checkGenRowIndependence; 
	
	public BCPreciseMinDist(int expectedMinDist, boolean checkGenRowIndependence) {
		this.minDist = expectedMinDist;
		this.checkGenRowIndependence = checkGenRowIndependence;
	}
	
	@Override
	public boolean check(Code code) {
		BlockCode _code = (BlockCode)code;
		
		if (checkGenRowIndependence){
			try {
				_code.getGeneratorSpanForm();
			}catch(Exception e) {
				return false;
			}
		}
		
		/*System.out.println(_code.getMinDist());
		try {
			IOMatrix.writeMatrix(_code.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.in.read();
		} catch (IOException e) {			
			e.printStackTrace();
		}/**/
		
		int minDistance;
		
		try {
			minDistance = _code.getMinDist();
		} catch(Exception e) {
			return false;
		}
		
		return minDistance >= minDist;
	}

}
