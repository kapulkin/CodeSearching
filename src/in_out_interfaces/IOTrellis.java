package in_out_interfaces;

import java.io.BufferedWriter;
import java.io.IOException;

import trellises.Trellis;


public class IOTrellis { 

	public static void writeTrellisInReadableFormat(Trellis trellis, BufferedWriter writer) throws IOException
	{		
		writer.write("Количество слоев:" + Integer.toString(trellis.Layers.length));
		writer.newLine();
		
		for(int i = 0;i < trellis.Layers.length;i ++)
		{			
			writer.write("Слой №" + Integer.toString(i+1));
			writer.newLine();
			
			for(int j = 0;j < trellis.Layers[i].length;j ++)
			{
				writer.write(" Вершина №" + Integer.toString(j+1));
				writer.newLine();
				
				writer.write("  Последующие");
				writer.newLine();
				
				for(int k = 0;k < trellis.Layers[i][j].Accessors.length;k ++)
				{
					Trellis.Edge acc = trellis.Layers[i][j].Accessors[k];
						
					writer.write("   " + Integer.toString(acc.Dst+1) + " ");
										
					for(int b = 0;b < acc.Bits.getFixedSize();b ++)
					{
						if(acc.Bits.get(b) == true)
						{
							writer.write("1");
						}else
						{
							writer.write("0");
						}
						writer.flush();
					}
					
					writer.newLine();
				}
				
				writer.write("  Предыдущие");
				writer.newLine();
				
				for(int k = 0;k < trellis.Layers[i][j].Predecessors.length;k ++)
				{
					Trellis.Edge pred = trellis.Layers[i][j].Predecessors[k];
						
					writer.write("   " + Integer.toString(pred.Src+1) + " ");
										
					for(int b = 0;b < pred.Bits.getFixedSize();b ++)
					{
						if(pred.Bits.get(b) == true)
						{
							writer.write("1");
						}else
						{
							writer.write("0");
						}
						writer.flush();
					}
					
					writer.newLine();
				}
			}
		}
		
		writer.flush();
	}
	
	public static void writeTrellisInGVZFormat(Trellis trellis, BufferedWriter writer) throws IOException
	{		
		writer.write("digraph G {");
		writer.newLine();
		
		writer.write("ranksep=4;rotate=90;");
		writer.newLine();
		
		for(int i = 0;i < trellis.Layers.length;i ++)
		{						
			writer.newLine();
			
			writer.write("subgraph cluster" + Integer.toString(i) + " {");
			
			for(int j = 0;j < trellis.Layers[i].length;j ++)
			{
				writer.write("\"" + Integer.toString(i) + "," + Integer.toString(j) + "\";");
			}/**/
			
			writer.write("}");
			writer.newLine();
			
			for(int j = 0;j < trellis.Layers[i].length;j ++)
			{												
				for(int k = 0;k < trellis.Layers[i][j].Accessors.length;k ++)
				{
					Trellis.Edge acc = trellis.Layers[i][j].Accessors[k];
					
					writer.write("\"" + Integer.toString(i) + "," + Integer.toString(acc.Src) + "\"" 
							+ " -> " + "\"" + Integer.toString((i+1) % trellis.Layers.length) + "," + Integer.toString(acc.Dst)+"\"");
					writer.write(" [weight = 100, label=\"");
										
					for(int b = 0;b < acc.Bits.getFixedSize();b ++)
					{
						if(acc.Bits.get(b) == true)
						{
							writer.write("1");
						}else
						{
							writer.write("0");
						}
						writer.flush();
					}
					
					writer.write("\"];");
					
					writer.newLine();
				}
			}			
		}
		
		writer.write("}");
		
		writer.flush();
	}

	
}
