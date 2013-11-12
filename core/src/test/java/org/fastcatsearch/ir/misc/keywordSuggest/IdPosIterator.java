package org.fastcatsearch.ir.misc.keywordSuggest;

import java.util.List;


public class IdPosIterator {
	private List<IdPos> list;
	private int pos;
	
	public IdPosIterator(){
	}
	
	public IdPosIterator(List<IdPos> list){
		this.list = list;
	}

	public int size(){
		if(list == null){
			return 0;
		}
		return list.size();
	}
	public boolean next(IdPos idPos){
		if(pos < list.size()){
			idPos.set(list.get(pos++));
			return true;
		}
		
		return false;
	}
}
