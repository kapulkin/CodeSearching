package trellises;

import java.util.ArrayList;
import java.util.TreeMap;

import math.BitSet;
import math.GrayCode;
import math.SpanForm;

public class TrellisScanner
{
	private SpanForm sf;
	
	// упорядоченные концы активных строк
	private TreeMap<Integer, Integer> activeRowTails = new TreeMap<Integer, Integer>();
	
	// активные строки в столбце scanColumn
	private ArrayList<Integer> activeRows = new ArrayList<Integer>();
	
	// сканируемая строка
	private int scanRow;
	
	// столбец, на котором остановилась сканирующая прямая
	private int scanColumn;
	
	// столбец, до которого производится сканирование
	private int maxColumn;
	
	// слой, к которому достраивается следующий за ним
	private Trellis.Vertex[] leftLayer;		
	
	public TrellisScanner(SpanForm sf, Trellis.Vertex[] firstLayer, int begSeg, int endSeg)
	{
		this.scanRow = begSeg;
		this.scanColumn = sf.SpanHeads[begSeg];		
		
		if(endSeg < sf.Matr.getRowCount())
		{
			this.maxColumn = sf.SpanHeads[endSeg]-1;
		}else{
			this.maxColumn = sf.Matr.getColumnCount()-1;
		}
			
		this.sf = sf;
		this.leftLayer = firstLayer;
				
		for(int i = 0;i < sf.Matr.getRowCount();i ++)
		{
			int shiftedColumn = scanColumn + sf.Matr.getColumnCount();
			
			if((sf.SpanHeads[i] < scanColumn && scanColumn <= sf.SpanTails[i]) ||
				(sf.SpanHeads[i] < shiftedColumn && shiftedColumn <= sf.getUncycledTail(i)))
			{
				activeRows.add(i);			
				activeRowTails.put(sf.SpanTails[i], i);
			}
		}
		
	}
	
	public boolean hasNextLayer()
	{
		return scanColumn <= maxColumn;
	}		
	
