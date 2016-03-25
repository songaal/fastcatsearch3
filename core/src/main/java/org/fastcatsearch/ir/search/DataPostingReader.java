package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;

import java.io.IOException;

public class DataPostingReader extends AbstractPostingReader {

	private int postingPointer;
	private PostingDoc[] dataList;
	private int dataLength;
	
	public DataPostingReader(CharVector term, int termPosition, int weight, PostingDoc[] dataList, int dataLength){
		this(term, termPosition, weight, dataList, dataLength, 0);
	}
	public DataPostingReader(CharVector term, int termPosition, int weight, PostingDoc[] dataList, int dataLength, int segmentDocumentCount){
		super(term, termPosition, weight, segmentDocumentCount);
		this.dataList = dataList;
		this.dataLength = dataList != null ? dataLength : 0;
	}
	
	@Override
	public int size() {
		return dataLength;
	}

	@Override
	public boolean hasNext() throws IOException {
		return postingPointer < dataLength;
	}

	@Override
	public PostingDoc next() throws IOException {
		if(postingPointer < dataLength){
			return dataList[postingPointer++];
		}
		return null;
	}

	@Override
	public void close() {
		dataList = null;
	}

}
