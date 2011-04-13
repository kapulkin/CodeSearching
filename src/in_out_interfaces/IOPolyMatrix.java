package in_out_interfaces;

import java.io.BufferedWriter;
import java.io.IOException;

import math.Poly;
import math.PolyMatrix;

public class IOPolyMatrix {

	public static void writeMatrix(PolyMatrix mat, BufferedWriter writer) throws IOException
	{
		for(int i = 0;i < mat.getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				Poly p = mat.get(i, j);
				
				if(p.isZero())
				{
					writer.write("0  ");
					continue;
				}
				
				if(p.getCoeff(0) == true)
				{
					writer.write("1");
					if(p.getDegree() > 0)
					{
						writer.write("+");
					}
				}
				
				for(int k = 1;k < p.getDegree()+1;k ++)
				{					
					if(p.getCoeff(k))
					{						
						if(k > 1)
						{
							writer.write("D" + k);
						}else{
							writer.write("D");
						}
						
						if(k < p.getDegree())
						{
							writer.write("+");
						}
					}
				}
				
				writer.write("  ");
			}
						
			writer.newLine();
		}
		
		writer.newLine();
		writer.flush();
	}
	
}
