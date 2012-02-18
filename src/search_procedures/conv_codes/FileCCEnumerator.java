package search_procedures.conv_codes;

import in_out_interfaces.IOConvCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import codes.tests.ConvCodeTest;
import search_procedures.CodesCounter;
import search_procedures.ICodeEnumerator;

public class FileCCEnumerator implements ICodeEnumerator<ConvCode> {
	static final private Logger logger = LoggerFactory.getLogger(FileCCEnumerator.class);
	
	private Scanner scanner;
	private String filename;
	
	public FileCCEnumerator(String fileName) throws FileNotFoundException{
		this.filename = fileName;
		reset();
	}

	@Override
	public ConvCode next() {		
		while (scanner.hasNext()){
			ConvCode code;
			
			try {
				code = IOConvCode.readConvCode(scanner);				
			} catch (IOException e) {				
				e.printStackTrace();
				return null;
			}
							
			return code;
		}
		
		return null;
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
	public BigInteger count() {		
		try {
			return CodesCounter.count(new FileCCEnumerator(filename));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		
		return BigInteger.valueOf(0);
	}

}
