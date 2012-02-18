package search_procedures;

import java.math.BigInteger;

public interface ICandidateEnumerator<DesiredCode> {

	DesiredCode next();
	BigInteger count();
	
}
