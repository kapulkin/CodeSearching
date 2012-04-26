package codes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.ConvCodeTrellis;
import trellises.ITrellis;
import trellises.Trellis;
import trellises.Trellises;
import trellises.LightTrellis;
import math.BitArray;
import math.BlockMatrix;
import math.ConvCodeAlgs;
import math.ConvCodeSpanForm;
import math.ConvCodeSpanForm.SpanFormException;
import math.Matrix;
import math.MinDistance;
import math.PolyMatrix;
import math.SmithDecomposition;

/**
 * 
 * @author fedor
 *
 */
public class ConvCode implements Code{
	static private Logger logger = LoggerFactory.getLogger(ConvCode.class); 
	
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
	private PolyMatrix genMatr = null;
		
	/**
	 * Блоки, циклические сдвиги которых формируют порождающую матрицу сверточного кода 	 
	 */
	private Matrix[] genBlocks = null;
	
	/**
	 * Проверочная матрица в полиномиальном представлении
	 */
	private PolyMatrix checkMatr = null;
	
	/**
	 * Спеновая форма порождающей матрицы
	 */
	private ConvCodeSpanForm spanForm = null;

	private ITrellis trellis; 
	
	/**
	 * @param matrix Порождающая или проверочная матрица в полиномиальном представлении
	 * @param isGenerator Является ли mat порождающей матрицей
	 */
	public ConvCode(PolyMatrix matrix, boolean isGenerator)
	{
		if(isGenerator)
		{
			genMatr = matrix;
			regCount = genMatr.getRowCount();
			adderCount = genMatr.getColumnCount();
			delay = ConvCodeAlgs.getHighestDegree(genMatr);
		}else{
			checkMatr = matrix;
			adderCount = checkMatr.getColumnCount();
			regCount = adderCount - checkMatr.getRowCount();
			delay = ConvCodeAlgs.getHighestDegree(checkMatr);
		}
	}

	public ConvCode(BlockMatrix matrix, boolean isGenerator) {
		this(new PolyMatrix(matrix), isGenerator);
	}
	
	public ConvCode(Matrix genBlocks[]) {
		this.genBlocks = genBlocks;
		regCount = genBlocks[0].getRowCount();
		adderCount = genBlocks[0].getColumnCount();
		delay = genBlocks.length - 1;
	}
	
	public ConvCode(ConvCodeSpanForm spanForm) {
		delay = spanForm.delay;
		regCount = spanForm.getRowCount();
		adderCount = spanForm.matrix.getColumnCount();

		genBlocks = new Matrix[delay + 1];
		for (int degree = 0; degree < genBlocks.length; ++degree) {
			BitArray data[] = new BitArray[regCount]; 
			for (int i = 0; i < regCount; ++i) {
				data[i] = spanForm.matrix.getRow(degree * regCount + i);
			}
			genBlocks[degree] = new Matrix(data);
		}
	}
	
	/**
	 * 
	 * @return Количество регистров сдвига
	 */
	public int getK()
	{
		return regCount; 
	}
	
	/**
	 * 
	 * @return Количество сумматоров по модулю 2
	 */
	public int getN()
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
	public PolyMatrix generator()
	{
		if (genMatr == null) {
			if (genBlocks != null) {
				genMatr = ConvCodeAlgs.buildPolyComposition(genBlocks);
			} else {
				genMatr = ConvCodeAlgs.getOrthogonalMatrix(new SmithDecomposition(checkMatr));
				ConvCodeAlgs.toMinimalForm(genMatr);
			}
		}
		return genMatr;
	}

	public boolean isGeneratorNull() {
		return genMatr == null;
	}
	
	/**
	 * 
	 * @return Проверочная матрица в полиномиальном представлении
	 */
	public PolyMatrix parityCheck()
	{
		if (checkMatr == null) {
			checkMatr = ConvCodeAlgs.getOrthogonalMatrix(new SmithDecomposition(generator()));
			ConvCodeAlgs.toMinimalForm(checkMatr);
		}
		return checkMatr;
	}
	
	public boolean isParityCheckNull() {
		return checkMatr == null;
	}
	
