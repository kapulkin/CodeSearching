package codes;

import trellises.BlockCodeTrellis;
import trellises.ITrellis;
import trellises.LightTrellis;
import math.BitArray;
import math.BlockCodeAlgs;
import math.ConvCodeSpanForm.SpanFormException;
import math.MathAlgs;
import math.Matrix;
import math.MinDistance;
import math.SpanForm;

/**
 * 
 * @author fedor
 *
 */
public class BlockCode implements Code {
	
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
	protected ITrellis trellis;
	
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
	
	protected BlockCode() {}
	
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
	
	public boolean isGeneratorNull() {
		return genMatr == null;
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
	
	public boolean isParityCheckNull() {
		return checkMatr == null;
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
			genSpanForm = BlockCodeAlgs.toSpanForm(generator());
		}
		
		return genSpanForm;
	}
	
	/**
	 * 	
	 * @return Решетка кода
	 * @throws Exception 
	 */
	public ITrellis getTrellis() throws Exception {
		if (trellis == null) {
			ITrellis implicitTrellis = new BlockCodeTrellis(getGeneratorSpanForm());
		//	try {
		//		trellis = new LightTrellis(implicitTrellis);
		//	} catch (IllegalArgumentException e) {
				trellis = implicitTrellis;
		//	}
		}
		
		return trellis; 
	}

	public int getMinDist() throws Exception {
		if (minDist < 0) {
			minDist = MinDistance.findMinDist(this);
		}
		return minDist;
	}
	
	public void setMinDist(int minDist) {
		this.minDist = minDist;
	}
	
	public void setTrellis(ITrellis trellis) {
		this.trellis = trellis;		
	}
	
	/**
	 * 
	 * @return Минимальное расстояние кода
	 */
//	public int getMinDistByTrellis()
//	{
//		if(minDist != -1)
//		{
//			return minDist;
//		}
//		
//		Trellis t = getTrellis();
//		
//		MinDistance.computeDistanceMetrics(t);
//		minDist = MinDistance.findMinDist(t, 0, 0);
//		
//		return minDist;
//	}

	@Override
	public BitArray encodeSeq(BitArray infSeq) {
		if (infSeq.getFixedSize() % getK() != 0) {
			throw new IllegalArgumentException();
		}
		
		int wordsNumber = infSeq.getFixedSize() / getK();
		BitArray codeSeq = new BitArray(getN() * wordsNumber);
		for (int i = 0; i < wordsNumber; ++i) {
			BitArray infWord = infSeq.get(i * getK(), i * getK() + getK());
			BitArray codeWord = new BitArray(getN());
			for (int bitPos = infWord.nextSetBit(0); bitPos >= 0; bitPos = infWord.nextSetBit(bitPos + 1)) {
				codeWord.xor(generator().getRow(bitPos));
			}
			
			for (int j = 0; j < getN(); ++j) {
				codeSeq.set(i * getN() + j, codeWord.get(j));
			}
		}

		return codeSeq;
	}
			
}
