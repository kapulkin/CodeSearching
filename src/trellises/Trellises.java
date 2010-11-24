package trellises;

import java.util.ArrayList;

import math.BitSet;
import math.PolyMatrix;
import math.SpanForm;

public class Trellises {	
	
	private static void sortHeads(SpanForm sf)
	{
		for(int i = 0;i < sf.SpanHeads.length;i ++)
		{
			int minHead = Integer.MAX_VALUE;
			int minRow = -1;
			
			for(int j = i;j < sf.SpanHeads.length;j ++)
			{
				if(sf.SpanHeads[j] < minHead)
				{
					minHead = sf.SpanHeads[j];
					minRow = j;
				}
			}
			
			if(i != minRow)
			{
				BitSet iRow = sf.Matr.getRow(i);
				int iHead = sf.SpanHeads[i], iTail = sf.SpanTails[i];
				
				sf.Matr.setRow(i, sf.Matr.getRow(minRow));
				sf.Matr.setRow(minRow, iRow);
				
				sf.SpanHeads[i] = sf.SpanHeads[minRow];
				sf.SpanHeads[minRow] = iHead;
				
				sf.SpanTails[i] = sf.SpanTails[minRow];
				sf.SpanTails[minRow] = iTail;
			}
		}
	}
	
	/**
	 * Строит секционированную решетку кода по спеновой форме порождающей матрицы. Реализация основывается
	 * на проходе по столбцам матрицы сканирущей прямой, хранящей информацию о активных строках. Возможные 
	 * положения прямой - в начале или в конце текущей активной строчки.   
	 * @param sf спеновая форма порождающей матрицы
	 * @return решетка кода
	 */
	public static Trellis trellisFromGenSF(SpanForm sf)
	{	
		Trellis trellis = trellisSegmentFromGenSF(sf, 0, sf.Matr.getRowCount());
		Trellis.Vertex[] firstLayer = trellis.Layers[0];
		Trellis.Vertex[] lastLayer = trellis.Layers[trellis.Layers.length-1];
		
		if(sf.IsTailbiting)
		{						
			for(int i = 0;i < firstLayer.length;i ++)
			{
				firstLayer[i].Predecessors = lastLayer[i].Predecessors;
			}
			
			Trellis cycledTrellis = new Trellis();
			
			cycledTrellis.Layers = new Trellis.Vertex[trellis.Layers.length-1][];
			for(int i = 0;i < cycledTrellis.Layers.length;i ++)
			{
				cycledTrellis.Layers[i] = trellis.Layers[i]; 
			}
			
			return cycledTrellis; 
		}
					
		firstLayer[0].Predecessors = new Trellis.Edge[0];
		lastLayer[0].Accessors = new Trellis.Edge[0];
				
		return trellis;
	}
	
	/**
	 * Строит сегмент секционированной решетки кода по спеновой форме порождающей матрицы. 
	 * @param sf спеновая форма порождающей матрицы
	 * @param begSeg начало сегмента
	 * @param endSeg конец сегмента
	 * @return решетка кода
	 */
	public static Trellis trellisSegmentFromGenSF(SpanForm sf, int begSeg, int endSeg)
	{		
		// слои решетки
		ArrayList<Trellis.Vertex[]> layers = new ArrayList<Trellis.Vertex[]>();		
		
		sortHeads(sf);
						
		int initialColumn = sf.SpanHeads[begSeg];
		ArrayList<Integer> activeRows = new ArrayList<Integer>();
		Trellis.Vertex[] firstLayer;
		
		for(int i = 0;i < sf.Matr.getRowCount();i ++)
		{
			int shiftedColumn = initialColumn + sf.Matr.getColumnCount();
			
			if((sf.SpanHeads[i] < initialColumn && initialColumn <= sf.SpanTails[i]) ||
				(sf.SpanHeads[i] < shiftedColumn && shiftedColumn <= sf.getUncycledTail(i)))
			{
				activeRows.add(i);			
			}
		}
		
		firstLayer = new Trellis.Vertex[1 << activeRows.size()];
		for(int i = 0;i < firstLayer.length;i ++)
		{
			firstLayer[i] = new Trellis().new Vertex();
		}					
		
		layers.add(firstLayer);
		
		TrellisScanner scanner = new TrellisScanner(sf, firstLayer, begSeg, endSeg);
		
		while(scanner.hasNextLayer())
		{									
			layers.add(scanner.buildNextLayer());
		}		
		
		Trellis trellis = new Trellis();
		
		trellis.Layers = new Trellis.Vertex[layers.size()][];
		
		for(int i = 0;i < layers.size();i ++)
		{
			trellis.Layers[i] = layers.get(i);
		}
		
		return trellis;
	}
	
