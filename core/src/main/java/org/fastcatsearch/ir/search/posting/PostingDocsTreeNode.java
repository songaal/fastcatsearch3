package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.TermDocCollector;
import org.fastcatsearch.ir.search.CompositePostingDoc;

public class PostingDocsTreeNode extends NodeReader {

	private CompositePostingDoc postingDocs;
	private int pos;
	private int count;
	private int queryPosition;
	private boolean isSynonym;
	private PostingDoc postingDoc;
	
	public PostingDocsTreeNode(CompositePostingDoc termDocs, int queryPosition) {
		this(termDocs, queryPosition, false);
	}
	
	public PostingDocsTreeNode(CompositePostingDoc termDocs, int queryPosition, boolean isSynonym) {
		this.postingDocs = termDocs;
		this.queryPosition = queryPosition;
		this.isSynonym = isSynonym;
		this.count = termDocs.count();
	}

	@Override
	public int next() {
		if (pos < count) {
			postingDoc = postingDocs.postingDocList()[pos++];
			return postingDoc.docNo();
		}
		return -1;
	}

	@Override
	public void fill(TermDocCollector termDocCollector) {
		termDocCollector.add(postingDocs.term(), postingDoc, queryPosition, isSynonym);
	}
}
