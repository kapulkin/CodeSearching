package search_procedures.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;
import search_heuristics.CCFirstLastBlockStateHeur;
import search_heuristics.CCWeightsDistHeur;
import search_heuristics.IHeuristic;
import search_heuristics.LRCCGreismerDistHeur;
import search_heuristics.TBWeightDistHeur;
import search_procedures.block_codes.BlockCodesTable;
import trellises.ITrellis;
import trellises.TrellisUtils;

public class BasicBlockCodeSearcherTest {
	static final private Logger logger = LoggerFactory.getLogger(BasicBlockCodeSearcherTest.class);

	@Test
	public void testCorrectnessForCodesFromPaper() throws IOException {
		BlockCodesTable.initDesiredParameters("tb_codes_params.txt");
		
		int minK = 3, maxK = BlockCodesTable.complexitiesInPaper.length - 1;
		Scanner scanner = new Scanner(new File("conv_codes_for_tb_truncation.txt"));
		
		for (int k = minK;k <= maxK; ++k) {
			ConvCode code = IOConvCode.readConvCode(scanner);
			BlockCode tbCode = new TBCode(code, k - (code.getDelay() + 1));
			 
//			code.parityCheck().sortColumns();
			testCodeParameters(tbCode); 
		}
	}
	
	private void testHeuristics(BlockCode tbCode) {
		int k = tbCode.getK();
		ArrayList<IHeuristic> cc_heuristics = new ArrayList<IHeuristic>();
		
		cc_heuristics.add(new CCWeightsDistHeur(BlockCodesTable.distancesInPaper[k][2 * k]));
		cc_heuristics.add(new CCFirstLastBlockStateHeur());
		cc_heuristics.add(new LRCCGreismerDistHeur(BlockCodesTable.distancesInPaper[k][2 * k]));
		
		IHeuristic tb_heuristic = new TBWeightDistHeur(BlockCodesTable.distancesInPaper[k][2 * k]);
		
		logger.debug("k: " + k);
		
		for (int i = 0;i < cc_heuristics.size(); ++i) {
			if (!cc_heuristics.get(i).check(((TBCode)tbCode).getParentCode())) {
				fail(cc_heuristics.get(i).getClass() + " fails");					
			}
		}
		
		if (!tb_heuristic.check(tbCode)) {
			fail(tb_heuristic.getClass() + " fails");
		}

	}
	
	private void testCodeParameters(BlockCode tbCode) throws IOException {
		//IOMatrix.writeMatrix(tbCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
		 
		/* try {
			tbCode.getGeneratorSpanForm();				
		}catch(Exception e) {
			fail("Unexpected exception.");
		}/**/
		 
		int k = tbCode.getK();
		long start, end;
		 
		logger.info("k = " + k);
			
		start = System.currentTimeMillis();
		
		ITrellis trellis = tbCode.getTrellis();
		
		end = System.currentTimeMillis();
		
		logger.info("trellis creation time = " + (double)(end - start) / 1000 + "s");
				
		start = System.currentTimeMillis();
		
//		int d = MinDistance.findMinDist(tbCode);
		int d = tbCode.getMinDist();
		
		end = System.currentTimeMillis();
		
		logger.info("minimal distance finding time = " + (double)(end - start) / 1000 + "s");
		
		int s = TrellisUtils.stateComplexity(trellis);
		int s_paper = BlockCodesTable.complexitiesInPaper[k][2 * k];
		int d_paper = BlockCodesTable.distancesInPaper[k][2 * k];
		
		logger.debug("layers: " + trellis.layersCount());
		
//		IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		
		logger.debug(" s = " + s + "(" + s_paper + ")" + " d = " + d + "(" + d_paper + ")");
		
		//assertEquals(s_paper, s);
		//assertEquals(d_paper, d);
	}
	
	
}
