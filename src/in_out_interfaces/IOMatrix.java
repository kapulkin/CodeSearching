package in_out_interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.BitArray;
import math.Matrix;

public class IOMatrix {
	
	public static Matrix readMatrix(BufferedReader reader) throws IOException
	{
		return readMatrix(reader, 0);
	}
	
	public static Matrix readMatrix(BufferedReader reader, int rows) throws IOException
	{		
		ArrayList<BitArray> data = new ArrayList<BitArray>();
		String line;
		boolean octValues = false;
		int columnCount = 0;
		
		while((line = reader.readLine()).equals("")) {}
		
		if(line.equals("oct"))
		{
			octValues = true;
			while((line = reader.readLine()).equals("")) {}
			columnCount = Integer.parseInt(line);
			line = reader.readLine();
		}
		
		do
		{			
			Pattern patt = octValues ? Pattern.compile("0|1|2|3|4|5|6|7") : Pattern.compile("0|1");
			Matcher matcher = patt.matcher(line);
			BitArray comprRow;
			
			if(!octValues)
			{
				ArrayList<Boolean> row = new ArrayList<Boolean>();			
							
				while(matcher.find())
				{
					row.add(matcher.group().equals("1"));
				}
				
				comprRow = new BitArray(row.size());
				
				for(int i = 0;i < row.size();i ++)
				{
					comprRow.set(i, row.get(i));
				}
			}else{
				comprRow = new BitArray(columnCount);
				for(int i = 0;i < Math.ceil(columnCount / 3);i ++)
				{
					if(matcher.find())
					{
						int octDigit = Integer.parseInt(matcher.group());
						for(int j = 3*i;j < Math.min(columnCount, 3*i+3);j ++)
						{
							if((octDigit&(1<<(j-3*i))) != 0)
							{
								comprRow.set(j, true);
							}
						}
					}else{
						throw new IOException();
					}
				}
			}
			
			data.add(comprRow);
		}while((rows <= 0 || data.size() < rows) && (line=reader.readLine()) != null && !line.trim().equals(""));
		
		Matrix mat = new Matrix(data.size(), data.get(0).size());
		
		for(int i = 0;i < data.size();i ++)
		{
			mat.setRow(i, data.get(i));
		}
		
		return mat;		
	}
	
	public static void writeMatrix(Matrix mat, BufferedWriter writer) throws IOException
	{
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
