package org.fastcatsearch.ir.dictionary;

public interface ReloadableDictionary {
	public void reload(Object object) throws IllegalArgumentException;
}
