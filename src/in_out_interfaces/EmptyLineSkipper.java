package in_out_interfaces;

import java.util.Scanner;

public class EmptyLineSkipper {

	private Scanner scanner;
	
	EmptyLineSkipper(Scanner scanner)
	{
		this.scanner = scanner;
	}
	
	public String nextLine()
	{
		String line;
		
		while((line = scanner.nextLine()).isEmpty()) {}
		
		return line;
	}
}
