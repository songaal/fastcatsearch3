package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.PostingDoc;
import org.fastcatsearch.ir.search.PostingReader;
import org.fastcatsearch.ir.search.TermDocCollector;

import java.io.IOException;

public class PostingDocsTreeNode extends NodeReader {

	private PostingReader postingReader;
	private boolean isSynonym;
	private PostingDoc postingDoc;
	
	public PostingDocsTreeNode(PostingReader postingReader) {
		this(postingReader, false);
	}
	
	public PostingDocsTreeNode(PostingReader postingReader, boolean isSynonym) {
		this.postingReader = postingReader;
		this.isSynonym = isSynonym;
	}

	@Override
	public int next() throws IOException {
		if(postingReader.hasNext()){
			postingDoc = postingReader.next();
			return postingDoc.docNo();
		}else{
			return -1;
		}
	}

	@Override
	public void fill(TermDocCollector termDocCollector) {
		termDocCollector.add(postingReader.term(), postingDoc, postingReader.termPosition(), isSynonym);
	}

	@Override
	public void close() {
		postingReader.close();
	}
}
