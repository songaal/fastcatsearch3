/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.query;

import java.util.HashMap;
import java.util.Map;


/**
 * Contains information that used when making summary result. 
 * */
public class HighlightInfo {
	
	private Map<String, String> fieldAnalyzerMap;
	private Map<String, String> fieldQueryMap;
	private Map<String, Boolean> fieldHighlightMap;
	
	public HighlightInfo() {
		this.fieldAnalyzerMap = new HashMap<String, String>();
		this.fieldQueryMap = new HashMap<String, String>();
		this.fieldHighlightMap = new HashMap<String, Boolean>();
	}
	
	public HighlightInfo(Map<String, String> fieldAnalyzerMap, Map<String, String> fieldQueryMap) {
		this.fieldAnalyzerMap = fieldAnalyzerMap;
		this.fieldQueryMap = fieldQueryMap;
	}
	public void add(String fieldId, String analyzer, String termString, boolean b){
		fieldAnalyzerMap.put(fieldId, analyzer);
		String value = fieldQueryMap.get(fieldId);
		if(value != null){
			value += (" " + termString);
		}else{
			value = termString;
		}
		fieldQueryMap.put(fieldId, value);
		fieldHighlightMap.put(fieldId, b);
	}
	
	public String getAnalyzer(String fieldId){
		return fieldAnalyzerMap.get(fieldId);
	}
	
	public String getQueryString(String fieldId){
		return fieldQueryMap.get(fieldId);
	}
	
	public Map<String, String> fieldAnalyzerMap(){
		return fieldAnalyzerMap;
	}
	
	public Map<String, String> fieldQueryMap(){
		return fieldQueryMap;
	}

	public boolean useHighlight(String fieldId) {
		
		if(fieldHighlightMap.containsKey(fieldId)) {
			return fieldHighlightMap.get(fieldId);
		} else {
			return false;
		}
	}
}
