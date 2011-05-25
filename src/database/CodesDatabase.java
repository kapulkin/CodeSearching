package database;

import in_out_interfaces.IOMatrix;
import in_out_interfaces.IOPolyMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import math.Matrix;
import math.PolyMatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codes.BlockCode;
import codes.ConvCode;

public class CodesDatabase {
	public final static String codesDatabase = "codes";
	public final static String convCodesTable = "convCodes";
	public final static String articleConvCodesTable = "articleConvCodes";
	public final static String blockCodesTable = "blockCodes";
	
	private static Connection connection;
	
	private static Logger logger = LoggerFactory.getLogger(CodesDatabase.class);
	
	public static void addConvCode(ConvCode code, String table) throws SQLException, IOException {
		connection = getConnection();
		
		Statement stmt = null;
		String columns = "delay, inf_length, code_length";
		String values = "" + code.getDelay() + ", " + code.getK() + ", " + code.getN();
		if (!code.isGeneratorNull()) {
			columns += ", generator";
			StringWriter writer = new StringWriter();
			IOPolyMatrix.writeMatrixOct(code.generator(), new BufferedWriter(writer));
			values += ", '" + writer + "'"; 
		}
		if (!code.isParityCheckNull()) {
			columns += ", parity_check";
			StringWriter writer = new StringWriter();
			IOPolyMatrix.writeMatrixOct(code.parityCheck(), new BufferedWriter(writer));
			values += ", '" + writer + "'";
		}
		if (code.getFreeDist() > 0) {
			columns += ", free_dist";
			values += ", " + code.getFreeDist();
		}
		
		String query = "INSERT INTO " + codesDatabase + "." + table + "\n" +  
			" (" + columns + ")\n" +
			" VALUES (" + values + ")";
		
		stmt = connection.createStatement();
		stmt.executeUpdate(query);
	}
	
	public static void addBlockCode(BlockCode code, String table) throws SQLException {
		connection = getConnection();
		
		Statement stmt = null;
		String columns = "inf_length, code_length, generator, parity_check" + (code.getMinDist() > 0 ? ", free_dist" : ""); 
		String values = "" + code.getK() + ", " + code.getN();
		if (!code.isGeneratorNull()) {
			columns += ", generator";
			values += ", '" + code.generator() + "'"; 
		}
		if (!code.isParityCheckNull()) {
			columns += ", parity_check";
			values += ", '" + code.parityCheck() + "'";
		}
		if (code.getMinDist() > 0) {
			columns += ", min_dist";
			values += ", " + code.getMinDist();
		}
		String query = "INSERT INTO " + codesDatabase + "." + table + "\n" +  
			" (" + columns + ")\n" +
			" VALUES (" + values + ")";
		
		stmt = connection.createStatement();
		stmt.executeUpdate(query);
	}

	/**
	 * Возвращает коды из таблицы <code>table</code> с заданными параметрами. 
	 * Если какой-то параметр отрицательный, он не используется в запросе.
	 * @param v задержка кодера
	 * @param k длина информационного слова
	 * @param n длина кодового слова
	 * @param freeDist свободное расстояние кода
	 * @param table имя таблицы, в которой производится поиск
	 * @return найденные коды с заданными параметрами
	 * @throws SQLException
	 * @throws IOException
	 */
	public static ArrayList<ConvCode> getConvCodes(int v, int k, int n, int freeDist, String table) throws SQLException, IOException {
		connection = getConnection();
		
		Statement stmt = null;
		String where = new String();
		int whereCount = 0;
		if (v > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "delay = " + v;
		}
		if (k > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "inf_length = " + k;
		}
		if (n > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "code_length = " + n;
		}
		if (freeDist > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "free_dist = " + freeDist;
		}
		String query = "SELECT DISTINCT * FROM " + codesDatabase + "." + table + "\n" +
			where + "\n ORDER BY free_dist DESC"; 
		
		ArrayList<ConvCode> codes = new ArrayList<ConvCode>();
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				ConvCode code = null;
				
				int resFreeDist = rs.getInt("free_dist");
				
				String str;
				str = rs.getString("generator");
				if (str != null) {
					PolyMatrix generator = IOPolyMatrix.readMatrixOct(new BufferedReader(new StringReader(str)));
					code = new ConvCode(generator, true);
				}
				
				if (code == null) {
					str = rs.getString("parity_check");
					if (str != null) {
						PolyMatrix parityCheck = IOPolyMatrix.readMatrixOct(new BufferedReader(new StringReader(str)));
						code = new ConvCode(parityCheck, false);
					}
				}
				
				if (resFreeDist > 0) {
					code.setFreeDist(resFreeDist);
				}
				codes.add(code);
			}
		} catch (SQLException e ) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		logger.debug(codes.size() + " codes.");
		
		return codes;
	}

	public static ArrayList<BlockCode> getBlockCodes(int k, int n, int minDist, String table) throws SQLException, IOException {
		connection = getConnection();
		
		Statement stmt = null;
		String where = new String();
		int whereCount = 0;
		if (k > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "inf_length = " + k;
		}
		if (n > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "code_length = " + n;
		}
		if (minDist > 0) {
			where += (whereCount == 0 ? " WHERE " : " AND ") + "min_dist = " + minDist;
		}
		String query = "SELECT DISTINCT * FROM " + codesDatabase + "." + table + "\n" + 
			where + "\n ORDER BY min_dist DESC";

		ArrayList<BlockCode> codes = new ArrayList<BlockCode>();
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				BlockCode code = null;
				
				int resMinDist = rs.getInt("min_dist");
				
				String str;
				str = rs.getString("generator");
				if (str != null) {
					Matrix generator = IOMatrix.readMatrix(new BufferedReader(new StringReader(str)));
					code = new BlockCode(generator, true);
				}
				
				if (code == null) {
					str = rs.getString("parity_check");
					if (str != null) {
						Matrix parityCheck = IOMatrix.readMatrix(new BufferedReader(new StringReader(str)));
						code = new BlockCode(parityCheck, false);
					}
				}
				
				if (resMinDist > 0) {
					code.setMinDist(resMinDist);
				}
				codes.add(code);
			}
		} catch (SQLException e ) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

		logger.debug(codes.size() + " codes.");

		return codes;
	}

	public static Connection getConnection() throws SQLException {
		if (connection == null) {
			connection = createConnection(codesDatabase, "root", "parol");
		}
		return connection;
	}
	
	private static Connection createConnection(String dbName, String user, String password) throws SQLException {
		Properties connectionProperties = new Properties();
		
		connectionProperties.put("user", user);
		connectionProperties.put("password", password);
		
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dbName, connectionProperties);
		
		return connection;
	}
}
