package search_procedures.conv_codes;

import codes.Code;

public interface ICodeEnumerator<SomeCode extends Code> {

	public void reset();
	public SomeCode next();
	
}
