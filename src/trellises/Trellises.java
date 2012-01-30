package trellises;

import java.util.ArrayList;

import trellises.Trellis.Vertex;
import trellises.algorithms.TrellisScanner;

import math.BitArray;
import math.BlockCodeAlgs;
import math.PolyMatrix;
import math.SpanForm;

public class Trellises {	
	
	/**
	 * Строит секционированную решетку кода по спеновой форме порождающей матрицы. Реализация основывается
	 * на проходе по столбцам матрицы сканирущей прямой, хранящей информацию об активных строках. Возможные 
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
					
		firstLayer[0].Predecessors = new IntEdge[0];
		lastLayer[0].Accessors = new IntEdge[0];
				
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
		
		BlockCodeAlgs.sortHeads(sf);
						
		int initialColumn = sf.getHead(begSeg);
		ArrayList<Integer> activeRows = new ArrayList<Integer>();
		Trellis.Vertex[] firstLayer;
		
		for(int i = 0;i < sf.Matr.getRowCount();i ++)
		{
			int shiftedColumn = initialColumn + sf.Matr.getColumnCount();
			
			if((sf.getHead(i) < initialColumn && initialColumn <= sf.getTail(i)) ||
				(sf.getHead(i) < shiftedColumn && shiftedColumn <= sf.getUncycledTail(i)))
			{
				activeRows.add(i);			
			}
		}
		
		firstLayer = new Trellis.Vertex[1 << activeRows.size()];
		for(int i = 0;i < firstLayer.length;i ++)
		{
			firstLayer[i] = new Vertex();
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
		int degree = parityCheck.get(0, parityCheck.getColumnCount() - 1).getDegree();

		int levels = parityCheck.getColumnCount() - 1;		
		ArrayList<Trellis.Vertex[]> layers = new ArrayList<Trellis.Vertex[]>();
		Trellis.Vertex[] firstLayer = new Trellis.Vertex[1<<degree];
	
		// индекс вершины определяет содержимое регистров памяти
		for(int v = 0;v < firstLayer.length;v ++)
		{
			firstLayer[v] = new Vertex();
			firstLayer[v].Accessors = new IntEdge[2];
			firstLayer[v].Predecessors = new IntEdge[2];
		}
		layers.add(firstLayer);
		
		for(int l = 0;l < levels-1;l ++)
		{
			Trellis.Vertex[] lastLayer = layers.get(l);
			Trellis.Vertex[] newLayer = new Trellis.Vertex[1 << degree];
			
			for(int v = 0;v < newLayer.length;v ++)
			{
				newLayer[v] = new Vertex();
				newLayer[v].Accessors = new IntEdge[2];
				newLayer[v].Predecessors = new IntEdge[2];
				
				// индекс ребра соответствует значению l-ого бита кодового слова.
				IntEdge edge0 = new IntEdge();
				IntEdge edge1 = new IntEdge();
				
				edge0.bits = new BitArray(1);
				edge0.src = v;	// при переходе из lastLayer по нулевому ребру содержимое памяти не менялось
				edge0.dst = v;
				edge0.metrics = new int[0];
				
				newLayer[v].Predecessors[0] = edge0;				
				
				if(lastLayer[edge0.src].Accessors[0] == null)
				{
					lastLayer[edge0.src].Accessors[0] = edge0;
				}else{
					lastLayer[edge0.src].Accessors[1] = edge0;
				}
				
				// при переходе из lastLayer по единичному ребру содержимое памяти изменилось в соотв. с H[l]
				// вычисляем маску изменения памяти
				int h = 0;
				for(int i = 0;i < parityCheck.get(0, l).getDegree() + 1;i ++)
				{
					if(parityCheck.get(0, l).getCoeff(i) == true)
					{
						h |= (1<<i);
					}
				}
				
				edge1.bits = new BitArray(1);
				edge1.bits.set(0, true);
				edge1.src = v ^ h;
				edge1.dst = v;
				edge1.metrics = new int[0];
				
				newLayer[v].Predecessors[1] = edge1;
				
				if(lastLayer[edge1.src].Accessors[0] == null)
				{
					lastLayer[edge1.src].Accessors[0] = edge1;
				}else{
					lastLayer[edge1.src].Accessors[1] = edge1;
				}
			}
			
			layers.add(newLayer);
		}
		
		Trellis.Vertex[] finalLayer = layers.get(levels - 1);
		
		// основное требование при переходе: младший регистр памяти должен стать равен нулю, т.к. это бит синдрома
		for(int v = 0;v < finalLayer.length;v ++)
		{	
			int h1 = 0, h2 = 0;
			
			for(int i = 0;i < parityCheck.get(0, levels-1).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels-1).getCoeff(i) == true)
				{
					h1 |= (1<<i);
				}
			}
			
			for(int i = 0;i < parityCheck.get(0, levels).getDegree() + 1;i ++)
			{
				if(parityCheck.get(0, levels).getCoeff(i) == true)
				{
					h2 |= (1<<i);
				}
			}
			
			IntEdge edge0 = new IntEdge();
			IntEdge edge1 = new IntEdge();
			
			// нулевое ребро соотв. нулевому значению предпоследнего бита кодового слова. 
			edge0.bits = new BitArray(2);
			edge0.src = v;
			edge0.metrics = new int[0];
			
			if((v & 1) == 0)
			{	
				edge0.dst = (v >> 1);				
			}else{
				edge0.dst = ((v ^ h2) >> 1);
				edge0.bits.set(1, true);
			}
			
			finalLayer[v].Accessors[0] = edge0;				
			
			if(firstLayer[edge0.dst].Predecessors[0] == null)
			{
				firstLayer[edge0.dst].Predecessors[0] = edge0;
			}else{
				firstLayer[edge0.dst].Predecessors[1] = edge0;
			}			
			// единичное ребро соотв. единичному значению предпоследнего бита кодового слова. 
			edge1.bits = new BitArray(2);
			edge1.bits.set(0, true);
			edge1.src = v;
			edge1.metrics = new int[0];
			
			if(((v ^ h1) & 1) == 0)
			{	
				edge1.dst = ((v ^ h1) >> 1);				
			}else{
				edge1.dst = (((v ^ h1) ^ h2) >> 1);
				edge1.bits.set(1, true);
			}
			
			finalLayer[v].Accessors[1] = edge1;				
			
			if(firstLayer[edge1.dst].Predecessors[0] == null)
			{
				firstLayer[edge1.dst].Predecessors[0] = edge1;
			}else{
				firstLayer[edge1.dst].Predecessors[1] = edge1;
			}
		}
		
		Trellis trellis = new Trellis();
				
		trellis.Layers = new Trellis.Vertex[layers.size()][];
		layers.toArray(trellis.Layers);
		
		return trellis;
	}

	/**
	 * Создает решетку в явном виде, эквивалентную исходной. Если <code>trellis</code> - и есть решетка в явном виде, то возвращает ее.  
	 * @param trellis решетка
	 * @return <code>trellis</code>, если <code>trellis</code> - решетка в явном виде.
	 */
	public static Trellis getExplicitTrellisOf(ITrellis trellis) {
		if (trellis instanceof Trellis) {
			return (Trellis)trellis;
		}

		return buildExplicitTrellis(trellis);
	}
	
