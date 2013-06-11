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

package org.fastcatsearch.ir.setting;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "index")
public class IndexSetting {

	private String name;
	//index필드는 여러개의 필드를 받을 수 있다.
	
	private List<IndexField> field;
	private String indexAnalyzer;
	private String queryAnalyzer;
	private int positionIncrementGap;
	private boolean ignoreCase;
	
	public IndexSetting() { }
	
	public IndexSetting(String name, List<IndexField> field){
		this.name = name;
		this.field = field;
	}
	
	public IndexSetting(String name, List<IndexField> field, String indexAnalyzer, String queryAnalyzer){
		this.name = name;
		this.field = field;
		this.indexAnalyzer = indexAnalyzer;
		this.queryAnalyzer = (queryAnalyzer == null) ? queryAnalyzer : indexAnalyzer;
	}
	
	public String toString(){
		return "[index="+name+":"+field+":"+indexAnalyzer+":"+queryAnalyzer+"]";
	}

	@XmlAttribute(required = true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name="field")
	public List<IndexField> getField() {
		return field;
	}

	public void setField(List<IndexField> field) {
		this.field = field;
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
	
	public static class IndexField {
		private String ref;

		public IndexField() { }
		
		public IndexField(String ref){
			this.ref = ref;
		}
		
		@XmlAttribute(required = true)
		public String getRef() {
			return ref;
		}

		public void setRef(String ref) {
			this.ref = ref;
		}
		
	}
}
