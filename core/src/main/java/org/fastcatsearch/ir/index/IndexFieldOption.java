package org.fastcatsearch.ir.index;

public class IndexFieldOption {
	
	public static final int STORE_POSITION = 1 << 1;
	
	private int optionValue;
	
	
	public IndexFieldOption() {
	}
	
	public IndexFieldOption(int optionValue){
		this.optionValue = optionValue;
	}
	

	public int value(){
		return optionValue;
	}
	
	public void setStorePosition(){
		optionValue |= STORE_POSITION;
	}
	
	public boolean isStorePosition(){
		return (optionValue & STORE_POSITION) > 0 ;
	}
	
	public boolean equals(Object obj){
		IndexFieldOption other = (IndexFieldOption) obj;
		return optionValue == other.optionValue;
	}
}
