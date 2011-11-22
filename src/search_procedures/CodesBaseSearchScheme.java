package search_procedures;

import codes.Code;

public abstract class CodesBaseSearchScheme<DesiredCode extends Code> {
	protected ICandidateEnumerator<? extends DesiredCode> candidateEnum;
	
	public void setCandidateEnumerator(ICandidateEnumerator<? extends DesiredCode> candidateEnum) {
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
