package search_procedures.tests;

import static org.junit.Assert.*;
import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;

import search_procedures.block_codes.SearchMain;
import trellises.ITrellis;
import trellises.TrellisUtils;

public class BasicBlockCodeSearcherTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testCorrectnessForCodesFromPaper() throws IOException {
		SearchMain.initDesiredParameters("tb_codes_params.txt");
		
		int minK = 3, maxK = SearchMain.complexitiesInPaper.length - 1;
		Scanner scanner = new Scanner(new File("conv_codes_for_tb_truncation.txt"));
		
		for (int k = minK;k <= maxK; ++k) {
			 ConvCode code = IOConvCode.readConvCode(scanner);
			 BlockCode tbCode = new TBCode(code, k - (code.getDelay() + 1));
			 
			 IOMatrix.writeMatrix(tbCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
			 
			 try {
				tbCode.getGeneratorSpanForm();				
			}catch(Exception e) {
				assertTrue(false);
			}
				
			ITrellis trellis = tbCode.getTrellis();
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));

			int s = TrellisUtils.stateComplexity(trellis);
			int d = tbCode.getMinDist();
			int s_paper = SearchMain.complexitiesInPaper[k][2 * k];
			int d_paper = SearchMain.distancesInPaper[k][2 * k];
			
			logger.debug("k = " + k + " s = " + s + "(" + s_paper + ")" + " d = " + d + "(" + d_paper + ")");
			
			assertEquals(s_paper, s);
			assertEquals(d_paper, d);
		}
	}
}