	/**
	 * Выполняет построение слоя, следующего за leftLayer. Конструкция зависит от текущего столбца scanColumn:
	 * 1) Если scanColumn указывает на начало последней активной строки, то производится расщепление по соответствующему
	 * информационному биту. В этом случае, исходящая степень вершин в leftLayer равна 2.
	 * 2) Если scanColumn указывает на конец последней активной строки, то происходит только склейка вершин.
	 * В этом случае, исходящая степень вершин в leftLayer равна 1.  				
	 * @return rightLayer
	 */
	public Trellis.Vertex[] buildNextLayer()
	{			
		Trellis.Vertex[] rightLayer;
		
		// граница сканирования
		int bound;		
		
		// множество строк, которые останутся активными после деактивации на данном шаге
		ArrayList<Integer> stillActiveRows = new ArrayList<Integer>();
		
		// верно ли, что на данном шаге исходящая степень вершин левого слоя равна 2
		boolean onExpandStage = false;
		
		for(int i = 0;i < activeRows.size();i ++)
		{
			stillActiveRows.add(i);
		}
		
		bound = maxColumn;
		
		// если сканирующая прямая в положении начала строки, то сделать ее активной
		if(scanColumn == sf.SpanHeads[scanRow])
		{
			onExpandStage = true;
			stillActiveRows.add(activeRows.size());
			bound = Math.min(bound, sf.getUncycledTail(scanRow)-1);
		}
		
		if(scanRow < sf.Matr.getRowCount() - 1)
		{
			bound = Math.min(bound, sf.SpanHeads[scanRow+1]-1);
		}
		
		// Находим все строки, которые заканчиваются до границы bound
		while(activeRowTails.size() > 0)
		{
			int nearestTail = activeRowTails.firstEntry().getKey();
			int correspRow = activeRowTails.firstEntry().getValue();
			
			if(nearestTail <= bound)
			{				
				stillActiveRows.remove(stillActiveRows.indexOf(activeRows.indexOf(correspRow))); 
			}else{
				break;
			}
			
			activeRowTails.remove(nearestTail);
		}					

		BitSet lastEdgeBits = new BitSet(bound - scanColumn + 1);
		// количесво битов характеризующих состояние в левом слое
		int lStateBitsCount = activeRows.size();		
		// количесво битов характеризующих состояние в правом слое
		int rStateBitsCount = stillActiveRows.size();
		//состояние в правом слое, соответствующее первому ребенку текущей вершины 
		int rStateFirst = 0;
		//состояние в правом слое, соответствующее второму ребенку текущей вершины 
		int rStateSecond = 0;
		//размер правого слоя
		int rLayerSize = 1 << rStateBitsCount;
		//текущие количества предшественников у вершин правого слоя
		int[] rPredCounts = new int[rLayerSize];
		
		rightLayer = new Trellis.Vertex[rLayerSize];
		for(int v = 0;v < leftLayer.length;v ++)
		{
			//состояние в левом слое
			int lState = GrayCode.getWord(v);				
			int changedBit = GrayCode.getChangedBit(v);
							
			// если состояние в правом слое поменялось
			if(changedBit == -1 || stillActiveRows.contains(changedBit))
			{				
				int predsArraySize = onExpandStage ? 1 << Math.max(lStateBitsCount - rStateBitsCount + 1, 0) : 1 << Math.max(lStateBitsCount - rStateBitsCount, 0); 
				
				if(changedBit != -1)
				{
					//изменившийся бит правого состояния
					int stillActBit = stillActiveRows.indexOf(changedBit);
					rStateFirst ^= (1 << stillActBit);
				}
				
				if(rPredCounts[rStateFirst] == 0)
				{											
					rightLayer[rStateFirst] = new Trellis().new Vertex();
					rightLayer[rStateFirst].Predecessors = new Trellis.Edge[predsArraySize];
				}
				
				// если исходящая степень равна двум, то надо учесть второе ребро
				if(onExpandStage)
				{					
					rStateSecond = rStateFirst+(1<<(rStateBitsCount-1));
					
					if(rPredCounts[rStateSecond] == 0)
					{																	
						rightLayer[rStateSecond] = new Trellis().new Vertex();
						rightLayer[rStateSecond].Predecessors = new Trellis.Edge[predsArraySize];
					}
				}
									
			}
			
			//расчитываем биты на ребрах
			BitSet changedBitContr = new BitSet(bound - scanColumn + 1);
			BitSet activeBitContr = new BitSet(bound - scanColumn + 1);
			
			for(int col = scanColumn;col <= bound;col ++)
			{
				if(changedBit != -1)
				{
					int changedRow = activeRows.get(changedBit);
					changedBitContr.set(col - scanColumn, sf.Matr.get(changedRow, col));
				}
				activeBitContr.set(col - scanColumn, sf.Matr.get(scanRow, col));
			}
			
			lastEdgeBits.xor(changedBitContr);
			
			Trellis.Edge edge1 = new Trellis().new Edge();
			Trellis.Edge edge2 = new Trellis().new Edge();
			
			edge1 = new Trellis().new Edge();
			edge1.Src = lState;
			edge1.Dst = rStateFirst;
			edge1.Bits = (BitSet)lastEdgeBits.clone();
			edge1.Metrics = new double[0];
			
			rightLayer[rStateFirst].Predecessors[rPredCounts[rStateFirst]++] = edge1;
			
			if(onExpandStage)
			{
				edge2 = new Trellis().new Edge();
				edge2.Src = lState;
				edge2.Dst = rStateSecond;
				edge2.Bits = (BitSet)lastEdgeBits.clone();
				edge2.Bits.xor(activeBitContr);
				edge2.Metrics = new double[0];
				
				leftLayer[lState].Accessors = new Trellis.Edge[]{edge1, edge2};					
				rightLayer[rStateSecond].Predecessors[rPredCounts[rStateSecond]++] = edge2;
			}else{
				leftLayer[lState].Accessors = new Trellis.Edge[]{edge1};
			}
			
		}
		
		for(int i = activeRows.size()-1;i >= 0;i --)
		{
			if(!stillActiveRows.contains(i))
			{
				activeRows.remove(i);
			}
		}
		
		if(onExpandStage)
		{
			activeRows.add(scanRow);
			activeRowTails.put(sf.getUncycledTail(scanRow), scanRow);
		}
		scanColumn = bound+1;	
		
		if(scanRow < sf.Matr.getRowCount()-1 && scanColumn == sf.SpanHeads[scanRow+1])
		{
			scanRow ++;
		}
					
		leftLayer = rightLayer;
					
		return rightLayer;
	}

}

