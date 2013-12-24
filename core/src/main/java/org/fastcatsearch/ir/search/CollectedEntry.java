package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;

public class CollectedEntry {

	private CharVector term;
	private PostingDoc termDoc;
	private int queryPosition;
	private boolean isSynonym;

	public void set(CharVector term, PostingDoc termDoc, int queryPosition, boolean isSynonym) {
		this.term = term;
		this.termDoc = termDoc;
		this.queryPosition = queryPosition;
		this.isSynonym = isSynonym;
	}

	public CharVector term() {
		return term;
	}

	public void setTerm(CharVector term) {
		this.term = term;
	}

	public PostingDoc termDoc() {
		return termDoc;
	}

	public void setTermDoc(PostingDoc termDoc) {
		this.termDoc = termDoc;
	}

	public int queryPosition() {
		return queryPosition;
	}

	public void setQueryPosition(int queryPosition) {
		this.queryPosition = queryPosition;
	}

	public boolean isSynonym() {
		return isSynonym;
	}

	public void setSynonym(boolean isSynonym) {
		this.isSynonym = isSynonym;
	}
}
