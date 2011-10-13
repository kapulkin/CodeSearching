package search_procedures.conv_codes;

import codes.ConvCode;

public interface ConvCodeListener {
	public void codeFound(ConvCode code);
	public void searchFinished();
}
