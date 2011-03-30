package search_procedures.tests;

import in_out_interfaces.IOMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.Assert;
import org.junit.Test;

import codes.ConvCode;

import search_procedures.HighRateCCSearcher;


public class HighRateCCSearcherTest {
	@Test
	public void SearcherTest() {
		ConvCode code = HighRateCCSearcher.search(4, 3, 3);
		try {
			IOMatrix.writeMatrix(code.generator().breakBlockStructure(), new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception.");
		}
	}

}
