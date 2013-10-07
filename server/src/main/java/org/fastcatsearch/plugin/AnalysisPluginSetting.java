package org.fastcatsearch.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "plugin")
public class AnalysisPluginSetting extends PluginSetting {
	private List<DictionarySetting> dictionaryList;
	private List<Analyzer> analyzerList;
	
	@XmlElementWrapper(name = "dictionary-list")
	@XmlElement(name="dictionary")
	public List<DictionarySetting> getDictionarySettingList() {
		return dictionaryList;
	}

	public void setDictionaryList(List<DictionarySetting> dictionaryList) {
		this.dictionaryList = dictionaryList;
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
	
	public static class DictionarySetting {
		String id;
		String name;
		String type;
		String valueColumnList;
		boolean ignoreCase;
		
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
			return name;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		@XmlAttribute
		public String getValueColumnList() {
			return valueColumnList;
		}
		public void setValueColumnList(String valueColumnList) {
			this.valueColumnList = valueColumnList;
		}
		
		@XmlAttribute(required = true)
		public boolean isIgnoreCase() {
			return ignoreCase;
		}
		
		public void setIgnoreCase(boolean ignoreCase) {
			this.ignoreCase = ignoreCase;
		}
		
	}
}
