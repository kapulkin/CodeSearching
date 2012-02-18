package search_procedures;

import java.math.BigInteger;

import codes.Code;

public interface ICodeEnumerator<SomeCode extends Code> {

	public void reset();
	public SomeCode next();
	public BigInteger count();
	
}
