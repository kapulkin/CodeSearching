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
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		
		int[][] lowerBounds = DistanceBoundsParser.parse(false);
		int[][] upperBounds = DistanceBoundsParser.parse(true);
		
		for (int k = 1;k <= 30; ++k) {
			for (int n = 1;n <= 2 * k; ++n) {
				if (lowerBounds[k][n] != upperBounds[k][n]) {
					System.out.println("k=" + k + "n=" + n + " " + lowerBounds[k][n] + "-" + upperBounds[k][n]);
				}
			}			
		}		
	}

}
