package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;

public abstract class AbstractPostingReader implements PostingReader {

	protected CharVector term;
	protected int termPosition;
	protected int weight;
	
	
	public AbstractPostingReader(CharVector term, int termPosition, int weight) {
		this.term = term;
		this.termPosition = termPosition;
		this.weight = weight;
	}

	@Override
	public int weight() {
		return weight;
	}

	@Override
	public int termPosition() {
		return termPosition;
	}

	@Override
	public CharVector term() {
		return term;
	}

}
