package org.fastcatsearch.ir.index;

import java.util.ArrayList;

public class IndexWriteInfoList extends ArrayList<IndexWriteInfo>{

	private int docSize;
	
	public IndexWriteInfoList(){
	}
	
	public void setDocumentSize(int docSize){
		this.docSize = docSize;
	}
	
	public int getDocumentSize(){
		return docSize;
	}
}
