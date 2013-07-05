package org.fastcatsearch.ir.search;

public class PostingDoc {
	private int docNo;
	private int tf;
	private int[] positions;
	
	public PostingDoc(int docNo, int tf){
		this(docNo, tf, null);
	}
	public PostingDoc(int docNo, int tf, int[] positions){
		this.docNo = docNo;
		this.tf = tf;
		this.positions = positions;
	}
	
	public int docNo(){
		return docNo;
	}
	
	public int tf(){
		return tf;
	}
	
	public void setTf(int tf){
		this.tf = tf;
	}
	
	///prefix검색등 여러 termdoc이 하나의 단어에서 검색되었을때 사용된다.
	public void addTf(int tf){
		this.tf += tf;
	}
	
	
	public int[] positions(){
		return positions;
	}
	
	///prefix검색등 여러 termdoc이 하나의 단어에서 검색되었을때 사용된다.
	public void addPositions(int[] positions){
		//TODO 기존 position에 추가.
	}
	
	@Override
	public String toString(){
		String pos = ">>";
		if(positions != null){
			for (int i = 0; i < positions.length; i++) {
				pos += positions[i];
				if(i < positions.length - 1){
					pos += ",";
				}
			}
			return docNo+":"+tf+pos;
		}
		return docNo+":"+tf;
	}
}
