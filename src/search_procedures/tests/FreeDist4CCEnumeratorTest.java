package search_procedures.tests;

import static org.junit.Assert.*;
import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_procedures.conv_codes.FreeDist4CCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class FreeDist4CCEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(FreeDist4CCEnumeratorTest.class);

	@Test
	public void searcherShouldGiveCodeWithFreeDistEqualTo3() {
		FreeDist4CCEnumerator codeEnumerator = new FreeDist4CCEnumerator(5, 9);
		
		int codeCount = 0;
		while (codeEnumerator.hasNext()) {
			++codeCount;

			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.parityCheck());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());
			MinDistance.computeDistanceMetrics(trellis);
			
			int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
			logger.debug("free dist = " + freeDist);
			assertTrue(freeDist >= 4);
		}
		
		logger.info("Codes were found: " + codeCount);
		logger.info("Search is finished.");
	}
}
