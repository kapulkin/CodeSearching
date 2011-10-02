package search_procedures.conv_codes;

import codes.ConvCode;

public interface IConvCodeEnumerator {

	public void reset();
	public ConvCode next();
	
}
