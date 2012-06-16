package search_procedures.tests;

import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import math.ConvCodeSpanForm.SpanFormException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_heuristics.CombinedHeuristic;
import search_procedures.conv_codes.ExhaustiveHRCCEnumByCheckMatr;
import codes.ConvCode;

public class ExhaustiveHRCCEnumByCheckMatrTest {
static final private Logger logger = LoggerFactory.getLogger(RandomMethodsTest.class);
	
	@Test
	public void enumerationWithoutRepetitions() throws IOException, SpanFormException {
		ExhaustiveHRCCEnumByCheckMatr ccEnum = new ExhaustiveHRCCEnumByCheckMatr(3, 2, new CombinedHeuristic());
		ConvCode code;
			
		while ((code = ccEnum.next()) != null) {			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			
			IOPolyMatrix.writeMatrix(code.parityCheck(), writer);
		//	code.getFreeDist();
			writer.newLine();
		}
	}
}
