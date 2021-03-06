package search_procedures.tests;

import in_out_interfaces.IOPolyMatrix;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_procedures.conv_codes.FileCCEnumerator;

public class FileCCEnumeratorTest {
	static final private Logger logger = LoggerFactory.getLogger(FileCCEnumeratorTest.class);

	@Test
	public void test() {
		FileCCEnumerator enumerator;
		
		try {
			enumerator = new FileCCEnumerator("3&10&7.txt");
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			return;
		}
		
	//	enumerator.setParameters(12, 1);
		
		ConvCode code;
		while((code = enumerator.next()) != null){
			//logger.info("Free dist = " + code.getFreeDist());
			try {
				IOPolyMatrix.writeMatrix(code.generator(), System.out);
				IOPolyMatrix.writeMatrix(code.parityCheck(), System.out);
				logger.info("Free dist: " + code.getFreeDist());
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
	}

}
