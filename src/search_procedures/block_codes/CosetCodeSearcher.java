package search_procedures.block_codes;

import in_out_interfaces.IOMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import math.BitArray;
import math.BlockCodeAlgs;
import math.MLSRandomMethod;
import math.Matrix;
import math.MaximalLinearSubspace;
import codes.BlockCode;
import codes.TruncatedCode;

public class CosetCodeSearcher extends BasicBlockCodesSearcher<BlockCode> {
	private static String COSET_FILENAME = "big_cosets.txt";
	
	private int n;
	private int k;
	private int d;
	
	public CosetCodeSearcher(int k, int n, int d) {
		this.k = k;
		this.n = n;
		this.d = d;
	}
	
	private void writeCosetsToFile(BitArray[] cosets) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(COSET_FILENAME)));
		
		for (int i = 0;i < cosets.length; ++i) {
			for (int j = 0;j < cosets[i].getFixedSize(); ++j) {
				if (cosets[i].get(j)) {
					writer.write("1");
				} else {
					writer.write("0");
				}
			}
			writer.newLine();
		}
		
		writer.flush();
	}
	
	private BitArray[] readCosetsFromFile() throws IOException {
		//BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(COSET_FILENAME)));
		Matrix cosets = IOMatrix.readMatrix(new Scanner(new File(COSET_FILENAME)));
		BitArray[] _cosets = new BitArray[cosets.getRowCount()];
		
		for (int row = 0; row < cosets.getRowCount(); ++row) {
			_cosets[row] = cosets.getRow(row);
		}
		
		return _cosets;
	}
	
	@Override
	protected BlockCode process(BlockCode candidate) {
		BlockCode subcode = candidate;
		
		//subcode = new BlockCode(subcode.generator(), true);
		subcode = BlockCodeAlgs.fitToTheLength(subcode, n);
		
		if (heuristic != null && !heuristic.check(subcode)) {
			return null;
		}		
		
		MaximalLinearSubspace basisSearcher = new MLSRandomMethod(1);		
		int q = k - subcode.getK();		
		
		if (q == 0) {
			return subcode;
		}
		
		BitArray[] bigCosets = BlockCodeAlgs.buildCosetsWithBigWeight(subcode, d);
		
		/*try {
			bigCosets = readCosetsFromFile();
			System.out.println(bigCosets.length);
		}  catch (IOException e) {			
			e.printStackTrace();
		}/**/
		
		/*try {
			writeCosetsToFile(bigCosets);
		} catch (IOException e) {			
			e.printStackTrace();
		}/**/
		
		BitArray[] syndroms = basisSearcher.findBasis(bigCosets, q);
		
		if (syndroms == null) {
			return null;
		}
		
		Matrix newGenerator = new Matrix(k, subcode.getN());
		
		for (int i = 0;i < subcode.getK(); ++i) {
			newGenerator.setRow(i, subcode.generator().getRow(i));
		}		
		
		for (int i = subcode.getK();i < k; ++i) {
			BitArray cosetLeader = BlockCodeAlgs.findCosetLeader(subcode, syndroms[i - subcode.getK()]); 
			newGenerator.setRow(i, cosetLeader);
		}
		
		BlockCode extendedCode = new BlockCode(newGenerator, true); 
		
		if (heuristic != null && !heuristic.check(extendedCode)) {
			return null;
		}		
		
		return extendedCode;
	}

}
