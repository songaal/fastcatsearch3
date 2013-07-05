package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.CompositePostingDoc;

public class PostingDocsReader implements Comparable<PostingDocsReader> {
	private CompositePostingDoc termDocs;
	private int pos = -1;
	
	public PostingDocsReader(CompositePostingDoc termDocs) {
		this.termDocs = termDocs;
	}

	public boolean next() {
		return ++pos < termDocs.count();
	}
	
	public PostingDoc read() {
		return termDocs.postingDocList()[pos];
	}
	
	public int compareTo(PostingDocsReader o) {
		return termDocs.postingDocList()[pos].docNo() - o.termDocs.postingDocList()[o.pos].docNo();
	}
}