	/**
	 * Строит решетку в явном виде эквивалентную исходной. Входная решетка
	 * должна удовлетворять ограничениям создаваемой решетки класса Trellis. 
	 * @param trellis решетка кода.
	 * @return решетка кода, эквивалентная исходной.
	 */
	public static Trellis buildExplicitTrellis(ITrellis trellis) {
		Trellis newTrellis = new Trellis();

		newTrellis.Layers = new Vertex[trellis.layersCount()][];
		
		for (int layer = 0; layer < newTrellis.Layers.length; ++layer) {
			long layerSize = trellis.layerSize(layer);
			if (layerSize > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("Trellis contains layers of length more, then " + Integer.MAX_VALUE);
			}
			newTrellis.Layers[layer] = new Vertex[(int)layerSize];
			for (int vertexIndex = 0; vertexIndex < newTrellis.Layers[layer].length; ++vertexIndex) {
				Vertex vertex = newTrellis.Layers[layer][vertexIndex] = new Vertex();
				
				ITrellisIterator iterator = trellis.iterator(layer, vertexIndex);
				ITrellisEdge accessors[] = iterator.getAccessors();
				vertex.Accessors = new IntEdge[accessors.length];
				for (int e = 0; e < accessors.length; ++e) {
					if (accessors[e].src() != vertexIndex) {
						throw new IllegalArgumentException("Wrong src index on the edge: " + layer + ", " + vertexIndex + ", " + e);
					}
					if (accessors[e].dst() >= Integer.MAX_VALUE || accessors[e].dst() < 0) {
						throw new IllegalArgumentException("A dst index on the edge is not inside [0, " + Integer.MAX_VALUE + "]:" +
								layer + ", " + vertexIndex + ", " + e);
					}

					vertex.Accessors[e] = new IntEdge(accessors[e]);
				}
				
				ITrellisEdge predecessors[] = iterator.getPredecessors();
				vertex.Predecessors = new IntEdge[predecessors.length];
				for (int i = 0; i < predecessors.length; ++i) {
					vertex.Predecessors[i] = new IntEdge(predecessors[i]);
				}
			}
		}
				
		return newTrellis;
	}
	
}
