import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import codes.BlockCode;
import codes.ConvCode;
import codes.TBCode;
import codes.ZTCode;

import search_procedures.block_codes.BlockCodesTable;
import trellises.Trellis;
import trellises.Trellises;

import math.BitArray;
import math.BlockCodeAlgs;
import math.BlockMatrix;
import math.ConvCodeAlgs;
import math.Matrix;
import math.MinDistance;
import math.Poly;
import math.PolyMatrix;
import math.SmithDecomposition;
import math.SpanForm;
import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOPolyMatrix;
import in_out_interfaces.IOTrellis;
import in_out_interfaces.DistanceBoundsParser;


public class Main {

/*		
	private static void convCodeTest() throws FileNotFoundException, IOException
	{
		ConvCode convCode = IOConvCode.readConvCode(new BufferedReader(new FileReader(new File("conv_code2.txt"))));
		TBCode tbCode = new TBCode(convCode, 2);
		ZTCode ztCode = new ZTCode(convCode, convCode.getDelay());
		
		IOMatrix.writeMatrix(tbCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOMatrix.writeMatrix(ztCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOTrellis.writeTrellisInGVZFormat(BlockCodeAlgs.buildTrellis(ztCode), new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		
		System.out.println();
		
		System.out.println(tbCode.getMinDistByTrellis());
		System.out.println(MinDistance.findMinDist(tbCode.generator()));
//		System.out.println(convCode.getFreeDistanceByVA());
		System.out.println(MinDistance.findMinDistWithViterby(BlockCodeAlgs.buildTrellis(ztCode), 0, 0));
	}
	
	/**/
	
	private static void convertGenToParityCheck() throws IOException {
		int minK = 3, maxK = 26;
		Scanner scanner = new Scanner(new File("conv_codes_for_tb_truncation.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("HRconv_codes_for_tb_truncation.txt")));
		
		for (int k = minK;k <= maxK; ++k) {
			ConvCode code = IOConvCode.readConvCode(scanner);
			
			IOConvCode.writeConvCode(code, writer, "pc");
		}
	}
	
	private static void patch() throws IOException {
		Scanner scanner = new Scanner(new File("3&12&8.txt"));
		StringBuilder modifiedContent = new StringBuilder();		
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			
			if (line.contains(",")) {
				String[] gens = line.split(", ");
				
				for (int i = 0;i < 4; ++i) {
					String gen = gens[i];
					int digits = 0;
					
					for (int j = 0;j < gen.length(); ++j) {
						if (Character.isDigit(gen.charAt(j))) {
							++digits;
						}
					}
					
					if (i != 0) {
						modifiedContent.append(", ");
					}
					
					if (digits == 4) {
						modifiedContent.append("0" + gen);
					} else if (digits == 3) {
						modifiedContent.append("00" + gen);
					} else if (digits == 2) {
						modifiedContent.append("000" + gen);
					} else if (digits == 1) {
						modifiedContent.append("0000" + gen);
					}
					else {
						modifiedContent.append(gen);
					}					
				}
			} else {
				modifiedContent.append(line);
			}
			
			modifiedContent.append("\n");
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("_3&12&8.txt")));
		
		writer.write(modifiedContent.toString());
		writer.flush();
		//System.out.println(modifiedContent.toString().substring(0, 5000));
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		patch();		
		
		int[][] lowerBounds = DistanceBoundsParser.parse(false);
		int[][] upperBounds = DistanceBoundsParser.parse(true);
		
		BlockCodesTable.createTables(256, 256);
		BlockCodesTable.distanceUpperBounds = upperBounds;
		
		int b = 2;
		
		BlockCodesTable.computeTBStateLowerBounds(b, b + 1);
		
		for (int k = 1;k <= 100; ++k) {
			for (int n = b + 1;n <= 150; n += b + 1) {
				if (lowerBounds[k][n] != upperBounds[k][n] && lowerBounds[k][n] == 8) {// && b * (n / (b + 1)) <= k) {
					System.out.println("k=" + k + " n=" + n + " s=" + BlockCodesTable.complexityLowerBounds[b * (n / (b + 1))][n] + " " + lowerBounds[k][n] + "-" + upperBounds[k][n]);
					break;
				}
			}			
		}		
	}

}
