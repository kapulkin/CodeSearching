package codes;

import math.BlockMatrix;
import math.Matrix;
import math.MinDistance;

public class TruncatedCode extends BlockCode {
	/**
	 * Сверточный код, по которому был построен данный код
	 */
	protected ConvCode parentCode;
	
	/**
	 * Блочное представление порождающей матрицы
	 */
	protected BlockMatrix blockGenMatr;
	
	int cycles;
	
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

		cycles = L;
		
		parentCode = code;		
		k = code.getK() * (L - L0);
		n = code.getN() * L;		
	}
	
	public TruncatedCode() {
		
	}
	
	@Override
	public Matrix generator() {
		if(genMatr == null) {
			genMatr = blockGenMatrix().breakBlockStructure();
		}
		
		return genMatr;
	}
	
	@Override
	public Matrix parityCheck() {
		if(checkMatr == null) {
			generator();
			return super.parityCheck();
		}
		
		return checkMatr;
	}

	/**
	 * Порождающая матрица в виде блоков
	 * @return порождающая матрица в виде блоков
	 */
	public BlockMatrix blockGenMatrix() {
		if (blockGenMatr == null) {
			int L = cycles;
			int L0 = L - k / parentCode.getK();

			blockGenMatr = new BlockMatrix(L - L0, L, parentCode.getK(), parentCode.getN());			
			
			for (int row = 0; row < L - L0; ++row) {
				for (int power = 0; power < parentCode.getGenBlocks().length; ++power) {
					int col = (row + power) % L;
					blockGenMatr.set(row, col, parentCode.getGenBlocks()[power]);
				}
			}
		}
		return blockGenMatr;
	}

	/**
	 * Возвращает сверточный код, по которому был построен данный. 
	 * @return сверточный код, по которому был построен данный
	 */
	public ConvCode getParentCode() {
		return parentCode;
	}
	
	public int getMinDist() {
		if (minDist == -1) {
			minDist = MinDistance.findMinDist(this);
		}
		
		return minDist;
	}

	/*@Override
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
		
		int[] spanHeads = new int[blockGenMatrix().getTotalRowCount()];
		int[] spanTails = new int[blockGenMatrix().getTotalRowCount()];
				
		for (int rowBlock = 0;rowBlock < blockGenMatrix().getRowCount();rowBlock ++) {			
			for (int i = 0;i < parentCode.getK();i ++) {
				spanHeads[rowBlock * parentCode.getK() + i] = (pattSpanForm.getHead(i) + rowBlock * parentCode.getN()) % (blockGenMatr.getTotalColumnCount());
				spanTails[rowBlock * parentCode.getK() + i] = (pattSpanForm.getTail(i) + rowBlock * parentCode.getN()) % (blockGenMatr.getTotalColumnCount());
			}

			for (int power = 0; power < dividedPattern.getColumnCount(); ++power) {
				int colBlock = (rowBlock + power) % blockGenMatrix().getColumnCount();
				blockGenMatrix().set(rowBlock, colBlock, dividedPattern.get(0, power));
			}
		}
		
		genMatr = blockGenMatrix().breakBlockStructure();
		genSpanForm = new SpanForm(genMatr, spanHeads, spanTails);		

		return genSpanForm;
	}/**/
}
