package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.TermDoc;
import org.fastcatsearch.ir.search.TermDocCollector;
import org.fastcatsearch.ir.search.CompositeTermDoc;

public class TermDocsTreeNode extends NodeReader {

	private CompositeTermDoc termDocs;
	private int pos;
	private int count;
	private int queryPosition;
	private boolean isSynonym;
	private TermDoc termDoc;
	
	public TermDocsTreeNode(CompositeTermDoc termDocs, int queryPosition) {
		this(termDocs, queryPosition, false);
	}
	
	public TermDocsTreeNode(CompositeTermDoc termDocs, int queryPosition, boolean isSynonym) {
		this.termDocs = termDocs;
		this.queryPosition = queryPosition;
		this.isSynonym = isSynonym;
		this.count = termDocs.count();
	}

	@Override
	public int next() {
		if (pos < count) {
			termDoc = termDocs.termDocList()[pos++];
			return termDoc.docNo();
		}
		return -1;
	}

	@Override
	public void fill(TermDocCollector termDocCollector) {
		termDocCollector.add(termDocs.term(), termDoc, queryPosition, isSynonym);
	}
}
