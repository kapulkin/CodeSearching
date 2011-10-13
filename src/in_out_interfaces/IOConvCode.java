package in_out_interfaces;

import java.io.IOException;
import java.util.Scanner;

import codes.ConvCode;

import math.BlockMatrix;
import math.Matrix;

public class IOConvCode {

	public static ConvCode readConvCode(Scanner scanner) throws IOException
	{
		int delay;				
	
		delay = scanner.nextInt();
		
		Matrix polyGen = IOMatrix.readMatrix(scanner);
		BlockMatrix dividedPolyGen = new BlockMatrix(polyGen, 1, delay+1);
		
		return new ConvCode(dividedPolyGen, true);
		/*int regCount = polyGen.getRowCount();
		int adderCount = polyGen.getColumnCount() / (delay+1);
		
		genMatr = new Matrix[regCount];
		for(int reg = 0;reg < polyGen.getRowCount();reg ++)
		{
			genMatr[reg] = new Matrix(adderCount, delay+1);
			
			for(int adder = 0;adder < adderCount;adder ++)
			{
				for(int cell = 0;cell < delay + 1;cell ++)
				{
					genMatr[reg].set(adder, cell, polyGen.get(reg, (delay+1)*adder+cell));
				}
			}
		}
		
		return new ConvCode(genMatr);/**/		
	}
	
}
