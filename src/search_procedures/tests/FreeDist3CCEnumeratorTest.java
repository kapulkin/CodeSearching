package search_procedures.tests;

import static org.junit.Assert.*;
import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_procedures.conv_codes.FreeDist3CCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class FreeDist3CCEnumeratorTest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void searcherShouldGiveCodeWithFreeDistEqualTo3() {
		FreeDist3CCEnumerator codeEnumerator = new FreeDist3CCEnumerator(3, 5);
		
		int codeCount = 0;
		while (codeEnumerator.hasNext()) {
			++codeCount;

			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.parityCheck());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());
			MinDistance.computeDistanceMetrics(trellis);
			
			int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
			logger.debug("free dist = " + freeDist);
			assertTrue(freeDist >= 3);
		}
		
		logger.info("Codes was found: " + codeCount);
		logger.info("Search is finished.");
	}
}
