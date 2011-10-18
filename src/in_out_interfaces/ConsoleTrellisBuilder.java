package in_out_interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import trellises.ITrellis;

import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.Matrix;
import math.PolyMatrix;
import codes.BlockCode;
import codes.ConvCode;

public class ConsoleTrellisBuilder {
	static final String usage = "Usage:\n" +
			"\ttrellis {-b|-c} input_file output_file\n" +
			"\ttrellis --help\n" +
			"\ttrellis --usage\n";
	static final String help =
			"-h, --help             print this help message.\n" +
			"-b                     read file with block codes.\n" +
			"-c                     read file with convolutional codes.\n";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args[0].equals("--usage")) {
			System.out.println(usage);
			return ;
		}
		
		if (args[0].equals("-h") || args[0].equals("--help")) {
			System.out.println(usage + "\n" + help);
			return ;
		}

		boolean isBlockCode;
		if (args[0].equals("-b")) {
			isBlockCode = true;
		} else if (args[0].equals("-c")) {
			isBlockCode = false;
		} else {
			System.out.println(usage);
			return ;
		}

		if (args.length < 3) {
			System.out.println(usage);
			return ;
		}
		
		String inputFileName = args[1];
		String outputFileName = args[2];
		
		try {
			if (isBlockCode) {
				Matrix matrix = IOMatrix.readMatrix(new Scanner(new File(inputFileName)));
				BlockCode code = new BlockCode(matrix, true);
				ITrellis trellis = code.getTrellis();
				IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File(outputFileName))));				
			} else {
				ConvCode code = IOConvCode.readConvCode(new Scanner(new File(inputFileName)));

				PolyMatrix minBaseG = ConvCodeAlgs.getMinimalBaseGenerator(code.generator());
				ConvCodeSpanForm spanForm = ConvCodeAlgs.buildSpanForm(minBaseG);
				
				ITrellis trellis = ConvCodeAlgs.buildTrellis(spanForm);
				IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File(outputFileName))));				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
