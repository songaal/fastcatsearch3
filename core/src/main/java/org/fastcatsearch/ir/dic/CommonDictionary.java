package org.fastcatsearch.ir.dic;

import java.util.List;
import java.util.Map;

import org.fastcatsearch.ir.io.CharVector;

public class CommonDictionary<E> {
	
	private Dictionary<E> systemDictionary;
	
	Map<String, Object> dictionaryMap;
	
	
	public CommonDictionary(Dictionary<E> systemDictionary){
		this.systemDictionary = systemDictionary;
	}
	
	public List<E> find(CharVector token) {
		return systemDictionary.find(token);
	}
	
	public int size(){
		return systemDictionary.size();
	}
	
	public Object getDictionary(String name){
		return dictionaryMap.get(name);
	}
	
	public Object addDictionary(String name, Object dictionary){
		return dictionaryMap.put(name, dictionary);
	}
	
}
