package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.TermDoc;
import org.fastcatsearch.ir.search.CompositeTermDoc;

public class TermDocsReader implements Comparable<TermDocsReader> {
	private CompositeTermDoc termDocs;
	private int pos = -1;
	
	public TermDocsReader(CompositeTermDoc termDocs) {
		this.termDocs = termDocs;
	}

	public boolean next() {
		return ++pos < termDocs.count();
	}
	
	public TermDoc read() {
		return termDocs.termDocList()[pos];
	}
	
	public int compareTo(TermDocsReader o) {
		return termDocs.termDocList()[pos].docNo() - o.termDocs.termDocList()[o.pos].docNo();
	}
}
