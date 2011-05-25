package codes;

import math.BlockCodeAlgs;
import math.BlockMatrix;
import math.Matrix;
import math.SpanForm;

public class TruncatedCode extends BlockCode {
	/**
	 * Сверточный код, по которому был построен данный код
	 */
	protected ConvCode parentCode;
	
	/**
	 * Блочное представление порождающей матрицы
	 */
	protected BlockMatrix blockGenMatr;
	
	/**
	 * Строит усеченный код со скоростью <em>k/n</em>, где <em>k = b * (L - L0), n = c * L, 
	 * L = delay + 1 + scaleDelta, L0 = tailTruncation</em>.
	 * 
	 * При <code>tailTruncation</code> = 0 получаем tailbiting-код, при 
	 * <code>tailTrancation</code> = delay получаем zero-tail-код.
	 * @param code сверточный код
	 * @param tailTruncation L0 - степень нейтрализации хвтостов
	 * @param scaleDelta L - delay - 1
	 */
	public TruncatedCode(ConvCode code, int tailTruncation, int scaleDelta) {
		int L = code.getDelay() + 1 + scaleDelta;
		int L0 = tailTruncation;
		
		parentCode = code;
		blockGenMatr = new BlockMatrix(L - L0, L, code.getK(), code.getN());
		k = code.getK() * (L - L0);
		n = code.getN() * L;
		
		for (int row = 0; row < L - L0; ++row) {
			for (int power = 0; power <= code.getDelay(); ++power) {
				int col = (row + power) % L;
				blockGenMatr.set(row, col, code.getGenBlocks()[power]);
			}
		}
	}
	
	@Override
	public Matrix generator()
	{
		if(genMatr == null)
		{
			genMatr = blockGenMatr.breakBlockStructure();
		}
		
		return genMatr;
	}

	/**
	 * Порождающая матрица в виде блоков
	 * @return порождающая матрица в виде блоков
	 */
	public BlockMatrix blockGenMatrix() {
		return blockGenMatr;
	}

	/**
	 * Возвращает сверточный код, по которому был построен данный. 
	 * @return сверточный код, по которому был построен данный
	 */
	public ConvCode getParentCode()
	{
		return parentCode;
	}

	@Override
	public SpanForm getGeneratorSpanForm() {
		if (genSpanForm != null) {
			return genSpanForm;
		}
		
		// матрица вида Go...Gm - паттерн, циклическими сдвигами которого получается порождающая матрица
		Matrix pattern = (new BlockMatrix(parentCode.getGenBlocks())).breakBlockStructure();
		
		// спеновая форма паттерна
		SpanForm pattSpanForm = BlockCodeAlgs.toSpanForm(pattern);
		
		// разбиение паттерна на блоки
		BlockMatrix dividedPattern = new BlockMatrix(pattern, parentCode.getK(), parentCode.getN());
		
		int[] spanHeads = new int[blockGenMatr.getTotalRowCount()];
		int[] spanTails = new int[blockGenMatr.getTotalRowCount()];
				
		for(int rowBlock = 0;rowBlock < blockGenMatr.getRowCount();rowBlock ++)
		{			
			for(int i = 0;i < parentCode.getK();i ++)
			{
				spanHeads[rowBlock * parentCode.getK() + i] = (pattSpanForm.getHead(i) + rowBlock * parentCode.getN()) % (blockGenMatr.getTotalColumnCount());
				spanTails[rowBlock * parentCode.getK() + i] = (pattSpanForm.getTail(i) + rowBlock * parentCode.getN()) % (blockGenMatr.getTotalColumnCount());
			}

			for (int power = 0; power < dividedPattern.getColumnCount(); ++power) {
				int colBlock = (rowBlock + power) % blockGenMatr.getColumnCount();
				blockGenMatr.set(rowBlock, colBlock, dividedPattern.get(0, power));
			}
		}
		
		genMatr = blockGenMatr.breakBlockStructure();
		genSpanForm = new SpanForm(genMatr, spanHeads, spanTails);
		genSpanForm.IsTailbiting = true;

		return genSpanForm;
	}
}
