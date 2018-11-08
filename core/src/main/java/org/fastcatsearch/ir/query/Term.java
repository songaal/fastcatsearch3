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

import org.fastcatsearch.ir.analysis.TermsEntry;
import org.fastcatsearch.ir.search.SearchIndexReader;
import org.fastcatsearch.ir.search.clause.AnalyzedBooleanClause;
import org.fastcatsearch.ir.search.clause.BooleanClause;
import org.fastcatsearch.ir.search.clause.OperatedClause;
import org.fastcatsearch.ir.search.clause.PhraseClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


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
	public static int HIGHLIGHT_FORCE = 1 << 6; //강제 하이라이팅. 최적의 분석 결과와는 상관없이 무조건 검색 결과를 하이라이팅한다.

	public static final Option OPTION_DEFAULT = new Option(SYNONYM | STOPWORD); //기본 옵션.
	
	private String[] indexFieldId;
	private String termString;
	private int weight;
	private Type type; //set AND between terms
	protected Option option;
	protected String typeAttribute;

    private List<TermsEntry> termsEntryList;
    private int proximity;

	private String analyzeType;
	// 2018-11-8 swsong
	// 추가단어를 허용할지 여부. 디폴트로는 사용한다. 사용못하게 하려면 query 로는 안되고 programmatic 하게 설정해야함.
	//복합명사 허용때문에 만들었는데, 전체적으로 추가단어에 대해서 사용하면 좋을듯..
	private boolean disableAdditionalTerm;

	public Term(){}
	public Term(String indexFieldId, String termString){
		this(new String[]{indexFieldId}, termString, -1, Type.ALL);
	}
	public Term(String indexFieldId, String termString, int weight, Type type){
		this(new String[]{indexFieldId}, termString, weight, type);
	}
	public Term(String[] indexFieldId, String termString){
		this(indexFieldId, termString, -1, Type.ALL);
	}
	public Term(String[] indexFieldId, String termString, Type type){
		this(indexFieldId, termString, -1, type);
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

	public boolean isDisableAdditionalTerm() {
		return disableAdditionalTerm;
	}

	public void setDisableAdditionalTerm(boolean disableAdditionalTerm) {
		this.disableAdditionalTerm = disableAdditionalTerm;
	}

	public int getProximity() {
        return proximity;
    }

    public Term withProximity(int proximity) {
        this.proximity = proximity;
        return this;
    }

    public String toString(){
		String fieldList = "";
		for (int i = 0; i < indexFieldId.length; i++) {
			fieldList += indexFieldId[i];
			if (i < indexFieldId.length - 1) {
                fieldList += ",";
			}
		}

        return "{" + fieldList + ":" + type
                + "(" + termString + (proximity == 0 ? ")" : ")~" + proximity)
                + ":" + weight + ":" + option + "}";
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
	
	public void addOption(int op){
		option.addOption(op);
	}
	
	public Option option(){
		return option;
	}
	
	public String typeAttribute(){
		return typeAttribute;
	}
	
	public void setTypeAttribute(String typeAttribute){
		this.typeAttribute = typeAttribute;
	}

    public void setTermString(String str) {
        this.termString = str;
    }

    public List<TermsEntry> getTermsEntryList() {
        return termsEntryList;
    }

    public void setTermsEntryList(List<TermsEntry> termsEntryList) {
        this.termsEntryList = termsEntryList;
    }

	public void setAnalyzeType(String analyzeType) {
		this.analyzeType = analyzeType;
	}

    public static class Option {
		private int optionValue;
		
		public Option(){
		}
		
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

		public boolean useForceHighlight(){
			return (optionValue & HIGHLIGHT_FORCE) > 0;
		}

		@Override
		public String toString(){
			return "OPT-"+String.valueOf(optionValue);
		}
	}

	public OperatedClause createOperatedClause(SearchIndexReader searchIndexReader, HighlightInfo highlightInfo) throws IOException {
		if(termsEntryList != null) {
            return new AnalyzedBooleanClause(searchIndexReader, this, highlightInfo);
        }
		if(type == Type.ALL || type == Type.ANY){
			return new BooleanClause(searchIndexReader, this, highlightInfo, analyzeType);
		}else if(type == Type.PHRASE){
			return new PhraseClause(searchIndexReader, this, highlightInfo);
		}
		
		return new BooleanClause(searchIndexReader, this, highlightInfo, null);
	}
	
}

