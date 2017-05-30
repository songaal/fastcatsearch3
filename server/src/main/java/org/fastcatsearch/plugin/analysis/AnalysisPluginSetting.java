package org.fastcatsearch.plugin.analysis;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fastcatsearch.plugin.PluginSetting;

@XmlRootElement(name = "plugin")
public class AnalysisPluginSetting extends PluginSetting {
	private List<DictionarySetting> dictionarySettingList;
	private List<Analyzer> analyzerList;
	
	@XmlElementWrapper(name = "dictionary-list")
	@XmlElement(name="dictionary")
	public List<DictionarySetting> getDictionarySettingList() {
		return dictionarySettingList;
	}

	public void setDictionarySettingList(List<DictionarySetting> dictionarySettingList) {
		this.dictionarySettingList = dictionarySettingList;
	}

	@XmlElementWrapper(name = "analyzer-list", required = false)
	@XmlElement(name="analyzer", required = false)
	public List<Analyzer> getAnalyzerList() {
		return analyzerList;
	}

	public void setAnalyzerList(List<Analyzer> analyzerList) {
		this.analyzerList = analyzerList;
	}
	
	@XmlType(propOrder={"className", "name", "id"})
	public static class Analyzer {
		private String id;
		private String name;
		private String className;
		
		public Analyzer(){
		}
		
		public Analyzer(String id, String name, String className) {
			this.id = id;
			this.name = name;
			this.className = className;
		}
		
		//memory to xml only 속성.
		@XmlAttribute
		public String getId() {
			return id;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		@XmlAttribute(name="className")
		public String getClassName() {
			return className;
		}
	}
	
	@XmlType(propOrder={"columnSettingList", "ignoreCase", "tokenType", "type", "name", "id"})
	public static class DictionarySetting {
		private String id;
		private String name;
		private Type type;
		private String tokenType;
		private boolean ignoreCase;
		private List<ColumnSetting> columnSettingList;
		
		public static enum Type {
			SYSTEM, SET, MAP, SYNONYM, SYNONYM_2WAY, SPACE, CUSTOM, INVERT_MAP, COMPOUND
		}
		
		@XmlAttribute(required = true)
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@XmlAttribute(required = true)
		public Type getType() {
			return type;
		}
		public void setType(Type type) {
			this.type = type;
		}
		
		@XmlAttribute
		public String getTokenType() {
			return tokenType;
		}
		public void setTokenType(String tokenType) {
			this.tokenType = tokenType;
		}
		
		@XmlAttribute
		public boolean isIgnoreCase() {
			return ignoreCase;
		}
		
		public void setIgnoreCase(boolean ignoreCase) {
			this.ignoreCase = ignoreCase;
		}
		
		@XmlElement(name="column")
		public List<ColumnSetting> getColumnSettingList() {
			return columnSettingList;
		}
		public void setColumnSettingList(List<ColumnSetting> columnSettingList) {
			this.columnSettingList = columnSettingList;
		}
		
	}
	
	/*
	 * <dictionary id="boosting" name="부스팅사전" type="custom" ignoreCase="true">
			<column name="key" searchable="true" key="true" compilable="true"/>
			<column name="value" searchable="true" compilable="true" separator=","/>
			<column name="display" searchable="true" />
			<column name="url" searchable="true" /> 
			<column name="score" index="true" compilable="true" />
		</dictionary>
	 * */
	@XmlType(propOrder={"nullableUnique", "compilable", "searchable", "separator", "index", "key", "type", "name"})
	public static class ColumnSetting {
		private String name;
		private String type;
		private boolean key; /*사전으로 컴파일시 key로 사용된다.*/
		private boolean index;
		private String separator;
		private boolean searchable;
		private boolean compilable;
		private boolean nullableUnique; //null을 허용하지만 값이 존재할때에는 unique해야하는 컬럼. 유사어의 key컬럼용도. table생성시에는 관여하지 않는다.
		
		@XmlAttribute(required = true)
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@XmlAttribute(required = true)
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		@XmlAttribute
		public boolean isSearchable() {
			return searchable;
		}
		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}
		@XmlAttribute
		public boolean isKey() {
			return key;
		}
		public void setKey(boolean key) {
			this.key = key;
		}
		@XmlAttribute
		public boolean isIndex() {
			return index;
		}
		public void setIndex(boolean index) {
			this.index = index;
		}
		@XmlAttribute
		public boolean isCompilable() {
			return compilable;
		}
		public void setCompilable(boolean compilable) {
			this.compilable = compilable;
		}
		@XmlAttribute
		public String getSeparator() {
			return separator;
		}
		public void setSeparator(String separator) {
			this.separator = separator;
		}

		@XmlAttribute
		public boolean isNullableUnique() {
			return nullableUnique;
		}
		
		public void setNullableUnique(boolean nullableUnique) {
			this.nullableUnique = nullableUnique;
		}
		
	}
}
