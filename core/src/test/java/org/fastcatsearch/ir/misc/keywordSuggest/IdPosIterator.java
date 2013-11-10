package org.fastcatsearch.ir.misc.keywordSuggest;


public class IdPosIterator {
	private IdPos[] list;
	
	public IdPosIterator(){
		
	}
	public IdPosIterator(IdPos[] list){
		this.list = list;
	}

	public boolean next(IdPos idPos){
		return false;
	}
}
