package common;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoDBSample {
	public static void main(String args[]) {
		try {
			Mongo mongo = new Mongo();
			DB db = mongo.getDB("test");
			
			DBCollection collection = db.getCollection("parent.child.list");
			DBObject dbObject = new BasicDBObject("first", "string").append("second", 10);
			collection.save(dbObject);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}
}/**/
