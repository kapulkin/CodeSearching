package codes;

import trellises.Trellis;
import trellises.Trellises;
import math.BlockMatrix;
import math.MathAlgs;
import math.Matrix;
import math.SpanForm;

/**
 * Тайлбитинговый код
 * @author fedor
 *
 */
public class TBCode extends BlockCode {	
	
	/**
	 * Сверточный код, по которому был построен данный код
	 */
	private ConvCode parentCode;
	
	/**
	 * Блочное представление порождающей матрицы
	 */
	private BlockMatrix tbGenMatr;	
	
	/**
	 * Построение тайлбитингого кода по сверточному
	 * @param convCode сверточный код
	 * @param scaleDelta определяет параметры TB кода: k=(delay+1+scaleDelta)*b, n=(delay+1+scaleDelta)*c, где 
	 * b/c - скорость, а delay - задержка сверточного кода  	 
	 */
	public TBCode(ConvCode convCode, int scaleDelta)
	{
		int scale = convCode.getDelay() + 1 + scaleDelta;		
		Matrix[] genBlocks = convCode.getGenBlocks();
		
		parentCode = convCode;
		tbGenMatr = new BlockMatrix(scale, scale);
		k = convCode.getRegCount() * scale;
		n = convCode.getAdderCount() * scale;
		
		for(int rowBlock = 0;rowBlock < scale;rowBlock ++)
		{
			for(int colBlock = 0;colBlock < scale;colBlock ++)
			{
				int blockNumber = (colBlock + (scale - rowBlock)) % scale;
				
				if(blockNumber < convCode.getDelay() + 1)
				{
					tbGenMatr.set(rowBlock, colBlock, genBlocks[blockNumber]);
				}
			}
		}
	
	}
	
	/**
	 * 
	 * @return Порождающая матрица
	 */
	public Matrix generator()
	{
		if(genMatr == null)
		{
			genMatr = tbGenMatr.breakBlockStructure();
		}
		
		return genMatr;
	}
	
	/**
	 * 
	 * @return Порождающая матрица блоковой структуры
	 */
	public BlockMatrix generatorTB()
	{
		return tbGenMatr;
	}
	
	/**
	 * 
	 * @return Сверточный код, по которому был построен данный код
	 */
	public ConvCode getParentCode()
	{
		return parentCode;
	}
	
	/**
	 * 
	 * @return Тайлбитинговая решетка кода
	 */
	public Trellis getTrellis()
	{	
		if(trellis != null)
		{
			return trellis;
		}
		
		// матрица вида Go...Gm - паттерн, циклическими сдвигами которого получается порождающая матрица
		Matrix pattern = (new BlockMatrix(parentCode.getGenBlocks())).breakBlockStructure();
		
		// спеновая форма паттерна
		SpanForm pattSpanForm = MathAlgs.toSpanForm(pattern);
		
		// разбиение паттерна на блоки
		BlockMatrix dividedPattern = new BlockMatrix(pattern, parentCode.getRegCount(), parentCode.getAdderCount());
		
		int scale = tbGenMatr.getRowCount();
		int[] spanHeads = new int[scale * parentCode.getRegCount()];
		int[] spanTails = new int[scale * parentCode.getRegCount()];
				
		for(int rowBlock = 0;rowBlock < scale;rowBlock ++)
		{			
			for(int i = 0;i < parentCode.getRegCount();i ++)
			{
				spanHeads[rowBlock * parentCode.getRegCount() + i] = (pattSpanForm.SpanHeads[i] + rowBlock * parentCode.getAdderCount()) % (tbGenMatr.getTotalColumnCount());
				spanTails[rowBlock * parentCode.getRegCount() + i] = (pattSpanForm.SpanTails[i] + rowBlock * parentCode.getAdderCount()) % (tbGenMatr.getTotalColumnCount());
			}
			
			for(int colBlock = 0;colBlock < scale;colBlock ++)
			{
				int blockNumber = (colBlock + (scale - rowBlock)) % scale;
				
				if(blockNumber < parentCode.getDelay() + 1)
				{
					tbGenMatr.set(rowBlock, colBlock, dividedPattern.get(0, blockNumber));
				}
			}
		}
		
		genMatr = tbGenMatr.breakBlockStructure();
		
		genSpanForm = new SpanForm(genMatr, spanHeads, spanTails);
		genSpanForm.IsTailbiting = true;
		
		Trellis segment = Trellises.trellisSegmentFromGenSF(genSpanForm, 0, parentCode.getRegCount());
		
		Trellis.Vertex[] firstLayer = segment.Layers[0];
		Trellis.Vertex[] lastLayer = segment.Layers[segment.Layers.length-1];
		
		for(int i = 0;i < firstLayer.length;i ++)
		{
			firstLayer[i].Predecessors = lastLayer[i].Predecessors;
		}		
		
		trellis = new Trellis();
		trellis.Layers = new Trellis.Vertex[segment.Layers.length-1][];
		for(int i = 0;i < trellis.Layers.length;i ++)
		{
			trellis.Layers[i] = segment.Layers[i]; 
		}		
		
		return trellis;
	}
	
	/**
	 * 
	 * @return Минимальное расстояние кода
	 */
	public int getMinDistByTrellis()
	{
		if(minDist != -1)
		{
			return minDist;
		}
		
		Trellis t = getTrellis();
		
		MinDistance.computeDistanceMetrics(t);
		minDist = MinDistance.findMinDistByTrellis(t, 0, tbGenMatr.getColumnCount());
		
		return minDist;
	}
	
}
