package in_out_interfaces;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.BitArray;
import math.Matrix;

public class IOMatrix {
	
	public static Matrix readMatrix(Scanner scanner) throws IOException {
		return readMatrix(scanner, 0);
	}
	
	public static Matrix readMatrix(Scanner scanner, int rows) throws IOException {		
		ArrayList<BitArray> data = new ArrayList<BitArray>();
		EmptyLineSkipper skipper = new EmptyLineSkipper(scanner);
		String line = skipper.nextLine();
		boolean octValues = false;
		int binaryBitsInBlock = 0;
		
		if(line.trim().equals("oct"))
		{
			octValues = true;			
			binaryBitsInBlock = scanner.nextInt();
			line = skipper.nextLine();
		}
				
		do
		{			
			Pattern patt = octValues ? Pattern.compile("0|1|2|3|4|5|6|7") : Pattern.compile("0|1");
			Matcher matcher = patt.matcher(line);
			ArrayList<Boolean> row = new ArrayList<Boolean>();
			BitArray comprRow;
			
			if(!octValues)
			{							
				while(matcher.find())
				{
					row.add(matcher.group().equals("1"));
				}			
			}else{				
				int octDigitsToRead = (int) Math.ceil((double)binaryBitsInBlock / 3);
				int residual = 2 - (3 * octDigitsToRead -  binaryBitsInBlock);
				
				while(true)
				{
					if(!matcher.find())
						break;		
					
					ArrayList<Boolean> block = new ArrayList<Boolean>();
					
					for(int i = 0;i < octDigitsToRead;i ++)
					{
						if(i != 0 && !matcher.find())
							throw new IOException();
						
						int octDigit = Integer.parseInt(matcher.group());
						for(int j = ((i == 0) ? residual : 2);j >= 0; --j)
						{
						//	if(3 * i + (2 - j) >= binaryBitsInBlock)
							//	break;
							block.add(0, (octDigit & (1 << j)) != 0);
						}
					}
					
					row.addAll(block);
				}				
			}
			
			comprRow = new BitArray(row.size());
			
			for(int i = 0;i < row.size();i ++)
			{
				comprRow.set(i, row.get(i));
			}
			
			data.add(comprRow);
			
			if(!scanner.hasNext())
				break;
			
			line = scanner.nextLine();
			
			if(line.isEmpty())
				break;
			
		}while((rows <= 0 || data.size() < rows));
		
		Matrix mat = new Matrix(data.size(), data.get(0).size());
		
		for(int i = 0;i < data.size();i ++)
		{
			mat.setRow(i, data.get(i));
		}
		
		return mat;		
	}
	
	public static void writeMatrix(Matrix mat, BufferedWriter writer) throws IOException {
		for(int i = 0;i < mat.getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				if(mat.get(i, j) == true)
				{
					writer.write("1");
				}else{
					writer.write("0");
				}
			}
			writer.newLine();
		}
		writer.flush();
	}
}
