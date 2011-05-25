package search_procedures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import trellises.Trellis;
import trellises.Trellises;

import math.Poly;
import math.PolyMatrix;

import codes.ConvCode;
import codes.MinDistance;
import database.CodesDatabase;

public class CodesFromArticleSearcher {
	Map<Integer, ArrayList<ConvCode>> codesFound = new HashMap<Integer, ArrayList<ConvCode>>();
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void searchCodes(int lowerFreeDist) throws IOException, SQLException {
		logger.info("free distance = " + lowerFreeDist);
		
		codesFound.put(lowerFreeDist, new ArrayList<ConvCode>());
		
		if (lowerFreeDist == 3) {
			for (ConvCode code : CodesDatabase.getConvCodes(-1, -1, -1, lowerFreeDist, CodesDatabase.articleConvCodesTable)) {
				logger.debug("v = " + code.getDelay() + ", k = " + code.getK());

				FreeDist3CCEnumerator enumerator = new FreeDist3CCEnumerator(code.getDelay(), code.getK());
				while (enumerator.hasNext()) {
					ConvCode newCode = enumerator.next();
					sortColumnsLexicographical(newCode.parityCheck());

					Trellis trellis = Trellises.trellisFromParityCheckHR(newCode.parityCheck());
					MinDistance.computeDistanceMetrics(trellis);
					int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN() * (code.getDelay() + 1));
					newCode.setFreeDist(freeDist);
					
					CodesDatabase.addConvCode(newCode, CodesDatabase.convCodesTable);
					if (code.parityCheck().equals(newCode.parityCheck())) {
						logger.info("Code from article is found:\n" + code.parityCheck());
						codesFound.get(lowerFreeDist).add(newCode);
					}
				}
			}
		} else if (lowerFreeDist == 4) {
			for (ConvCode code : CodesDatabase.getConvCodes(-1, -1, -1, lowerFreeDist, CodesDatabase.articleConvCodesTable)) {
				logger.debug("v = " + code.getDelay() + ", k = " + code.getK());

				FreeDist4CCEnumerator enumerator = new FreeDist4CCEnumerator(code.getDelay(), code.getK());
				while (enumerator.hasNext()) {
					ConvCode newCode = enumerator.next();
					sortColumnsLexicographical(newCode.parityCheck());

					Trellis trellis = Trellises.trellisFromParityCheckHR(newCode.parityCheck());
					MinDistance.computeDistanceMetrics(trellis);
					int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, code.getN());
					newCode.setFreeDist(freeDist);
					
					CodesDatabase.addConvCode(newCode, CodesDatabase.convCodesTable);
					if (code.parityCheck().equals(newCode.parityCheck())) {
						logger.info("Code from article is found:\n" + code.parityCheck());
						codesFound.get(lowerFreeDist).add(newCode);
					}
				}
			}
		} else {
			ArrayList<ConvCode> codes = CodesDatabase.getConvCodes(-1, -1, -1, lowerFreeDist, CodesDatabase.articleConvCodesTable);
			Map<Integer, ArrayList<ConvCode>> codesMap = new TreeMap<Integer, ArrayList<ConvCode>>();
			
			for (ConvCode code : codes) {
				if (codesMap.get(code.getDelay()) == null) {
					codesMap.put(code.getDelay(), new ArrayList<ConvCode>());
				}
				codesMap.get(code.getDelay()).add(code);				
			}
			
			for (int delay : codesMap.keySet()) {
				logger.debug("v = " + delay);
				HighRateCCEnumerator enumerator = new HighRateCCEnumerator(delay, lowerFreeDist);
				
				ConvCode newCode;
				while ((newCode = enumerator.next()) != null) {
					sortColumnsLexicographical(newCode.parityCheck());

					Trellis trellis = Trellises.trellisFromParityCheckHR(newCode.parityCheck());
					MinDistance.computeDistanceMetrics(trellis);
					int freeDist = MinDistance.findMinDistWithBEAST(trellis, 0, newCode.getN() * (newCode.getDelay() + 1));
					newCode.setFreeDist(freeDist);
					
					CodesDatabase.addConvCode(newCode, CodesDatabase.convCodesTable);
					for (ConvCode code : codesMap.get(delay)) {
						if (code.parityCheck().equals(newCode.parityCheck())) {
							logger.info("Code from article is found:\n" + code.parityCheck());
							codesFound.get(lowerFreeDist).add(newCode);
						}
					}
				}
			}
		}

	}
	
	public Map<Integer, ArrayList<ConvCode>> getCodesFound() {
		return codesFound;
	}
	
	public static ArrayList<ConvCode> readArticleCodes(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		// шаблон строки: '#комментарий' или 'число число восьмеричное_число (, восьмеричное_число)+'
//		Pattern pattern = Pattern.compile("#.*|(\\s*([1-9]\\d*\\s+){2}[0-7]+(\\s*,\\s*[0-7]+)+)");
		Pattern pattern = Pattern.compile("\\s* ([1-9]\\d* \\s+){2} [0-7]+ (\\s* , \\s* [0-7]+)+ \\s*", Pattern.COMMENTS);
//		Pattern pattern = Pattern.compile("\\s*([1-9]\\d*\\s+){2}[0-7]+(\\s*,\\s*[0-7]+)+");
		
		ArrayList<ConvCode> codes = new ArrayList<ConvCode>();
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().charAt(0) == '#') {
				continue;
			}

			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				throw new IOException("The stream doesn't matches the pattern.");
			}

			StringTokenizer tokenizer = new StringTokenizer(line, "\t ,");
			int v = Integer.parseInt(tokenizer.nextToken());
			int infoBitsNumber = Integer.parseInt(tokenizer.nextToken());
			
			PolyMatrix checkMatrix = new PolyMatrix(1, infoBitsNumber + 1);
			for (int i = 0; i < infoBitsNumber + 1; ++i) {
				if (!tokenizer.hasMoreTokens()) {
					throw new IOException("There is not enought vectors for the matrix: " + i + ", " + (infoBitsNumber + 1));
				}
				long polyBits = Long.parseLong(tokenizer.nextToken(), 8);
				boolean polyCoeffs[] = new boolean[Long.SIZE];
				for (int power = 0; power < polyCoeffs.length; ++power) {
					polyCoeffs[power] = (polyBits & (1L << power)) != 0;
				}
				Poly poly = new Poly(polyCoeffs);

				if (poly.getDegree() > v) {
					throw new IOException("The polynomial degree is too high: " + poly.getDegree() + ", " + v);
				}
				checkMatrix.set(0, i, poly);
			}
			sortColumnsLexicographical(checkMatrix);
			codes.add(new ConvCode(checkMatrix, false));
		}
		
		return codes;
	}
	
	private static void sortColumnsLexicographical(PolyMatrix matrix) {
		for (int i = 0; i < matrix.getColumnCount(); ++i) {
			for (int j = i + 1; j < matrix.getColumnCount(); ++j) {
				if (matrix.get(0, i).compareTo(matrix.get(0, j)) > 0) {
					Poly tmp = matrix.get(0, i);
					matrix.set(0, i, matrix.get(0, j));
					matrix.set(0, j, tmp);
				}
			}
		}
	}
}
