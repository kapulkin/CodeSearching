package search_procedures.block_codes;

import in_out_interfaces.IOConvCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.ConvCode;
import search_procedures.conv_codes.IConvCodeEnumerator;

public class FileCCEnumerator implements IConvCodeEnumerator {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Scanner scanner;
	private String filename;
	
	public FileCCEnumerator(String fileName) throws FileNotFoundException{
		this.filename = fileName;
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

}
