package org.fastcatsearch.ir.misc.keywordSuggest;


public class IdPos {
	protected int id;
	protected int pos;
	
	public IdPos(){
		id = -1;
	}
	public IdPos(int id, int pos){
		this.id = id;
		this.pos = pos;
	}
	
	public void set(int id, int pos){
		this.id = id;
		this.pos = pos;
	}
	
	public void set(IdPos other){
		this.id = other.id;
		this.pos = other.pos;
	}
	
	public int id(){
		return id;
	}
	
	public int pos(){
		return pos;
	}
	
	public String toString(){
		return id + " : " + pos;
	}
}
