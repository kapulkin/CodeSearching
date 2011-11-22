package search_procedures.tests;

import math.Poly;

import org.junit.Test;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_tools.WeightedCodeWordsEnumerator;


public class WeightedCodeWordsEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(WeightedCodeWordsEnumeratorTest.class);
	
	@Test
	public void operationalDemonstration() {
		WeightedCodeWordsEnumerator wordsEnumerator = new WeightedCodeWordsEnumerator(4, 4, 3);
		long counter = 0;
		while (wordsEnumerator.hasNext()) {
			Poly polies[] = wordsEnumerator.next();
			++counter;
			if (logger.isDebugEnabled()) {
				String str = counter + " ";
				for (Poly poly : polies) {
					str += poly + " ";
				}
				//logger.debug(counter + ": " + polies);
				logger.debug(str);
			}
		}
	}
}
