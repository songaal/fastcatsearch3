package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;

public class TermDocCollector {
	
	private CollectedEntry[] list;
	private int size;
	
	public TermDocCollector(){
		this(4);
	}
	
	public TermDocCollector(int length){
		list = new CollectedEntry[length];
		for (int i = 0; i < list.length; i++) {
			list[i] = new CollectedEntry();
		}
		size = 0;
	}
	
	public int add(CharVector term, PostingDoc termDoc, int queryPosition, boolean isSynonym){
		ensureCapasity();
		list[size++].set(term, termDoc, queryPosition, isSynonym);
		return size;
	}

	private void ensureCapasity() {
		if(size >= list.length){
			int newSize = (int) (size * 1.5) + 1;
			CollectedEntry[] newList = new CollectedEntry[newSize];
			System.arraycopy(list, 0, newList, 0, size);
			
			for (int i = size; i < newList.length; i++) {
				newList[i] = new CollectedEntry();
			}
			
			list = newList;
		}
	}
	
	public void clear(){
		for (int i = 0; i < size; i++) {
			//termDoc은 클수있으므로 gc대상이 되도록 해준다.
			list[i].setTermDoc(null);
		}
		size = 0;
	}
	
	
	public CollectedEntry get(int i){
		return list[i];
	}
	
	public int size(){
		return size;
	}
	
	public int capasity(){
		return list.length;
	}
	
	public void addAll(TermDocCollector termDocCollector){
		for (int i = 0; i < termDocCollector.size; i++) {
			CollectedEntry entry = termDocCollector.list[i];
			add(entry.term(), entry.termDoc(), entry.queryPosition(), entry.isSynonym());
		}
	}
	
	
}
