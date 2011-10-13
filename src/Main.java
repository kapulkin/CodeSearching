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
import codes.TBCode;
import codes.ZTCode;

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


public class Main {

/*	private static void blockCodeTest() throws IOException
	{
		Matrix mat = IOMatrix.readMatrix(new BufferedReader(new FileReader(new File("matr1.txt"))));				
		BlockCode blockCode = new BlockCode(mat, true);		
		SpanForm sf = blockCode.getGeneratorSpanForm();		
		Matrix ort = blockCode.parityCheck();		
		Trellis trellis = BlockCodeAlgs.buildTrellis(blockCode);		
		int minDist = MinDistance.findMinDistWithViterby(trellis, 0, 0);
		
		IOMatrix.writeMatrix(sf.Matr, new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();
		
		IOMatrix.writeMatrix(ort, new BufferedWriter(new OutputStreamWriter(System.out)));
		
		System.out.println();		
		
		IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new BufferedWriter(new FileWriter(new File("trellis.dot")))));
		
		System.out.println(minDist);

	}
	
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
	
	private static void smithDecompositionTest() throws FileNotFoundException, IOException
	{
		BlockMatrix mat = new BlockMatrix((IOMatrix.readMatrix(new BufferedReader(new FileReader(new File("matr1.txt"))))), 1, 3);
		PolyMatrix polyMat = new PolyMatrix(mat.getRowCount(), mat.getColumnCount()); 
		
		for(int i = 0;i < mat.getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				BitArray coeffs = mat.get(i, j).getRow(0);
				
				polyMat.set(i, j, new Poly(coeffs.toArray()));
			}
		}
		
		IOPolyMatrix.writeMatrix(polyMat, System.out);
		System.out.println();
		
		SmithDecomposition smithForm = new SmithDecomposition(polyMat);				
		
		IOPolyMatrix.writeMatrix(smithForm.getA(), System.out);
		System.out.println();
		
		IOPolyMatrix.writeMatrix(smithForm.getD(), System.out);
		System.out.println();
		
		IOPolyMatrix.writeMatrix(smithForm.getB(), System.out);
		System.out.println();
		
		IOPolyMatrix.writeMatrix(smithForm.getA().mul(smithForm.getD().mul(smithForm.getB())), System.out);
		System.out.println();
		
		PolyMatrix ort = ConvCodeAlgs.getOrthogonalMatrix(smithForm);
		
		IOPolyMatrix.writeMatrix(ort, System.out);
		System.out.println();				
		
		ConvCodeAlgs.toMinimalForm(polyMat);
		IOPolyMatrix.writeMatrix(polyMat, System.out);
		System.out.println();
	}/**/
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		//smithDecompositionTest();
		//convCodeTest();
		
		/*HighRateCCSearcher searcher = new HighRateCCSearcher();
		ConvCode code = searcher.search(4, 5, 2);
		
		if(code != null)
		{
			IOMatrix.writeMatrix(code.generator().breakBlockStructure(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
			
			IOMatrix.writeMatrix(new BlockMatrix(code.getGenBlocks()).breakBlockStructure(), new BufferedWriter(new OutputStreamWriter(System.out)));
			System.out.println();
		}/**/
		
//		PolyMatrix parityCheck = new PolyMatrix(1, 4);
//		
//		parityCheck.set(0, 0, new Poly(new Boolean[]{true}));
//		parityCheck.set(0, 1, new Poly(new Boolean[]{true, true}));
//		parityCheck.set(0, 2, new Poly(new Boolean[]{true, false, true}));
//		parityCheck.set(0, 3, new Poly(new Boolean[]{true, true, true}));
		
		PolyMatrix parityCheck = new PolyMatrix(1, 4);
		parityCheck.set(0, 0, new Poly(new Boolean[]{true, false}));
		parityCheck.set(0, 1, new Poly(new Boolean[]{true, true, true}));
		parityCheck.set(0, 2, new Poly(new Boolean[]{true, true, false, true}));
		parityCheck.set(0, 3, new Poly(new Boolean[]{true, true, true, true}));
		
		Trellis trellis = Trellises.trellisFromParityCheckHR(parityCheck);
		
		IOTrellis.writeTrellisInGVZFormat(trellis, new BufferedWriter(new FileWriter(new File("trellis.dot"))));/**/
		
		MinDistance.computeDistanceMetrics(trellis);
		
		int minDist = MinDistance.findMinDistWithBEAST(trellis, 0, 6);
		
		System.out.println(minDist);
	}

}