	public static Trellis trellisFromParityCheckHR(PolyMatrix parityCheck)
	{
		int degree = parityCheck.get(0, parityCheck.getColumnCount()-1).getDegree();
		int levels = parityCheck.getColumnCount() - 1;		
		ArrayList<Trellis.Vertex[]> layers = new ArrayList<Trellis.Vertex[]>();
		Trellis.Vertex[] firstLayer = new Trellis.Vertex[1<<degree];
		
		for(int v = 0;v < firstLayer.length;v ++)
		{
			firstLayer[v] = new Trellis().new Vertex();
			firstLayer[v].Accessors = new Trellis.Edge[2];
			firstLayer[v].Predecessors = new Trellis.Edge[2];
		}
		
		layers.add(firstLayer);
		
		for(int l = 0;l < levels-1;l ++)
		{
			Trellis.Vertex[] lastLayer = layers.get(l);
			Trellis.Vertex[] newLayer = new Trellis.Vertex[1<<degree];
			
			for(int v = 0;v < newLayer.length;v ++)
			{
				newLayer[v] = new Trellis().new Vertex();
				newLayer[v].Accessors = new Trellis.Edge[2];
				newLayer[v].Predecessors = new Trellis.Edge[2];
				
				Trellis.Edge edge0 = new Trellis().new Edge();
				Trellis.Edge edge1 = new Trellis().new Edge();
				
				edge0.Bits = new BitSet(1);
				edge0.Src = v;
				edge0.Dst = v;
				edge0.Metrics = new double[0];
				
				newLayer[v].Predecessors[0] = edge0;				
				
				if(lastLayer[edge0.Src].Accessors[0] == null)
				{
					lastLayer[edge0.Src].Accessors[0] = edge0;
				}else{
					lastLayer[edge0.Src].Accessors[1] = edge0;
				}
				
				int h = 0;
				
				for(int i = 0;i < parityCheck.get(0, l).getDegree() + 1;i ++)
				{
					if(parityCheck.get(0, l).getCoeff(i) == true)
					{
						h += (1<<i);
					}
				}
				
				edge1.Bits = new BitSet(1);
				edge1.Bits.set(0, true);
				edge1.Src = v ^ h;
				edge1.Dst = v;
				edge1.Metrics = new double[0];
				
				newLayer[v].Predecessors[1] = edge1;
				
				if(lastLayer[edge1.Src].Accessors[0] == null)
				{
					lastLayer[edge1.Src].Accessors[0] = edge1;
				}else{
					lastLayer[edge1.Src].Accessors[1] = edge1;
				}
			}
			
			layers.add(newLayer);
		}
		
		Trellis.Vertex[] finalLayer = layers.get(levels - 1);
		
		for(int v = 0;v < finalLayer.length;v ++)
		{	
			int h1 = 0, h2 = 0;
			
			for(int i = 0;i < parityCheck.get(0, levels-1).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels-1).getCoeff(i) == true)
				{
					h1 += (1<<i);
				}
			}
			
			for(int i = 0;i < parityCheck.get(0, levels).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels).getCoeff(i) == true)
				{
					h2 += (1<<i);
				}
			}
			
			Trellis.Edge edge0 = new Trellis().new Edge();
			Trellis.Edge edge1 = new Trellis().new Edge();
			
			edge0.Bits = new BitSet(2);
			edge0.Src = v;
			edge0.Metrics = new double[0];
			
			if((v & 1) == 0)
			{	
				edge0.Dst = (v >> 1);				
			}else{
				edge0.Dst = ((v ^ h2) >> 1);
				edge0.Bits.set(1, true);
			}
			
			finalLayer[v].Accessors[0] = edge0;				
			
			if(firstLayer[edge0.Dst].Predecessors[0] == null)
			{
				firstLayer[edge0.Dst].Predecessors[0] = edge0;
			}else{
				firstLayer[edge0.Dst].Predecessors[1] = edge0;
			}			
			
			edge1.Bits = new BitSet(2);
			edge1.Bits.set(0, true);
			edge1.Src = v;
			edge1.Metrics = new double[0];
			
			if(((v ^ h1) & 1) == 0)
			{	
				edge1.Dst = ((v ^ h1) >> 1);				
			}else{
				edge1.Dst = (((v ^ h1) ^ h2) >> 1);
				edge1.Bits.set(1, true);
			}
			
			finalLayer[v].Accessors[1] = edge1;				
			
			if(firstLayer[edge1.Dst].Predecessors[0] == null)
			{
				firstLayer[edge1.Dst].Predecessors[0] = edge1;
			}else{
				firstLayer[edge1.Dst].Predecessors[1] = edge1;
			}
		}
		
		Trellis trellis = new Trellis();
		
		trellis.Layers = new Trellis.Vertex[layers.size()][];
		
		for(int i = 0;i < layers.size();i ++)
		{
			trellis.Layers[i] = layers.get(i);
		}
		
		return trellis;
	}

}
