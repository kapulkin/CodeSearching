package search_procedures.tests;

import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import codes.MinDistance;

import search_procedures.FreeDist3CCEnumerator;
import trellises.Trellis;
import trellises.Trellises;


public class FreeDist3CCEnumeratorTest {
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void searcherShouldGiveCodeWithFreeDistEqualTo3() {
		FreeDist3CCEnumerator codeEnumerator = new FreeDist3CCEnumerator(4, 3);
		
		int codeCount = 0;
		while (codeEnumerator.hasNext()) {
			++codeCount;

			ConvCode code = codeEnumerator.next();
			logger.debug("code:\n" + code.checkMatrix());
			Trellis trellis = Trellises.trellisFromParityCheckHR(code.checkMatrix());
			MinDistance.computeDistanceMetrics(trellis);
			
			int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, 2 * (code.getDelay() + 1));
			logger.debug("free dist = " + freeDist);
			assertTrue(freeDist >= 3);
		}
		
		logger.info("Codes was found: " + codeCount);
		logger.info("Search is finished.");
	}
}
