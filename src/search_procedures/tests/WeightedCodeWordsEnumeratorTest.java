package search_procedures.tests;

import math.Poly;

import org.junit.Test;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_tools.WeightedCodeWordsEnumerator;


public class WeightedCodeWordsEnumeratorTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void operationalDemonstration() {
		WeightedCodeWordsEnumerator wordsEnumerator = new WeightedCodeWordsEnumerator(4, 4, 3);
		long counter = 0;
		while (wordsEnumerator.hasNext()) {
			Poly polies[] = wordsEnumerator.next();
			++counter;
			logger.debug(counter + ": " + polies);
		}
	}
}
