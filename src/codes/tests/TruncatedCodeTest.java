package codes.tests;

import static org.junit.Assert.*;

import math.BitArray;
import math.BlockMatrix;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.BlockCodeTrellis;
import trellises.ITrellis;

import codes.ConvCode;
import codes.TBCode;
import codes.ZTCode;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOTrellis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class TruncatedCodeTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void printTBCodeMatrix() {
		try {
			ConvCode code = IOConvCode.readConvCode(new BufferedReader(new FileReader(new File("conv_code2.txt"))));
			logger.debug("Conv code:");
			IOBlockMatrix.writeMatrix(new BlockMatrix(code.getGenBlocks()), new BufferedWriter(new OutputStreamWriter(System.out)));
			TBCode tbCode = new TBCode(code, 0);
			ITrellis trellis = new BlockCodeTrellis(tbCode.getGeneratorSpanForm());
			logger.debug("Tailbiting code:");
			IOBlockMatrix.writeMatrix(tbCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}
	}

	@Test
	public void printZTCodeMatrix() {
		ConvCode convCode = null;
		ZTCode ztCode = null;
		int scaleDelta = 0;
		try {
			convCode = IOConvCode.readConvCode(new BufferedReader(new FileReader(new File("conv_code2.txt"))));
			logger.debug("Conv code:");
			IOBlockMatrix.writeMatrix(new BlockMatrix(convCode.getGenBlocks()), new BufferedWriter(new OutputStreamWriter(System.out)));
			scaleDelta = convCode.getDelay();
			ztCode = new ZTCode(convCode, scaleDelta);
			logger.debug("Zero-tail code:");
			IOBlockMatrix.writeMatrix(ztCode.blockGenMatrix(), new BufferedWriter(new OutputStreamWriter(System.out)));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception.");
		}
		
		logger.debug("delay = " + convCode.getDelay());

		for (int word = 0; word < (1 << ztCode.getK()); ++word) {
			BitArray ztInfWord = new BitArray(ztCode.getK());
			BitArray convInfWord = new BitArray(ztCode.getK() + convCode.getDelay() * convCode.getK());
			for (int i = 0; i < ztCode.getK(); ++i) {
				ztInfWord.set(i, (word & (1 << i)) != 0);
				convInfWord.set(i, (word & (1 << i)) != 0);
			}
			
			BitArray ztCodeWord = ztCode.encodeSeq(ztInfWord);
			BitArray convCodeWord = convCode.encodeSeq(convInfWord);
			logger.debug("infWord: " + ztInfWord + ", ztCode: " + ztCodeWord + ", " + convCodeWord);
			assertEquals(ztCodeWord, convCodeWord);
		}
	}
}
