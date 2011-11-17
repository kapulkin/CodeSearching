package search_procedures.tests;


import static org.junit.Assert.*;
import math.MinDistance;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import codes.tests.ConvCodeTest;

import search_procedures.conv_codes.HighRateCCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class HighRateCCEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(HighRateCCEnumeratorTest.class);

	@Test
	public void SearcherTest() {
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
