package search_procedures.tests;

import in_out_interfaces.IOBlockMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Assert;
import org.junit.Test;

import codes.ConvCode;

import search_procedures.ConvCodeListener;
import search_procedures.HighRateCCSearcher;


public class HighRateCCSearcherTest {
	@Test
	public void SearcherTest() {
		try {
			final HighRateCCSearcher codeSearcher = new HighRateCCSearcher();
	
			ConvCodeListener listener = new ConvCodeListener() {
				
				@Override
				public void searchFinished() {
				}
				
				@Override
				public void codeFound(ConvCode code) {
//					try {
//						IOBlockMatrix.writeMatrix(code.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
//					} catch (IOException e) {
//						e.printStackTrace();
//						Assert.fail("Unexpected exception.");
//					}
				}
			};

			codeSearcher.addCodeListener(listener);
			codeSearcher.startSearch(3, 4, 3);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception.");
		}
	}

}
