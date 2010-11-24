package codes;

import math.BlockMatrix;
import math.Matrix;

/**
 * 
 * @author fedor
 *
 */
public class ConvCode {
	
	/**
	 * Свободное расстояние кода
	 */
	private int freeDist = -1;
	
	/**
	 * Количество регистров сдвига
	 */
	private int regCount;
	
	/**
	 * Количество сумматоров по модулю 2
	 */
	private int adderCount;	
	
	/**
	 * Задержка кодера
	 */
	private int delay;
	
	/**
	 * Порождающая матрица в полиномиальном представлении
	 */
	private BlockMatrix genMatr;
	
	/**
	 * Блоки, циклические сдвиги которых формируют порождающую матрицу сверточного кода 	 
	 */
	private Matrix[] genBlocks;
	
	/**
	 * Проверочная матрица в полиномиальном представлении
	 */
	private BlockMatrix checkMatr;
	
	
	/**
	 * 
	 * @param mat Порождающая или проверочная матрица в полиномиальном представлении
	 * @param isGenerator Является ли mat порождающей матрицей
	 */
	public ConvCode(BlockMatrix mat, boolean isGenerator)
	{
		if(isGenerator)
		{
			genMatr = mat;
			regCount = genMatr.getRowCount();
			adderCount = genMatr.getColumnCount();
			delay = genMatr.get(0, 0).getColumnCount()-1;
		}else{
			checkMatr = mat;
			adderCount = checkMatr.getColumnCount();
			regCount = adderCount - checkMatr.getRowCount();
			delay = checkMatr.get(0, 0).getColumnCount()-1;
		}
	}
	
	/**
	 * 
	 * @return Количество регистров сдвига
	 */
	public int getRegCount()
	{
		return regCount; 
	}
	
	/**
	 * 
	 * @return Количество сумматоров по модулю 2
	 */
	public int getAdderCount()
	{
		return adderCount;
	}
	
	/**
	 * 
	 * @return Задержка кодера
	 */
	public int getDelay()
	{
		return delay;
	}
	
	/**
	 * 
	 * @return Скорость кода
	 */
	public double getRate()
	{
		return (double)regCount / adderCount;
	}
	
	/**
	 * 
	 * @return Порождающая матрица в полиномиальном представлении
	 */
	public BlockMatrix generator()
	{
		return genMatr;
	}
	
	/**
	 * 
	 * @return Проверочная матрица в полиномиальном представлении
	 */
	public BlockMatrix checkMatrix()
	{
		return checkMatr;
	}
	
	/**
	 * 
	 * @return Блоки, циклические сдвиги которых формируют порождающую матрицу сверточного кода
	 */
	public Matrix[] getGenBlocks()
	{
		if(genBlocks != null)
		{
			return genBlocks;
		}
		
		genBlocks = new Matrix[delay + 1];
		for(int i = 0;i < delay + 1;i ++)
		{
			genBlocks[i] = new Matrix(regCount, adderCount);
			for(int j = 0;j < regCount;j ++)
			{
				for(int k = 0;k < adderCount;k ++)
				{
					genBlocks[i].set(j, k, genMatr.get(j, k).get(0, i));
				}
			}
		}
		
		return genBlocks;
	}
	
	public int getFreeDistanceByVA()
	{
		if(freeDist != -1)
		{
			return freeDist;
		}
		
		ZTCode zt = new ZTCode(this, delay);
		
		freeDist = zt.getMinDistByTrellis();
		return freeDist;
	}
		
}
