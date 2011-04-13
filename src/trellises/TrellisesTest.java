package trellises;

import static org.junit.Assert.*;
import in_out_interfaces.IOTrellis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import math.BitSet;
import math.Matrix;

import org.junit.Test;

import codes.BlockCode;

public class TrellisesTest {

	@Test
	public void visualizeTrellisFromGenSF() {

		// Генератор задан в минимальной спеновой форме. Секционированная решетка должна иметь 6 ярусов. 
		BitSet row0 = new BitSet(6); row0.set(0); row0.set(1); row0.set(3);
		BitSet row1 = new BitSet(6); row1.set(1); row1.set(4); row1.set(5);
		BitSet row2 = new BitSet(6); row2.set(2); row2.set(3); row2.set(4);
		Matrix generator = new Matrix(new BitSet[] { row0, row1, row2 });

		BlockCode code = new BlockCode(generator, true);
		Trellis trellis = code.getTrellis();
		
		try {
			IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
}
