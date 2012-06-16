package in_out_interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.BlockMatrix;
import math.Matrix;

public class IOBlockMatrix {
	
	public static BlockMatrix readMatrix(InputStream _in) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(_in));
		StreamTokenizer tokenizer = new StreamTokenizer(in);
		int k, n, h, radix;
		
		tokenizer.nextToken();
		k = (int)tokenizer.nval;
		
		tokenizer.nextToken();
		n = (int)tokenizer.nval;	
		
		tokenizer.nextToken();
		h = (int)tokenizer.nval;
		
		tokenizer.nextToken();
		radix = (int)tokenizer.nval;
		
		BlockMatrix bmatrix = new BlockMatrix(k, n, 1, h);
		
		for (int i = 0;i < k; ++i) {
			in.readLine();
			StringTokenizer stokenizer = new StringTokenizer(in.readLine());
			for (int j = 0;j < n; ++j) {								
				long cellValue = Long.parseLong(stokenizer.nextToken(), radix);
				Matrix cell = new Matrix(1, h);
				
				for (int l = 0;l < h; ++l) {
					cell.set(0, l, (cellValue & (1L << l)) != 0); 
				}
				
				bmatrix.set(i, j, cell);
			}
		}
		
		return bmatrix;
	}
	
	public static BlockMatrix readMatrix(Scanner scanner) throws IOException
	{
		ArrayList<Matrix[]> data = new ArrayList<Matrix[]>();
		int blocksCnt;
		Matrix[] blocks;
		
		blocksCnt = scanner.nextInt();
		blocks = new Matrix[blocksCnt];
		
		for(int i = 0;i < blocksCnt;i ++)
		{
			int rows;
			
			rows = scanner.nextInt();			
			blocks[i] = IOMatrix.readMatrix(scanner, rows);
		}
		
		String line = scanner.next();
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
			line = scanner.next();
		}while(scanner.hasNextLine());
		
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
