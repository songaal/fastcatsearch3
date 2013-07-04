package org.fastcatsearch.ir.field;


public class FieldType {
	
	private int size;
	private boolean store;
	private boolean removeTag;
	private boolean multiValue;
	private char multiValueDelimiter;
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public boolean isMultiValue() {
		return multiValue;
	}
	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}
	public char getMultiValueDelimiter() {
		return multiValueDelimiter;
	}
	public void setMultiValueDelimiter(char multiValueDelimiter) {
		this.multiValueDelimiter = multiValueDelimiter;
	}
	public boolean isStore() {
		return store;
	}
	public void setStore(boolean store) {
		this.store = store;
	}
	public boolean isRemoveTag() {
		return removeTag;
	}
	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}
	
	
}
