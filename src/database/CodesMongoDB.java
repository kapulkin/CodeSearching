package database;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import math.BitArray;
import math.BitSet;
import math.ConvCodeSpanForm.SpanFormException;
import math.Matrix;
import math.Poly;
import math.PolyMatrix;

import codes.BlockCode;
import codes.ConvCode;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class CodesMongoDB {
	private static Mongo mongo;
	private DB db;
	
	public CodesMongoDB(String dbname) throws UnknownHostException, MongoException {
		if (mongo == null) {
			mongo = new Mongo();
		}
		
		db = mongo.getDB(dbname);
	}
	
	public void clear() {
		Set<String> colls = db.getCollectionNames();
		
		for (String name : colls) {
			DBCollection coll = db.getCollection(name);
			DBCursor cur = coll.find();
			
			while (cur.hasNext()) {
				coll.remove(cur.next());
			}
		}
	}
	
	public void addBlockCode(BlockCode blockCode, boolean gen) {
		DBCollection collection = db.getCollection("codes");
		
		BasicDBObject dbObject = new BasicDBObject();
		Matrix mat =  gen ? blockCode.generator() : blockCode.parityCheck();
		String keyPrefix = gen ? "g" : "h";
		
		dbObject.append("mat", gen ? "G" : "H");
		dbObject.append("k", mat.getRowCount());
		dbObject.append("n", mat.getColumnCount());
		for (int i = 0; i < mat.getRowCount(); ++i) {
			dbObject.append(keyPrefix + i, mat.getRow(i).toString());			
		}
		
		collection.save(dbObject);
	}
	
	public ArrayList<BlockCode> getBlockCodes() {
		DBCollection collection = db.getCollection("codes");
		ArrayList<BlockCode> codes = new ArrayList<BlockCode>();
		DBCursor cur = collection.find();
		
		while (cur.hasNext()) {
			DBObject codeObject = cur.next();
			boolean gen = codeObject.get("mat").toString().equals("G");
			int k = Integer.parseInt(codeObject.get("k").toString());
			int n = Integer.parseInt(codeObject.get("n").toString());
			Matrix matrix = new Matrix(k, n);
			String keyPrefix = gen ? "g" : "h";
			
			for (int i = 0;i < k; ++i) {
				matrix.setRow(i, new BitArray(codeObject.get(keyPrefix + i).toString()));
			}
			
			codes.add(new BlockCode(matrix, gen));
		}
		
		return codes;
	}
	
	public void addConvCode(ConvCode convCode, boolean gen) {
		try {
			DBCollection collection = db.getCollection("b" + convCode.getK() + "c" + convCode.getN() +
					"v" + convCode.getDelay() + "d" + convCode.getFreeDist());
			
			BasicDBObject dbObject = new BasicDBObject();
			PolyMatrix mat =  gen ? convCode.generator() : convCode.parityCheck();
			String keyPrefix = gen ? "g" : "h";
			
			dbObject.append("mat", gen ? "G" : "H");
			for (int i = 0; i < mat.getRowCount(); ++i) {
				for (int j = 0; j < convCode.getN(); ++j) {
					Poly poly = mat.get(i, j);
					dbObject.append(keyPrefix + i + "," + j, Long.toOctalString(poly.getBitSet().getWord(0)));
				}
			}
			
			collection.save(dbObject);
		} catch (SpanFormException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<ConvCode> getConvCodes() {
		Set<String> colls = db.getCollectionNames();
		ArrayList<ConvCode> codes = new ArrayList<ConvCode>();
		
		for (String name : colls) {
			int b, c, v, d;
			int ind1 = 0, ind2 = 0;
			
			try {
				ind1 = name.indexOf('c');
				b = Integer.parseInt(name.substring(1, ind1));
				
				ind2 = name.indexOf('v');
				c = Integer.parseInt(name.substring(ind1 + 1, ind2));
				ind1 = ind2 + 1;
				
				ind2 = name.indexOf('d');
				v = Integer.parseInt(name.substring(ind1, ind2));
				
				d = Integer.parseInt(name.substring(ind2 + 1, name.length()));
				
				codes.addAll(getConvCodes(b, c, v, d));
			} catch (Exception e) {
				
			}
		}
		
		return codes;
	}

	
	public ArrayList<ConvCode> getConvCodes(int b, int c, int v, int d) {
		DBCollection collection = db.getCollection("b" + b + "c" + c + "v" + v + "d" + d);
		ArrayList<ConvCode> codes = new ArrayList<ConvCode>();
		DBCursor cur = collection.find();
		
		while (cur.hasNext()) {
			DBObject codeObject = cur.next();
			boolean gen = (codeObject.get("mat").toString() == "G");
			PolyMatrix matrix = new PolyMatrix(gen ? b : c - b, c);
			
			for (int i = 0;i < matrix.getRowCount(); ++i) {
				for (int j = 0;j < c; ++j) {
					long polyBits = Long.parseLong(codeObject.get((gen ? "g" : "h") + i + "," + j).toString(), 8);
					Poly poly = new Poly(new BitSet(polyBits));
					
					matrix.set(i, j, poly);
				}
			}
			
			codes.add(new ConvCode(matrix, false));
		}
		
		return codes;
	}
}
