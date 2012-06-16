package math.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_procedures.block_codes.SearchMain;

import math.BitArray;
import math.MLSCliqueMethod;
import math.MaximalLinearSubspace;
import static org.junit.Assert.*;

public class MaximalLinearSubspaceTest {
	static final private Logger logger = LoggerFactory.getLogger(MaximalLinearSubspaceTest.class);
	
	@Test
	public void maximal3ElementsSubspace() {
		BitArray s1 = new BitArray(4);
		s1.set(0);
		
		BitArray s2 = new BitArray(4);
		s2.set(1);
		
		BitArray s3 = new BitArray(4);
		s3.set(0);
		s3.set(1);
		
		BitArray s4 = new BitArray(4);
		s4.set(2);
		
		BitArray s5 = new BitArray(4);
		s5.set(3);
		
		MaximalLinearSubspace mls = new MLSCliqueMethod();
		BitArray[] basis = mls.findBasis(new BitArray[] { s1, s2, s3, s4, s5 }, 3);
		
		assertTrue(basis != null);
		
		logger.info(basis[0].toString());
		logger.info(basis[1].toString());
		logger.info(basis[2].toString());
	}
}
