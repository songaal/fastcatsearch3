package org.fastcatsearch.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class AnalysisPluginSetting extends PluginSetting {
	private List<Dictionary> dictionaryList;
	private List<Analyzer> analyzerList;
	
	@XmlElementWrapper(name = "dictionary-list")
	@XmlElement(name="dictionary")
	public List<Dictionary> getDictionaryList() {
		return dictionaryList;
	}

	public void setDictionaryList(List<Dictionary> dictionaryList) {
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
	
	public static class Dictionary {
		String id;
		String name;
		boolean unmodifiable;
		String daoClass;
		
		@XmlAttribute
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
		
		@XmlAttribute
		public boolean isUnmodifiable() {
			return unmodifiable;
		}
		public void setUnmodifiable(boolean unmodifiable) {
			this.unmodifiable = unmodifiable;
		}
		
		@XmlAttribute
		public String getDaoClass() {
			return daoClass;
		}
		public void setDaoClass(String daoClass) {
			this.daoClass = daoClass;
		}
		
		
	}
}
