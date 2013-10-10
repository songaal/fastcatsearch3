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

	@XmlElementWrapper(name = "analyzer-list")
	@XmlElement(name="analyzer")
	public List<Analyzer> getAnalyzerList() {
		return analyzerList;
	}

	public void setAnalyzerList(List<Analyzer> analyzerList) {
		this.analyzerList = analyzerList;
	}
	
	public static class Analyzer {
		String id;
		String name;
		String className;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		@XmlAttribute(name="class")
		public String getClassName() {
			return className;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setClassName(String className) {
			this.className = className;
		}
	}
	
	@XmlType(propOrder={"columnSettingList", "type", "name", "id"})
	public static class DictionarySetting {
		private String id;
		private String name;
		private String type;
		private List<ColumnSetting> columnSettingList;
		
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
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
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
	 * <dictionary id="boosting" name="부스팅사전" type="custom">
			<column name="key" searchable="true" key="true" compilable="true" ignoreCase="true"/>
			<column name="value" searchable="true" compilable="true" separator=","/>
			<column name="display" searchable="true" />
			<column name="url" searchable="true" /> 
			<column name="score" index="true" compilable="true" />
		</dictionary>
	 * */
	public static class ColumnSetting {
		private String name;
		private String type;
		private boolean key; /*사전으로 컴파일시 key로 사용된다.*/
		private boolean index;
		private String separator;
		private boolean searchable;
		private boolean compilable;
		private boolean ignoreCase;
		
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
		public boolean isIgnoreCase() {
			return ignoreCase;
		}
		
		public void setIgnoreCase(boolean ignoreCase) {
			this.ignoreCase = ignoreCase;
		}
		
	}
}
