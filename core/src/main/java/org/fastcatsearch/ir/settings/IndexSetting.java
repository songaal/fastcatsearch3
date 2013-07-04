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

package org.fastcatsearch.ir.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "ignoreCase", "positionIncrementGap", "queryAnalyzer", "indexAnalyzer", "fieldList", "id"} )
@XmlRootElement(name = "index")
public class IndexSetting {

	private String id;
	//index필드는 여러개의 필드를 받을 수 있다.
	
	private List<RefSetting> fieldList;
	private String indexAnalyzer;
	private String queryAnalyzer;
	private int positionIncrementGap;
	private boolean ignoreCase;
	private boolean storePosition;
	
	public IndexSetting() { }
	
	public IndexSetting(String id){
		this.id = id;
	}
	
	public IndexSetting(String id, String indexAnalyzer, String queryAnalyzer){
		this.id = id;
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = (queryAnalyzer == null) ? queryAnalyzer : indexAnalyzer;
	}
	
	public String toString(){
		return "[index="+id+":"+fieldList+":"+indexAnalyzer+":"+queryAnalyzer+":"+ignoreCase+":"+storePosition+"]";
	}

	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(name="field")
	public List<RefSetting> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<RefSetting> fieldList) {
		this.fieldList = fieldList;
	}

	@XmlAttribute
	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public void setIndexAnalyzer(String indexAnalyzer) {
		this.indexAnalyzer = indexAnalyzer;
	}

	@XmlAttribute
	public String getQueryAnalyzer() {
		return queryAnalyzer;
	}

	public void setQueryAnalyzer(String queryAnalyzer) {
		this.queryAnalyzer = queryAnalyzer;
	}

	@XmlAttribute
	public int getPositionIncrementGap() {
		return positionIncrementGap;
	}

	public void setPositionIncrementGap(int positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}

	@XmlAttribute
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	@XmlAttribute
	public boolean isStorePosition() {
		return storePosition;
	}

	public void setStorePosition(boolean storePosition) {
		this.storePosition = storePosition;
	}
	
}
