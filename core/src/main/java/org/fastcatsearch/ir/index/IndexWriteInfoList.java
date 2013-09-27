package org.fastcatsearch.ir.index;

import java.util.ArrayList;

/**
 * 색인후 타 노드에 색인파일을 전송하기 위한 색인파일정보를 담고 있다.  
 * */
public class IndexWriteInfoList extends ArrayList<IndexWriteInfo>{

	private static final long serialVersionUID = -3321132694001199373L;
	
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
