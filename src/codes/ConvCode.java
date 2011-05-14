package codes;

import smith_decomposition.SmithDecomposition;
import math.BitArray;
import math.BlockMatrix;
import math.ConvCodeAlgs;
import math.Matrix;
import math.PolyMatrix;

/**
 * 
 * @author fedor
 *
 */
public class ConvCode implements Code{
	
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
			delay = ConvCodeAlgs.getHigherDegree(genMatr);
		}else{
			checkMatr = matrix;
			adderCount = checkMatr.getColumnCount();
			regCount = adderCount - checkMatr.getRowCount();
			delay = ConvCodeAlgs.getHigherDegree(checkMatr);
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
				genMatr = new PolyMatrix(genBlocks[0].getRowCount(), genBlocks[0].getColumnCount());
				for (int row = 0; row < genMatr.getRowCount(); ++row) {
					for (int column = 0; column < genMatr.getColumnCount(); ++column) {
						for (int power = 0; power < genBlocks.length; ++power) {
							genMatr.get(row, column).setCoeff(power, genBlocks[power].get(row, column));
						}
					}
				}
			} else {
				genMatr = ConvCodeAlgs.getOrthogonalMatrix(new SmithDecomposition(checkMatr));
			}
		}
		return genMatr;
	}
	
	/**
	 * 
	 * @return Проверочная матрица в полиномиальном представлении
	 */
	public PolyMatrix checkMatrix()
	{
		if (checkMatr == null) {
			checkMatr = ConvCodeAlgs.getOrthogonalMatrix(new SmithDecomposition(genMatr));
		}
		return checkMatr;
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
	
	public int getFreeDist() {
		return freeDist;
	}
	
	// TODO: заменить на умный способ посчитать freeDist каким-нибудь методом (например, передав метод через интерфейс).
	public void setFreeDist(int freeDist) {
		this.freeDist = freeDist;
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
