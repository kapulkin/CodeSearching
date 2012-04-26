package search_procedures.tests;

import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import math.ConvCodeSpanForm.SpanFormException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;

import search_heuristics.CombinedHeuristic;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;

public class RandomMethodsTest {
	static final private Logger logger = LoggerFactory.getLogger(RandomMethodsTest.class);
	
	@Test
	public void exhaustiveHRCCEnumByCheckMatr() throws IOException, SpanFormException {
		ExhaustiveHRCCEnumByCheckMatr ccEnum = new ExhaustiveHRCCEnumByCheckMatr(3, 4, new CombinedHeuristic());
		int count = 1000;
			
		for (int i = 0;i < count; ++i) {
			ConvCode code = ccEnum.random();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			
			IOPolyMatrix.writeMatrix(code.parityCheck(), writer);
			code.getFreeDist();
			writer.newLine();
		}
	}
}
