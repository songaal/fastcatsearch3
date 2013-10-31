package org.fastcatsearch.ir.dic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDictionary<E> {
	protected static final Logger logger = LoggerFactory.getLogger(CommonDictionary.class);
	
	private Dictionary<E> systemDictionary;
	
	private Map<String, Object> dictionaryMap;
	
	public CommonDictionary(Dictionary<E> systemDictionary){
		this.systemDictionary = systemDictionary;
		dictionaryMap = new HashMap<String, Object>();
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
		logger.debug("addDictionary {} : {}", name, dictionary);
		
		return dictionaryMap.put(name, dictionary);
	}

	public void appendAdditionalNounEntry(Set<CharVector> keySet, String tokenType) {
		systemDictionary.appendAdditionalNounEntry(keySet, tokenType);
	}
	
}