	/**
	 * Метод приводит порождающую матрицу к minimal-base форме, а затем к спеновой форме.
	 * После этого возвращает спеновую форму матрицы.
	 * @return Спеновая форма порождающей матрицы. 
	 * @throws SpanFormException 
	 */
	public ConvCodeSpanForm spanForm() throws SpanFormException {
		if (spanForm == null) {
			genMatr = ConvCodeAlgs.getMinimalBaseGenerator(generator());
			spanForm = ConvCodeAlgs.buildSpanForm(genMatr);
		}
		return spanForm;
	}
	
	/**
	 * 
	 * @return Блоки, циклические сдвиги которых формируют порождающую матрицу сверточного кода
	 */
	public Matrix[] getGenBlocks()
	{
		if(genBlocks == null)
		{
			genBlocks = ConvCodeAlgs.buildPowerDecomposition(generator());
		}
		
		return genBlocks;
	}

	/**
	 * Returns trellis, building by generator or parityChack matrix, as given in <code>byGenerator</code>.
	 * @param byGenerator
	 * @return
	 * @throws SpanFormException
	 */
	public ITrellis getTrellis(boolean byGenerator) throws SpanFormException {
		ITrellis trellis;
		if (byGenerator) {
			ITrellis implicitTrellis = new ConvCodeTrellis(spanForm());
			try {
				trellis = new LightTrellis(implicitTrellis);
			} catch (IllegalArgumentException e) {
				logger.debug("Failed to build explicit trellis, implicit will be used: {}", e.getMessage());
				trellis = implicitTrellis;
			}
		} else {
			trellis = Trellises.trellisFromParityCheckHR(checkMatr);
			MinDistance.computeDistanceMetrics((Trellis)trellis);
		}
		
		return trellis;
	}
	
	public ITrellis getTrellis() throws SpanFormException {
		if (trellis == null) {
			if (checkMatr == null) {
				ITrellis implicitTrellis = new ConvCodeTrellis(spanForm());
			//	try {
			//		trellis = new LightTrellis(implicitTrellis);
			//	} catch (Exception e) { //IllegalArgumentException e) {
				//	logger.debug("Failed to build explicit trellis, implicit will be used: {}", e.getMessage());
					trellis = implicitTrellis;
			//	}
			} else {				
				trellis = Trellises.trellisFromParityCheckHR(checkMatr);
				MinDistance.computeDistanceMetrics((Trellis)trellis);
			}
		}
		return trellis;
	}
	
	public int getFreeDist() throws SpanFormException {
		if (freeDist == -1) {
			freeDist = MinDistance.findFreeDist(this);
		}
		return freeDist;
	}
	
	// TODO: заменить на умный способ посчитать freeDist каким-нибудь методом (например, передав метод через интерфейс).
	public void setFreeDist(int freeDist) {
		this.freeDist = freeDist;
	}
	
//	public int getFreeDistanceByVA()
//	{
//		if(freeDist != -1)
//		{
//			return freeDist;
//		}
//		
//		ZTCode zt = new ZTCode(this, delay);
//		
//		freeDist = zt.getMinDistByTrellis();
//		return freeDist;
//	}

	@Override
	public BitArray encodeSeq(BitArray infSeq) {
		if (infSeq.getFixedSize() % getK() != 0) {
			throw new IllegalArgumentException();
		}
		
		int wordsNumber = infSeq.getFixedSize() / getK();
		BitArray codeSeq = new BitArray(getN() * wordsNumber);
		for (int i = 0; i < wordsNumber; ++i) {
			BitArray codeWord = new BitArray(getN());
			
			Matrix G[] = getGenBlocks();

			int beginPos = Math.max(0, i - delay) * getK(); // позиция младшего бита, участвующего в текущей сумме
			int endPos = i * getK() + getK() - 1; // позиция старшего бита, участвующего в сумме
			
			for (int bitPos = infSeq.nextSetBit(beginPos); bitPos <= endPos && bitPos >= 0; bitPos = infSeq.nextSetBit(bitPos + 1)) {
				int matrixIndex = (endPos - bitPos) / getK();
				int rowIndex = getK() - 1  - (endPos - bitPos) % getK();
				codeWord.xor(G[matrixIndex].getRow(rowIndex));
			}

			for (int j = 0; j < getN(); ++j) {
				codeSeq.set(i * getN() + j, codeWord.get(j));
			}
		}
		
		return codeSeq;
	}
		
}
