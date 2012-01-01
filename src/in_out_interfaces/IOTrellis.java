package in_out_interfaces;

import java.io.BufferedWriter;
import java.io.IOException;

import trellises.ITrellis;
import trellises.ITrellisEdge;


public class IOTrellis { 

	public static void writeTrellisInReadableFormat(ITrellis trellis, BufferedWriter writer) throws IOException
	{		
		writer.write("Количество слоев:" + Integer.toString(trellis.layersCount()));
		writer.newLine();
		
		for(int i = 0;i < trellis.layersCount();i ++)
		{			
			writer.write("Слой №" + Integer.toString(i+1));
			writer.newLine();
			
			for(int j = 0;j < trellis.layerSize(i);j ++)
			{
				writer.write(" Вершина №" + Integer.toString(j+1));
				writer.newLine();
				
				writer.write("  Последующие");
				writer.newLine();
				
				ITrellisEdge accessors[] = trellis.iterator(i, j).getAccessors();
				for(int k = 0;k < accessors.length;k ++)
				{
					ITrellisEdge acc = accessors[k];
						
					writer.write("   " + (acc.dst()+1) + " ");
										
					for (int b = 0;b < acc.bits().getFixedSize();b ++)
					{
						if (acc.bits().get(b) == true)
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
				
				ITrellisEdge predcessors[] = trellis.iterator(i, j).getPredecessors();
				for(int k = 0;k < predcessors.length;k ++)
				{
					ITrellisEdge pred = predcessors[k];
						
					writer.write("   " + (pred.src()+1) + " ");
										
					for(int b = 0;b < pred.bits().getFixedSize();b ++)
					{
						if(pred.bits().get(b) == true)
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
	
	public static void writeTrellisInGVZFormat(ITrellis trellis, BufferedWriter writer) throws IOException
	{		
		writer.write("digraph G {");
		writer.newLine();
		
		writer.write("ranksep=4;rotate=90;");
		writer.newLine();
		
		for(int i = 0;i < trellis.layersCount();i ++)
		{						
			writer.newLine();
			
			writer.write("subgraph cluster" + Integer.toString(i) + " {");
			
			for(int j = 0;j < trellis.layerSize(i);j ++)
			{
				writer.write("\"" + Integer.toString(i) + "," + Integer.toString(j) + "\";");
			}/**/
			
			writer.write("}");
			writer.newLine();
			
			for(int j = 0;j < trellis.layerSize(i);j ++)
			{					
				ITrellisEdge accessors[] = trellis.iterator(i, j).getAccessors();
				for(int k = 0;k < accessors.length;k ++)
				{
					ITrellisEdge acc = accessors[k];
					
					writer.write("\"" + Integer.toString(i) + "," + acc.src() + "\"" 
							+ " -> " + "\"" + Integer.toString((i+1) % trellis.layersCount()) + "," + acc.dst()+"\"");
					writer.write(" [weight = 100");
										
					if (acc.bits() != null) {
						writer.write(", label=\"");
						for(int b = 0;b < acc.bits().getFixedSize();b ++)
						{
							if(acc.bits().get(b) == true)
							{
								writer.write("1");
							}else
							{
								writer.write("0");
							}
							writer.flush();
						}
						writer.write("\"");
					}
					writer.write("];");
					
					writer.newLine();
				}
			}			
		}
		
		writer.write("}");
		
		writer.flush();
	}
}
