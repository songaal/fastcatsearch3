package org.fastcatsearch.ir.index;

public class MultiKeyEntry {
	private String[] keys;
	public MultiKeyEntry(String... keys){
		
	}
	
	public String getKey(int i){
		return keys[i];
	}
	
	public int size(){
		return keys.length;
	}
}
