package trellises;

import java.util.ArrayList;

import math.BitSet;
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

}
