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

import org.fastcatsearch.ir.search.SearchIndexReader;
import org.fastcatsearch.ir.search.clause.BooleanClause;
import org.fastcatsearch.ir.search.clause.OperatedClause;
import org.fastcatsearch.ir.search.clause.PhraseClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class Term {
	private static Logger logger = LoggerFactory.getLogger(Term.class);
	public static enum Type {ALL, ANY, EXT, PHRASE, BOOL};
	public static int SYNONYM = 1 << 0;
	public static int STOPWORD = 1 << 1;
	public static int HIGHLIGHT = 1 << 2;
	public static int SUMMARY = 1 << 3;
	//IGNORE TERM FREQ의 의미로 단어가 여러번 중복되어 출현하더라도 가중치를 출현빈도에 곱해주지 않는다.
	//상품명이나 기사제목같은 경우 단어가 중복되는 것은 말머리에 추가된 부가정보가 많으므로 이경우 가중치를 곱하지 않는 옵션을 제공한다.  
	public static int WILDCARD = 1 << 4; 
	public static int BOOLEAN = 1 << 5; //불린검색. 검색텀에서 대문자 AND OR NOT을 허용한다.
	
	public static final Option OPTION_DEFAULT = new Option(SYNONYM | STOPWORD); //기본 옵션.
	
	private String[] indexFieldId;
	private boolean fieldConcat; //필드결합 검색인지..
	private String termString;
	private int weight;
	private Type type; //set AND between terms
	protected Option option;
	
	public Term(){}
	public Term(String indexFieldId, String termString){
		this(new String[]{indexFieldId}, termString, 1, Type.ALL);
	}
	public Term(String indexFieldId, String termString, int weight, Type type){
		this(new String[]{indexFieldId}, termString, weight, type);
	}
	public Term(String[] indexFieldId, String termString){
		this(indexFieldId, termString, 1, Type.ALL);
	}
	public Term(String[] indexFieldId, String termString, Type type){
		this(indexFieldId, termString, 1, type);
	}
	public Term(String[] indexFieldId, String termString, int weight, Type type){
		this(indexFieldId, termString, weight, type, OPTION_DEFAULT);
	}
	public Term(String[] indexFieldId, String termString, int weight, Type type, Option option){
		this.indexFieldId = indexFieldId;
		for (int i = 0; i < indexFieldId.length; i++) {
			indexFieldId[i] = indexFieldId[i].toUpperCase();
		}
		//remove escapse character '\'
		this.termString = termString.replaceAll("\\\\","");
		this.weight = weight;
		this.type = type;
		this.option = option;
	}
	
	public String toString(){
		String fieldList = "";
		for(int i=0;i<indexFieldId.length;i++){
			fieldList += indexFieldId[i];
			if(i < indexFieldId.length - 1){
				if(fieldConcat){
					fieldList += "+";
				}else{
					fieldList += ",";
				}
			}
		}
		
		return "{"+fieldList+":"+type+"("+termString+"):"+weight+":"+option+"}";
	}
	public String[] indexFieldId(){
		return indexFieldId;
	}
	public String termString(){
		return termString;
	}
	public int weight(){
		return weight;
	}
	public Type type(){
		return type;
	}
	
	public boolean isFieldConcat(){
		return fieldConcat;
	}
	
	public void setFieldConcat(){
		fieldConcat = true;
	}
	public void addOption(int op){
		option.addOption(op);
	}
	
	public Option option(){
		return option;
	}
	
	public static class Option {
		private int optionValue;
		
		public Option(int optionValue){
			this.optionValue = optionValue;
		}
		
		public int value(){
			return optionValue;
		}
		public int addOption(int op){
			return optionValue |= op;
		}
		
		public boolean useSynonym(){
			return (optionValue & SYNONYM) > 0;
		}
		
		public boolean useStopword(){
			return (optionValue & STOPWORD) > 0;
		}
		
		public boolean useHighlight(){
			return (optionValue & HIGHLIGHT) > 0;
		}
		
		public boolean useSummary(){
			return (optionValue & SUMMARY) > 0;
		}
		
		public boolean useWildcard(){
			return (optionValue & WILDCARD) > 0;
		}
		
		public boolean isBoolean(){
			return (optionValue & BOOLEAN) > 0;
		}
		
		@Override
		public String toString(){
			return "OPT-"+String.valueOf(optionValue);
		}
	}

	public OperatedClause createOperatedClause(SearchIndexReader searchIndexReader, HighlightInfo highlightInfo) {
		
		//TODO FIXME 현재는 모두 boolean clause로 생성.
		if(type == Type.ALL){
			return new BooleanClause(searchIndexReader, this, highlightInfo);
		}else if(type == Type.ANY){
			return new BooleanClause(searchIndexReader, this, highlightInfo);
		}else if(type == Type.PHRASE){
			return new PhraseClause(searchIndexReader, this, highlightInfo);
		}
		
		return new BooleanClause(searchIndexReader, this, highlightInfo);
	}
}

