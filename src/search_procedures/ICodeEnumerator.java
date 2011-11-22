package search_procedures;

import codes.Code;

public interface ICodeEnumerator<SomeCode extends Code> {

	public void reset();
	public SomeCode next();
	
}
