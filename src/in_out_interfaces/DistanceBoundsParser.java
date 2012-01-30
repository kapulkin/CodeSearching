package in_out_interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_procedures.block_codes.SearchMain;

public class DistanceBoundsParser {	
	static final private Logger logger = LoggerFactory.getLogger(SearchMain.class);
	private static Pattern bndPattern = Pattern.compile("a_target=\"bounds\"_title=\"n=\\d*,_k=\\d*\"_class=\"tab\"_href=\"BKLC\\.php\\?q=2&amp;n=\\d*&amp;k=\\d*\"_\\d*-?\\d+? ");
	
	private static String readWholeFile(String filename) throws FileNotFoundException {
		Scanner scanner = new Scanner(new FileReader(new File(filename)));
		StringBuilder text = new StringBuilder();
		
		while (scanner.hasNextLine()) {
			text.append(scanner.nextLine());
		}
		
		/*String[] pieces = text.toString().replace(' ', '_').replace('>', '_').split("[<>]");
		
		text = new StringBuilder();
		for (String piece : pieces) {
			text.append(" " + piece);
		}/**/
		
		return text.toString().replace(' ', '_').replace('>', '_').replace('<', ' ');
	}
	
	public static int[][] parse(boolean upperBounds) throws FileNotFoundException {
		int[][] distanceBounds = new int[257][257];
		String content = readWholeFile("block_codes_table.txt");
		Matcher bndMatcher = bndPattern.matcher(content);		
		
		while (bndMatcher.find()) {
			String matched = bndMatcher.group();
			String[] numbers = matched.split("[^\\d]+", 1000);
			int n = Integer.parseInt(numbers[1]);
			int k = Integer.parseInt(numbers[2]);
			int d;
			
			try{
				d = Integer.parseInt(numbers[upperBounds ? 7 : 6]);
			}catch(Exception e) {
				d = Integer.parseInt(numbers[6]);
			}
			
			distanceBounds[k][n] = d;
		}
		
		return distanceBounds;
	}
}
