package search_procedures.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import codes.MinDistance;

import search_procedures.FreeDist4CCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class FreeDist4CCEnumeratorTest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void searcherShouldGiveCodeWithFreeDistEqualTo3() {
		FreeDist4CCEnumerator codeEnumerator = new FreeDist4CCEnumerator(5, 9);
		
		int codeCount = 0;
		while (codeEnumerator.hasNext()) {
			++codeCount;

			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.checkMatrix());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.checkMatrix());
			MinDistance.computeDistanceMetrics(trellis);
			
			int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (code.getDelay() + 1));
			logger.debug("free dist = " + freeDist);
			assertTrue(freeDist >= 4);
		}
		
		logger.info("Codes were found: " + codeCount);
		logger.info("Search is finished.");
	}
}
