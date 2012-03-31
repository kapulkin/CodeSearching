package search_procedures.block_codes;

import in_out_interfaces.IOConvCode;
import in_out_interfaces.IOMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

import codes.BlockCode;
import codes.ConvCode;

import search_procedures.CodesCounter;
import search_procedures.ICodeEnumerator;
import search_procedures.conv_codes.FileCCEnumerator;

public class FileBCEnumerator implements ICodeEnumerator<BlockCode> {

	private Scanner scanner;
	private String filename;
	
	public FileBCEnumerator(String fileName) throws FileNotFoundException{
		this.filename = fileName;
		reset();
	}
	
	@Override
	public void reset() {
		try {
			scanner = new Scanner(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
		}			
	}

	@Override
	public BlockCode next() {
		while (scanner.hasNext()){
			BlockCode code;
			
			try {
				int k = scanner.nextInt();				
				code = new BlockCode(IOMatrix.readMatrix(scanner, k), true);				
			} catch (IOException e) {				
				e.printStackTrace();
				return null;
			}
							
			return code;
		}
		
		return null;
	}

	@Override
	public BigInteger count() {
		try {
			return CodesCounter.count(new FileBCEnumerator(filename));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		
		return BigInteger.valueOf(0);
	}

}
