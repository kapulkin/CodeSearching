package search_procedures;

import codes.ConvCode;

public interface ConvCodeListener {
	public void codeFound(ConvCode code);
	public void searchFinished();
}
