package codes;

import trellises.Trellis;
import trellises.Trellises;
import math.MathAlgs;
import math.Matrix;
import math.SpanForm;

/**
 * 
 * @author fedor
 *
 */
public class BlockCode {
	
	/**
	 * Минимальное расстояние кода
	 */
	protected int minDist = -1;
	
	/**
	 * Спэновая форма порождающей матрицы 
	 */
	protected SpanForm genSpanForm;
	
	/**
	 * Решетка кода 
	 */
	protected Trellis trellis;
	
	/**
	 * Количество информационных символов
	 */
	protected int k;
	
	/**
	 * Количество кодовых символов
	 */
	protected int n;	
	
	/**
	 * Порождающая матрица
	 */
	protected Matrix genMatr;
	
	/**
	 * Проверочная матрица
	 */
	protected Matrix checkMatr;
	
	protected BlockCode()
	{
		
	}
	
	/**
	 * 
	 * @param mat порождающая или проверочная матрица
	 * @param isGenerator true, если mat порождающая и false, если проверочная  
	 */
	public BlockCode(Matrix mat, boolean isGenerator)
	{
		if(isGenerator)
		{
			genMatr = mat;
			k = genMatr.getRowCount();
			n = genMatr.getColumnCount();
		}else{
			checkMatr = mat;
			n = checkMatr.getColumnCount();
			k = n - checkMatr.getRowCount();			
		}
	}
	
	/**
	 * 
	 * @return Количество информационных символов
	 */
	public int getK()
	{
		return k;
	}
	
	/**
	 * 
	 * @return Количество кодовых символов
	 */
	public int getN()
	{
		return n;
	}
	
	/**
	 * 
	 * @return Скорость кода
	 */
	public double getRate()
	{
		return (double)k / n;
	}
	
	/**
	 * 
	 * @return Порождающая матрица
	 */
	public Matrix generator()
	{
		if(genMatr == null)
			genMatr = MathAlgs.findOrthogonalMatrix(checkMatr, false);
		return genMatr;
	}
	
	/**
	 * 
	 * @return Проверочная матрица
	 */
	public Matrix parityCheck()
	{
		if(checkMatr == null)
			checkMatr = MathAlgs.findOrthogonalMatrix(genMatr, false);
		return checkMatr;
	}
	
	
	/**
	 * Метод возвращает спеновую форму порождающей матрицы. При первом вызове 
	 * приводит порождающую матрицу кода к спеновой форме. Т.о. любой вызов 
	 * метода <code>generator()</code> после вызова данного метода возварщает 
	 * матрицу в спеновой форме.
	 * 
	 * @return Спеновая форма порождающей матрицы
	 */
	public SpanForm getGeneratorSpanForm()
	{
		if(genSpanForm == null)
		{
			genSpanForm = MathAlgs.toSpanForm(generator());
		}
		
		return genSpanForm;
	}
	
	/* TODO: Этот метод, по-хорошему, надо убрать. Взамен создать класс 
	 * BlockCodeTrellis implemtnts ITellis, возвращающий итератор, позволяющий 
	 * ходить по решетке не строя ее.
	 */
	/**
	 * 	
	 * @return Решетка кода
	 */
	public Trellis getTrellis()
	{
		if(trellis == null)
		{
			trellis = Trellises.trellisFromGenSF(getGeneratorSpanForm());
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
		minDist = MinDistance.findMinDist(t, 0, 0);
		
		return minDist;
	}
			
}
