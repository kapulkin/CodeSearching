package math;

import trellises.BlockCodeTrellis;
import trellises.Trellis;
import trellises.Trellises;
import codes.BlockCode;

public class BlockCodeAlgs {

	/**
	 * Расчет спэновой формы матрицы. Вычисления ведутся над входной матрицей.
	 * @param matr
	 * @return
	 */
	public static SpanForm toSpanForm(Matrix matr) {
		return toSpanFormQuasicyclic(matr, matr.getColumnCount());
	}
	
	/**
	 * Циклический сдвиг строки
	 * @param row
	 * @param shift
	 * @return
	 */
	private static BitArray rowCyclicShift(BitArray row, int shift) {
		int n = row.fixedSize;
		BitArray shiftedRow = new BitArray(n);
		
		for (int i = 0;i < n; ++i) {
			shiftedRow.set((n + i + shift) % n, row.get(i));
		}
		
		return shiftedRow;
	}
	
	/**
	 * Расчет спэновой формы квазициклической матрицы с периодом period. Вычисления ведутся над входной матрицей.
	 * @param matr
	 * @param period
	 * @return
	 */
	public static SpanForm toSpanFormQuasicyclic(Matrix matr, int period) {
		int[] spanHeads = new int[matr.getRowCount()];
		int[] spanTails = new int[matr.getRowCount()];
		int[] headUniqueIndices = new int[period];
		int[] tailUniqueIndices = new int[period];
		
		for (int i = 0;i < matr.getColumnCount();i ++) {
			headUniqueIndices[i] = -1;
			tailUniqueIndices[i] = -1;
		}
		
		// упорядочение голов
		for (int i = 0;i < matr.getRowCount();i ++) {
			spanTails[i] = matr.getRow(i).previousSetBit(matr.getColumnCount() - 1);
			spanHeads[i] = matr.getRow(i).nextSetBit(0);			
			
			if (spanHeads[i] == -1) {
				throw new IllegalArgumentException("Строки линейно зависимы");
			}
			
			while (true) {
				int currentHead = spanHeads[i];
				int currentUniqueRow = headUniqueIndices[currentHead % period];
				
				if (currentUniqueRow != -1) {
					int shift = (currentHead - spanHeads[currentUniqueRow]);
					BitArray rowToAdd = rowCyclicShift(matr.getRow(currentUniqueRow), shift);
					
					matr.getRow(i).xor(rowToAdd);
					
					if (spanTails[currentUniqueRow] == spanTails[i]) {
						spanTails[i] = matr.getRow(i).previousSetBit(matr.getColumnCount() - 1);
					}else if (spanTails[currentUniqueRow] > spanTails[i]) {
						spanTails[i] = spanTails[currentUniqueRow];
					}
					spanHeads[i] = matr.getRow(i).nextSetBit(0);					
				}else{
					headUniqueIndices[currentHead] = i;
					break;
				}
			}
		}
	
		// упорядочение хвостов
		for (int i = 0;i < matr.getRowCount();i ++) {						
			int indexToVerify = i;
			while (true) {
				int currentTail = spanTails[indexToVerify];
				int currentUniqueRow = tailUniqueIndices[currentTail];
				
				if (currentUniqueRow != -1) {
					if (spanHeads[currentUniqueRow] > spanHeads[indexToVerify]) {			
						int shift = (currentTail - spanTails[currentUniqueRow]);						
						BitArray smallerRow = rowCyclicShift(matr.getRow(currentUniqueRow), shift);
						
						matr.getRow(indexToVerify).xor(smallerRow);
												
						spanTails[indexToVerify] = matr.getRow(indexToVerify).previousSetBit(matr.getColumnCount()-1);
					}else{
						int shift = (spanTails[currentUniqueRow] - currentTail);
						BitArray smallerRow = rowCyclicShift(matr.getRow(indexToVerify), shift);
						
						tailUniqueIndices[currentTail] = indexToVerify;
						
						matr.getRow(currentUniqueRow).xor(smallerRow);						
						spanTails[currentUniqueRow] = matr.getRow(currentUniqueRow).previousSetBit(matr.getColumnCount()-1);
						
						indexToVerify = currentUniqueRow;						
					}
				}else{
					tailUniqueIndices[currentTail] = indexToVerify;
					break;
				}
			}
		}
		
		return new SpanForm(matr, spanHeads, spanTails);
	}

	public static void sortHeads(SpanForm sf) {
		// сортировка выбором
		for (int i = 0;i < sf.getRowCount();i ++) {
			int minHead = Integer.MAX_VALUE;
			int minRow = -1;
			
			for (int j = i;j < sf.getRowCount();j ++) {
				if (sf.getHead(j) < minHead) {
					minHead = sf.getHead(j);
					minRow = j;
				}
			}
			
			if (i != minRow) {
				BitArray iRow = sf.Matr.getRow(i);
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.Matr.setRow(i, sf.Matr.getRow(minRow));
				sf.Matr.setRow(minRow, iRow);
				
				sf.spanHeads[i] = sf.getHead(minRow);
				sf.spanHeads[minRow] = iHead;
				
				sf.spanTails[i] = sf.getTail(minRow);
				sf.spanTails[minRow] = iTail;
			}
		}
	}

	public static void sortTails(SpanForm sf) {
		// сортировка выбором
		for (int i = 0;i < sf.getRowCount();i ++) {
			int minTail = Integer.MAX_VALUE;
			int minRow = -1;
			
			for (int j = i;j < sf.getRowCount();j ++) {
				if (sf.getTail(j) < minTail) {
					minTail = sf.getTail(j);
					minRow = j;
				}
			}
			
			if (i != minRow) {
				BitArray iRow = sf.Matr.getRow(i);
				int iHead = sf.getHead(i), iTail = sf.getTail(i);
				
				sf.Matr.setRow(i, sf.Matr.getRow(minRow));
				sf.Matr.setRow(minRow, iRow);
				
				sf.spanHeads[i] = sf.getHead(minRow);
				sf.spanHeads[minRow] = iHead;
				
				sf.spanTails[i] = sf.getTail(minRow);
				sf.spanTails[minRow] = iTail;
			}
		}
	}

	/**
	 * Строит решетку блокового кода с заполненной нулевой метрикой. Метрикой ребер выступает их весовая функция.
	 * @param code блоковый код.
	 * @return решетка блокового кода с рассчитанной метрикой
	 */
	public static Trellis buildTrellis(BlockCode code) {
		return Trellises.buildExplicitTrellis(new BlockCodeTrellis(code.getGeneratorSpanForm()));
	}
}
