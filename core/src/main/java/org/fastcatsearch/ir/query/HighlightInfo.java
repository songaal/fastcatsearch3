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

import org.fastcatsearch.ir.query.Term.Option;


/**
 * Contains information that used when making summary result. 
 * */
public class HighlightInfo {
	
	private Map<String, String> fieldIndexAnalyzerMap;
	private Map<String, String> fieldQueryAnalyzerMap;
	private Map<String, String> fieldQueryTermMap;
	private Map<String, Integer> fieldSearchOptionMap;
	
	public HighlightInfo() {
	}
	
	public HighlightInfo(Map<String, String> fieldIndexAnalyzerMap, Map<String, String> fieldQueryAnalyzerMap, Map<String, String> fieldQueryTermMap, Map<String, Integer> fieldSearchOptionMap) {
		this.fieldIndexAnalyzerMap = fieldIndexAnalyzerMap;
		this.fieldQueryAnalyzerMap = fieldQueryAnalyzerMap;
		this.fieldQueryTermMap = fieldQueryTermMap;
		this.fieldSearchOptionMap = fieldSearchOptionMap;
	}
	
	private void prepareMap(){
		this.fieldIndexAnalyzerMap = new HashMap<String, String>();
		this.fieldQueryAnalyzerMap = new HashMap<String, String>();
		this.fieldQueryTermMap = new HashMap<String, String>();
		this.fieldSearchOptionMap = new HashMap<String, Integer>();
	}
	public void add(String fieldId, String indexAnalyzerId, String queryAnalyzerId, String queryTerm, int searchOption){
		if(fieldQueryTermMap == null) {
			prepareMap();
		}
		fieldIndexAnalyzerMap.put(fieldId, indexAnalyzerId);
		fieldQueryAnalyzerMap.put(fieldId, queryAnalyzerId);
		String value = fieldQueryTermMap.get(fieldId);
		if(value != null){
			value += (" " + queryTerm);
		}else{
			value = queryTerm;
		}
		fieldQueryTermMap.put(fieldId, value);
		fieldSearchOptionMap.put(fieldId, searchOption);
	}
	
	public String getQueryAnalyzerId(String fieldId){
		return fieldQueryAnalyzerMap.get(fieldId);
	}
	
	public String getIndexAnalyzerId(String fieldId){
		return fieldIndexAnalyzerMap.get(fieldId);
	}
	
	public String getQueryTerm(String fieldId){
		return fieldQueryTermMap.get(fieldId);
	}
	
	public Map<String, String> fieldIndexAnalyzerMap(){
		return fieldIndexAnalyzerMap;
	}
	
	public Map<String, String> fieldQueryAnalyzerMap(){
		return fieldQueryAnalyzerMap;
	}
	
	public Map<String, String> fieldQueryTermMap(){
		return fieldQueryTermMap;
	}
	
	public Map<String, Integer> fieldSearchOptionMap(){
		return fieldSearchOptionMap;
	}

//	public boolean useHighlight(String fieldId) {
//		
//		if(fieldSearchOptionMap.containsKey(fieldId)) {
//			return fieldSearchOptionMap.get(fieldId);
//		} else {
//			return false;
//		}
//	}
	
	public Option getOption(String fieldId) {
		if(fieldSearchOptionMap != null && fieldSearchOptionMap.containsKey(fieldId)) {
			return new Option(fieldSearchOptionMap.get(fieldId));
		} else {
			return new Option();
		}
	}
}
