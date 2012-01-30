package math;

import java.util.ArrayList;

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
	 * Циклический сдвиг строки вправо
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
	 * Строит решетку блокового кода в явном виде с рассчитанной нулевой метрикой. Метрикой ребер выступает их весовая функция.
	 * @param code блоковый код.
	 * @return решетка блокового кода с рассчитанной метрикой
	 */
	public static Trellis buildExplicitTrellis(BlockCode code) {
		return Trellises.buildExplicitTrellis(new BlockCodeTrellis(code.getGeneratorSpanForm()));
	}	
	
	public static BitArray[] buildCosetsWithBigWeight(BlockCode code, int weight) {		
		BitArray cosetCharacteristicVector = new BitArray(1 << (code.getN() - code.getK()));
		ArrayList<BitArray> badCosetsFront = new ArrayList<BitArray>();
		Matrix parityCheck = code.parityCheck().transpose();
		
		cosetCharacteristicVector.set(0);
		badCosetsFront.add(new BitArray(code.getN() - code.getK()));
		for (int w = 1;w <= weight; ++w) {
			ArrayList<BitArray> newFront = new ArrayList<BitArray>();
			
			for (BitArray coset : badCosetsFront) {
				for (int i = 0;i < code.getN(); ++i) {
					BitArray neighbour = new BitArray(coset);
					
					neighbour.and(parityCheck.getRow(i));
					int neighbourIndex = 0;
					
					for (int b = 0;b < code.getN() - code.getK(); ++b) {
						if (neighbour.get(b)) {
							neighbourIndex ^= (1 << b); 
						}
					}
					
					if (!cosetCharacteristicVector.get(neighbourIndex)) {
						cosetCharacteristicVector.set(neighbourIndex);
						newFront.add(neighbour);
					}
				}
			}
			
			badCosetsFront = newFront;
		}
		
		ArrayList<BitArray> cosets = new ArrayList<BitArray>();
		for (int cosetIndex = 0;cosetIndex < cosetCharacteristicVector.getFixedSize(); ++cosetIndex) {
			if (!cosetCharacteristicVector.get(cosetIndex)) {
				BitArray coset = new BitArray(code.getN() - code.getK());
				
				for (int i = 0;i < code.getN() - code.getK(); ++i) {
					coset.set(i, cosetIndex & (1 << i));
				}
				
				cosets.add(coset);
			}
		}
		
		return cosets.toArray(new BitArray[0]);
	}
	
	public static BitArray findCosetLeader(BlockCode code, BitArray syndrome) {
		Matrix parityCheck = code.parityCheck();
		Integer[] permSubmatrix = MathAlgs.getPermutationSubmatrix(parityCheck);
		
		if (permSubmatrix.length < code.getN() - code.getK()) {
			permSubmatrix = new Integer[code.getN() - code.getK()];
			for(int i = 0;i < parityCheck.getRowCount();i ++) {
				int uniqueInd = parityCheck.getRow(i).nextSetBit(0);
				
				for(int j = 0;j < parityCheck.getRowCount();j ++) {
					if(i == j) continue;
					
					if(parityCheck.get(j, uniqueInd) == true) {
						parityCheck.getRow(j).xor(parityCheck.getRow(i));
						syndrome.set(j, syndrome.get(j) ^ syndrome.get(i));
					}
				}
							
				permSubmatrix[i] = uniqueInd;
			}
		}
		
		BitArray cosetWord = new BitArray(code.getN());
		
		for (int i = 0;i < syndrome.getFixedSize(); ++i) {
			if (syndrome.get(i)) {
				cosetWord.set(permSubmatrix[i]);
			}
		}
		
		
		
		return cosetWord;
	}
}
