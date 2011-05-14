package search_procedures.tests;


import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import codes.MinDistance;

import search_procedures.HighRateCCSearcher;
import trellises.Trellis;
import trellises.Trellises;


public class HighRateCCSearcherTest {
	@Test
	public void SearcherTest() {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		int v = 4, freeDist = 5;
		HighRateCCSearcher codeSearcher = new HighRateCCSearcher(v, freeDist);
		
		ConvCode code;
		while ((code = codeSearcher.next()) != null) {
			logger.debug("code:\n" + code.checkMatrix());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.checkMatrix());
			MinDistance.computeDistanceMetrics(trellis);
			
			int actualFreeDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (code.getDelay() + 1));
			logger.debug("free dist = " + actualFreeDist);
			assertTrue(actualFreeDist >= freeDist);
		}
	}

}
