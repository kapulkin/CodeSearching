import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import codes.BlockCode;
import codes.ConvCode;
import codes.MinDistance;
import codes.TBCode;
import codes.ZTCode;

import search_procedures.HighRateCCSearcher;
import trellises.Trellis;
import trellises.Trellises;

import math.Matrix;
import math.SpanForm;
import in_out_interfaces.IOBlockMatrix;
import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOTrellis;


public class Main {

	private static void blockCodeTest() throws IOException
	{
		Matrix mat = IOMatrix.readMatrix(new BufferedReader(new FileReader(new File("matr1.txt"))));		
		
		BlockCode blockCode = new BlockCode(mat, true);
		
		SpanForm sf = blockCode.getGenSpanForm();
		
		Matrix ort = blockCode.parityCheck();
		
		Trellis trellis = blockCode.getTrellis();
		
		int minDist = blockCode.getMinDistByTrellis();
		
		
		
		IOMatrix.writeMatrix(sf.Matr, new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOMatrix.writeMatrix(ort, new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();		
		
		IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new BufferedWriter(new FileWriter(new File("trellis.dot")))));
		
		System.out.println(minDist);

	}
	
	private static void convCodeTest() throws FileNotFoundException, IOException
	{
		ConvCode convCode = IOConvCode.readConvCode(new BufferedReader(new FileReader(new File("conv_code1.txt"))));
		TBCode tbCode = new TBCode(convCode, 2);
		ZTCode ztCode = new ZTCode(convCode, convCode.getDelay());
		
		IOMatrix.writeMatrix(tbCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOMatrix.writeMatrix(ztCode.generator(), new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOTrellis.writeTrellisInGVZFormat(ztCode.getTrellis(), new BufferedWriter(new FileWriter(new File("trellis.dot"))));
		
		System.out.println();
		
		System.out.println(tbCode.getMinDistByTrellis());
		System.out.println(MinDistance.findMinDistByGenerator(tbCode.generator()));
		System.out.println(convCode.getFreeDistanceByVA());
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		convCodeTest();
				
	}

}
