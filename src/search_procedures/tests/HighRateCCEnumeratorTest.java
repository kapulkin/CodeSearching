package search_procedures.tests;


import static org.junit.Assert.*;
import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_procedures.HighRateCCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class HighRateCCEnumeratorTest {
	@Test
	public void SearcherTest() {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		int v = 9, freeDist = 5;
		HighRateCCEnumerator codeSearcher = new HighRateCCEnumerator(v, freeDist);
		
		ConvCode code;
		int counter = 0;
		while ((code = codeSearcher.next()) != null) {
			++counter;
			logger.debug("code:\n{}",  code.parityCheck());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.parityCheck());
			MinDistance.computeDistanceMetrics(trellis);
			
			int actualFreeDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
			logger.debug("free dist = {}", actualFreeDist);
			assertTrue(actualFreeDist >= freeDist);
		}
		logger.debug("Codes found: {}", counter);
	}

}
