package search_procedures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import search_procedures.block_codes.SearchMain;
import codes.Code;

public abstract class CodesBaseSearchScheme<DesiredCode extends Code> {
	protected ICodeEnumerator<? extends DesiredCode> candidateEnum;
	
	public void setCandidateEnumerator(ICodeEnumerator<? extends DesiredCode> candidateEnum) {
		this.candidateEnum = candidateEnum;
	}
	
	public DesiredCode findNext() {
		DesiredCode candidate;				
		
		while((candidate = candidateEnum.next()) != null) {
			DesiredCode processedCode = process(candidate);						
			
			if(processedCode != null) {
				return processedCode;
			}
		}
		
		return null;
	}
	
	protected abstract DesiredCode process(DesiredCode candidate);
	
}
