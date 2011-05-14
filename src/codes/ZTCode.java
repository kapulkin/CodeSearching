package codes;

import math.BlockMatrix;
import math.Matrix;

public class ZTCode extends BlockCode {

	/**
	 * Сверточный код, по которому был построен данный код
	 */
	private ConvCode parentCode;
	
	/**
	 * Блочное представление порождающей матрицы
	 */
	private BlockMatrix ztGenMatr;
	
	/**
	 * Построение ZT кода по сверточному
	 * @param convCode сверточный код
	 * @param scaleDelta определяет параметры ZT кода: k=(delay+1+scaleDelta)*b, n=(delay+1+scaleDelta)*c, где 
	 * b/c - скорость, а delay - задержка сверточного кода  	 
	 */
	public ZTCode(ConvCode convCode, int scaleDelta)
	{
		int scale = convCode.getDelay() + 1 + scaleDelta;		
		Matrix[] genBlocks = convCode.getGenBlocks();
		
		parentCode = convCode;
		ztGenMatr = new BlockMatrix(scaleDelta+1, scale);
		k = convCode.getK() * (scaleDelta+1);
		n = convCode.getN() * scale;
		
		for(int rowBlock = 0;rowBlock < scaleDelta+1;rowBlock ++)
		{
			for(int colBlock = 0;colBlock < scale;colBlock ++)
			{
				int blockNumber = colBlock - rowBlock;
				
				if(blockNumber >= 0 && blockNumber < convCode.getDelay() + 1)
				{
					ztGenMatr.set(rowBlock, colBlock, genBlocks[blockNumber]);
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
			genMatr = ztGenMatr.breakBlockStructure();
		}
		
		return genMatr;
	}
	
	/**
	 * 
	 * @return Порождающая матрица блоковой структуры
	 */
	public BlockMatrix generatorZT()
	{
		return ztGenMatr;
	}
	
	/**
	 * 
	 * @return Сверточный код, по которому был построен данный код
	 */
	public ConvCode getParentCode()
	{
		return parentCode;
	}
	
}
