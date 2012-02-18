package search_procedures;

import java.math.BigInteger;

import codes.Code;

public class CodesCounter {
	public static BigInteger count(ICodeEnumerator<? extends Code> codeEnum) {
		int cnt = 0;
		
		while (codeEnum.next() != null) {
			++cnt;
		}
		
		return BigInteger.valueOf(cnt);
	}
}
