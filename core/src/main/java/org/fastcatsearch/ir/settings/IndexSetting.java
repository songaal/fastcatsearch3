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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(propOrder = { "positionIncrementGap", "storePosition", "ignoreCase", "queryAnalyzer", "indexAnalyzer", "fieldList", "name", "id"} )
@XmlRootElement(name = "index")
public class IndexSetting {

	private String id;
	private String name;
	//index필드는 여러개의 필드를 받을 수 있다.
	private List<RefSetting> fieldList;
	private String indexAnalyzer;
	private String queryAnalyzer;
	private boolean ignoreCase;
	private boolean storePosition;
	private int positionIncrementGap;
	
	public IndexSetting() { }
	
	public IndexSetting(String id){
		this.id = id.toUpperCase();
	}
	
	public IndexSetting(String id, String indexAnalyzer, String queryAnalyzer){
		this.id = id.toUpperCase();
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = (queryAnalyzer == null) ? queryAnalyzer : indexAnalyzer;
	}
	
	public String toString(){
		return "[index="+id+":"+name+":"+fieldList+":"+indexAnalyzer+":"+queryAnalyzer+":"+ignoreCase+":"+storePosition+"]";
	}

	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id.toUpperCase();
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="field")
	public List<RefSetting> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<RefSetting> fieldList) {
		this.fieldList = fieldList;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalStringAdapter.class)
	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public void setIndexAnalyzer(String indexAnalyzer) {
		this.indexAnalyzer = indexAnalyzer;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalStringAdapter.class)
	public String getQueryAnalyzer() {
		return queryAnalyzer;
	}

	public void setQueryAnalyzer(String queryAnalyzer) {
		this.queryAnalyzer = queryAnalyzer;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalIntPositiveAdapter.class)
	public Integer getPositionIncrementGap() {
		return positionIncrementGap;
	}

	public void setPositionIncrementGap(Integer positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(Boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isStorePosition() {
		return storePosition;
	}

	public void setStorePosition(Boolean storePosition) {
		this.storePosition = storePosition;
	}
	
}
