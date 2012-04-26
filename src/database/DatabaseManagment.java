package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import math.ConvCodeSpanForm.SpanFormException;

import codes.ConvCode;

import search_procedures.conv_codes.CodesFromArticleSearcher;

public class DatabaseManagment {
	public static void fillArticleCodesTable(int freeDist) throws IOException, SQLException, SpanFormException {
		ArrayList<ConvCode> articleCodes = CodesFromArticleSearcher.readArticleCodes(new FileInputStream("res/articleCodes" + freeDist + ".txt"));
		
		for (ConvCode code : articleCodes) {
			code.setFreeDist(freeDist);
			CodesDatabase.addConvCode(code, CodesDatabase.articleConvCodesTable);
		}
	}
}
