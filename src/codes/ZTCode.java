package codes;

import math.MinDistance;
import trellises.Trellis;

/**
 * Zero-tail код. Усеченый код при L0 = delay.
 * @author fedor
 *
 */
public class ZTCode extends TruncatedCode {
	
	/**
	 * Построение ZT кода по сверточному
	 * @param code сверточный код
	 * @param scaleDelta определяет параметры ZT кода: k=(delay+1+scaleDelta)*b, n=(delay+1+scaleDelta)*c, где 
	 * b/c - скорость, а delay - задержка сверточного кода  	 
	 */
	public ZTCode(ConvCode code, int scaleDelta)
	{
		super(code, code.getDelay(), scaleDelta);
	}
	
	public int getMinDist()
	{
		if(minDist != -1)
			return minDist;
		
		return getMinDistByTrellis();
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
		minDist = MinDistance.findMinDistWithVA(t, 0, 1, false);
		
		return minDist;
	}
}
