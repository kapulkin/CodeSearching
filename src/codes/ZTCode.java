package codes;

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
}
