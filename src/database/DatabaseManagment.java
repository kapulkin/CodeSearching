package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import codes.ConvCode;

import search_procedures.CodesFromArticleSearcher;

public class DatabaseManagment {
	public static void fillArticleCodesTable(int freeDist) throws IOException, SQLException {
		ArrayList<ConvCode> articleCodes = CodesFromArticleSearcher.readArticleCodes(new FileInputStream("res/articleCodes" + freeDist + ".txt"));
		
		for (ConvCode code : articleCodes) {
			code.setFreeDist(freeDist);
			CodesDatabase.addConvCode(code, CodesDatabase.articleConvCodesTable);
		}
	}
}