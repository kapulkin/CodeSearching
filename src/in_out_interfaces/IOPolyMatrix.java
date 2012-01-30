package in_out_interfaces;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.Poly;
import math.PolyMatrix;

public class IOPolyMatrix {

	public static void writeMatrix(PolyMatrix mat, OutputStream os) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		writeMatrix(mat, writer);
	}
	
	public static void writeMatrix(PolyMatrix mat, BufferedWriter writer) throws IOException
	{
		
		for(int i = 0;i < mat.getRowCount();i ++)
		{
			for(int j = 0;j < mat.getColumnCount();j ++)
			{
				Poly p = mat.get(i, j);
				
				if (p.isZero()) {
					writer.write("0  ");
					continue;
				}
				
				if (p.getCoeff(0) == true) {
					writer.write("1");
					if(p.getDegree() > 0) {
						writer.write("+");
					}
				}
				
				if (p.getCoeff(1) == true) {
					writer.write("D");
					if(p.getDegree() > 1) {
						writer.write("+");
					}
				}

				for (int k = 2; k < p.getDegree()+1; ++k) {					
					if (p.getCoeff(k)) {						
						writer.write("D" + k);
						
						if(k < p.getDegree())
						{
							writer.write("+");
						}
					}
				}
				
				writer.write("  ");
			}
						
			writer.newLine();
		}
		
		writer.newLine();
		writer.flush();
	}

	public static void writeMatrixOct(PolyMatrix mat, BufferedWriter writer) throws IOException {
		writeMatrix(mat, 8, writer);
	}
	
	public static void writeMatrix(PolyMatrix mat, int radix, BufferedWriter writer) throws IOException {
		for (int i = 0; i < mat.getRowCount(); ++i) {
			for (int j = 0; j < mat.getColumnCount(); ++j) {
				if (mat.get(i, j).getDegree() >= Long.SIZE) {
					throw new IllegalArgumentException("Doesn't support polynomials with power more then " + Long.SIZE + ".");
				}
				
				long polyBits = 0;
				for (int power = 0; power <= mat.get(i, j).getDegree(); ++power) {
					if (mat.get(i, j).getCoeff(power)) {
						polyBits |= (1L << power);
					}
				}
				
				writer.write(Long.toString(polyBits, radix));
				if (j != mat.getColumnCount() - 1) {
					writer.write(", ");
				}
			}
			writer.newLine();
		}
		writer.newLine();
		writer.flush();
	}
	
	public static PolyMatrix readMatrix(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		return readMatrix(reader);
	}
	
	public static PolyMatrix readMatrix(BufferedReader reader) throws IOException {
		ArrayList<ArrayList<Poly>> polyArrays = new ArrayList<ArrayList<Poly>>();
		
		Pattern pattern = Pattern.compile("(0 | ( (1 | D\\d?) (+D\\d?)* ) (\\s+ | $) )+");
		
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				throw new IOException("The stream doesn't matches the pattern.");
			}
			
			ArrayList<Poly> polies = new ArrayList<Poly>();
			
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				String polyString = tokenizer.nextToken();
				Poly poly = Poly.parsePoly(polyString);
				polies.add(poly);
			}
			
			if (polyArrays.size() > 0 && polyArrays.get(0).size() != polies.size()) {
				throw new IOException("Wrong format of stream.");
			}
			
			polyArrays.add(polies);
		}
		
		PolyMatrix matrix = new PolyMatrix(polyArrays.size(), polyArrays.get(0).size());
		for (int i = 0; i < polyArrays.size(); ++i) {
			matrix.setRow(i, polyArrays.get(i).toArray(new Poly[] {}));
		}
		
		return matrix;
	}

	public static PolyMatrix readMatrixOct(BufferedReader reader) throws IOException {
		return readMatrix(8, reader);
	}
	
	public static PolyMatrix readMatrix(int radix, BufferedReader reader) throws IOException {
		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
			throw new IllegalArgumentException("Radix " + radix + " is not supported.");
		}
		
		ArrayList<ArrayList<Poly>> polyArrays = new ArrayList<ArrayList<Poly>>();
		
		// TODO: протестировать закомменированные ниже 2 строчки и заменить ими следующую незакоментированную
//		String digits = (radix <= 10 ? "[0-" + (radix-1) + "]" : "[0-9A-" + Character.forDigit(radix - 1, radix));
//		Pattern pattern = Pattern.compile(digits + "+ (\\s* , \\s* " + digits + "+)+ \\s*", Pattern.COMMENTS);
		
		Pattern pattern = Pattern.compile("[0-7]+ (\\s* , \\s* [0-7]+)+ \\s*", Pattern.COMMENTS);
		
		String line;
		while ((line = reader.readLine()) != null) {
			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				throw new IOException("The stream doesn't matches the pattern.");
			}
			
			ArrayList<Poly> polies = new ArrayList<Poly>();
			
			StringTokenizer tokenizer = new StringTokenizer(line, "\t ,");
			while (tokenizer.hasMoreTokens()) {
				long polyBits = Long.parseLong(tokenizer.nextToken(), radix);
				boolean polyCoeffs[] = new boolean[Long.SIZE];
				for (int power = 0; power < polyCoeffs.length; ++power) {
					polyCoeffs[power] = (polyBits & (1L << power)) != 0;
				}
				Poly poly = new Poly(polyCoeffs);

				polies.add(poly);
			}
			
			if (polyArrays.size() > 0 && polyArrays.get(0).size() != polies.size()) {
				throw new IOException("Wrong format of stream.");
			}
			
			polyArrays.add(polies);
		}
		
		PolyMatrix matrix = new PolyMatrix(polyArrays.size(), polyArrays.get(0).size());
		for (int i = 0; i < polyArrays.size(); ++i) {
			matrix.setRow(i, polyArrays.get(i).toArray(new Poly[] {}));
		}
		
		return matrix;
	}
}
