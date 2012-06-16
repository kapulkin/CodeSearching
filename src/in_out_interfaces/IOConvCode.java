package in_out_interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import codes.ConvCode;

import math.BlockMatrix;
import math.Matrix;
import math.PolyMatrix;

public class IOConvCode {

	public static ConvCode readConvCode(InputStream in) throws IOException {
		String matType = new BufferedReader(new InputStreamReader(in)).readLine().trim();
		
		return new ConvCode(new PolyMatrix(IOBlockMatrix.readMatrix(in)), matType == "G");
	}
	
	public static ConvCode readConvCode(Scanner scanner) throws IOException {
		int delay;		
		int rows;
		String type;
	
		type = scanner.next();
		delay = scanner.nextInt();
		rows = scanner.nextInt();
		
		Matrix polyMat = IOMatrix.readMatrix(scanner, rows);
		BlockMatrix dividedPolyMat = new BlockMatrix(polyMat, 1, delay+1);
		
		return new ConvCode(dividedPolyMat, type.contains("g"));		
	}
	
	public static void writeConvCode(ConvCode code, BufferedWriter writer, String type) throws IOException {
		writer.write(type + "\n");
		writer.write(code.getDelay() + "\n");
		writer.write(code.getK() + "\n");
		
		if (type == "g") {
			writer.write("oct\n" + (code.getDelay()+1) + "\n");
			IOPolyMatrix.writeMatrix(code.generator(), 8, writer);
		} else {
			writer.write("oct\n" + (code.getDelay()+1) + "\n");
			IOPolyMatrix.writeMatrix(code.parityCheck(), 8, writer);
		}
	}
}
