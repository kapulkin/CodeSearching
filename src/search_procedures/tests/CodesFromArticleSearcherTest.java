package search_procedures.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_procedures.CodesFromArticleSearcher;

public class CodesFromArticleSearcherTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void searchingLaunch() {
		CodesFromArticleSearcher searcher = new CodesFromArticleSearcher();
		
		for (int freeDist = 5; freeDist <= 5; ++freeDist) {
			logger.info("free distance: " + freeDist);
			try {
				searcher.searchCodes(freeDist);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Unexpected exception.");
			}
		}
		
		logger.info("Codes from article found:");
		for (int freeDist : searcher.getCodesFound().keySet()) {
			logger.info("free distance: " + freeDist);
			for (ConvCode code : searcher.getCodesFound().get(freeDist)) {
				logger.info("v = " + code.getDelay() + ", k = " + code.getK() + "\n" + code.parityCheck());
			}
		}
	}
}
