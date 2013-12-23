package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;

public class DataPostingReader extends AbstractPostingReader {

	private int postingPointer;
	private PostingDoc[] dataList;
	private int dataLength;
	
	public DataPostingReader(CharVector term, int termPosition, int weight, PostingDoc[] dataList, int dataLength){
		super(term, termPosition, weight);
		this.dataList = dataList;
	}
	
	@Override
	public int size() {
		return dataLength;
	}

	@Override
	public boolean hasNext() {
		return postingPointer < dataLength;
	}

	@Override
	public PostingDoc next() {
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
