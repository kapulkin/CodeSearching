package search_procedures.block_codes;

import codes.BlockCode;

public abstract class BlockCodesBaseSearchScheme {
	protected ICandidateEnumerator candidateEnum;
	
	public void setCandidateEnumerator(ICandidateEnumerator candidateEnum) {
		this.candidateEnum = candidateEnum;
	}
	
	public BlockCode findNext() {
		BlockCode candidate;
		
		while((candidate = candidateEnum.next()) != null) {
			BlockCode processedCode = process(candidate);
			
			if(processedCode != null) {
				return processedCode;
			}
		}
		
		return null;
	}
	
	protected abstract BlockCode process(BlockCode candidate);
	
}
