package common;

import java.util.Iterator;

public class ToString {
	public static String arrayToString(Iterable<?> array) {
		Iterator<?> iter = array.iterator();
		
		if (!iter.hasNext()) {
			return "[]";
		}
		String str = "[" + iter.next();
		while (iter.hasNext()) {
			str += ", " + iter.next();
		}
		str += "]";
		
		return str;
	}

	public static String arrayToString(int array[]) {
		if (array.length == 0) {
			return "[]";
		}
		String str = "[" + array[0];
		for (int i = 1; i < array.length; ++i) {
			str += ", " + array[i];
		}
		str += "]";
		
		return str;
	}
}
