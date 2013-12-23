package org.fastcatsearch.job.indexing;

public class IndexingStopException extends Exception {
	public IndexingStopException(){
		super();
	}
	public IndexingStopException(String message){
		super(message);
	}
}
