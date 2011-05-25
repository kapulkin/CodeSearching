package in_out_interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.BlockMatrix;
import math.Matrix;

public class IOBlockMatrix {
	
	public static BlockMatrix readMatrix(BufferedReader reader) throws IOException
	{
		ArrayList<Matrix[]> data = new ArrayList<Matrix[]>();
		int blocksCnt;
		Matrix[] blocks;
		String line;
		
		while((line = reader.readLine()).equals("")) {}
		
		blocksCnt = Integer.parseInt(line);
		blocks = new Matrix[blocksCnt];
		
		for(int i = 0;i < blocksCnt;i ++)
		{
			int rows;
			
			while((line = reader.readLine()).equals("")) {}
			
			rows = Integer.parseInt(line);
			
			blocks[i] = IOMatrix.readMatrix(reader, rows);
		}
		
		while((line = reader.readLine()).equals("")) {}
		
		do
		{			
			Pattern patt = Pattern.compile("0|A|B|C|D|E");
			Matcher matcher = patt.matcher(line);
			Matrix[] comprRow;						
			ArrayList<Matrix> row = new ArrayList<Matrix>();			
						
			while(matcher.find())
			{
				if(matcher.group().equals("0"))
				{
					row.add(null);
				}else{
					row.add((Matrix)blocks[matcher.group().charAt(0)-'A'].clone());
				}
			}
			
			comprRow = new Matrix[row.size()];
			
			for(int i = 0;i < row.size();i ++)
			{
				comprRow[i] = row.get(i);
			}
						
			data.add(comprRow);
		}while((line=reader.readLine()) != null && !line.trim().equals(""));
		
		BlockMatrix matr = new BlockMatrix(data.size(), data.get(0).length,
				data.get(0)[0].getRowCount(), data.get(0)[0].getColumnCount());
		
		for(int i = 0;i < matr.getRowCount();i ++)
		{
			for(int j = 0;j < matr.getColumnCount();j ++)
			{
				matr.set(i, j, data.get(i)[j]);
			}
		}
		
		return matr;
	}

	public static void writeMatrix(BlockMatrix mat, BufferedWriter writer) throws IOException {
		for (int i = 0; i < mat.getRowCount(); ++i) {
			for (int v = 0; v < mat.getVerticalSize(); ++v) {
				for (int j = 0; j < mat.getColumnCount(); ++j) {
					for (int h = 0; h < mat.getHorizontalSize(); ++h) {
						writer.write(mat.get(i, j).get(v, h) ? "1" : "0");
					}
					writer.write(" ");
				}
				writer.newLine();
			}
		}
		writer.flush();
	}
}
