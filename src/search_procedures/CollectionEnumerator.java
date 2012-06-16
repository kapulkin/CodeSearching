package search_procedures;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;


import codes.Code;

public class CollectionEnumerator<CodeType extends Code> implements ICodeEnumerator<CodeType> {
	private Iterator<CodeType> iterator;
	private Collection<CodeType> collection;
	
	public CollectionEnumerator(Collection<CodeType> collection) {
		this.collection = collection;
		this.iterator = this.collection.iterator();
	}

	@Override
	public void reset() {
		this.iterator = this.collection.iterator();
	}

	@Override
	public CodeType next() {
		if (!iterator.hasNext())
			return null;
		return iterator.next();
	}

	@Override
	public BigInteger count() {
		return BigInteger.valueOf(collection.size());
	}
}
