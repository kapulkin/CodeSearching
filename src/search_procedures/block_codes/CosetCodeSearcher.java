package search_procedures.block_codes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import math.BitArray;
import math.BlockCodeAlgs;
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
	
	@Override
	protected BlockCode process(BlockCode candidate) {
		BlockCode subcode = candidate;
		
		//subcode = BlockCodeAlgs.fitToTheLength(subcode, n);
		
		if (heuristic != null && !heuristic.check(subcode)) {
			return null;
		}		
		
		MaximalLinearSubspace basisSearcher = new MaximalLinearSubspace();		
		int q = k - subcode.getK();		
		
		if (q == 0) {
			return subcode;
		}
		
		BitArray[] bigCosets = BlockCodeAlgs.buildCosetsWithBigWeight(subcode, d);
		
		try {
			writeCosetsToFile(bigCosets);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
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
