package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

//<analyzer id="korean_index" corePoolSize="10" maximumPoolSize="100">com.fastcatsearch.plugin.analysis.korean.StandardKoreanAnalyzer</analyzer>

@XmlRootElement(name = "analyzer-list")
@XmlType(propOrder = { "analyzer", "maximumPoolSize", "corePoolSize", "name", "id"})
public class AnalyzerSetting {
	private String id;
	private String name;
	private int corePoolSize;
	private int maximumPoolSize;
	private String analyzerClassName;
	
	public AnalyzerSetting(){}
	
	public AnalyzerSetting(String id, String name, int corePoolSize, int maximumPoolSize, String analyzerClassName){
		this.id = id.toUpperCase();
		this.name = name;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.analyzerClassName = analyzerClassName;
	}
	
	@XmlAttribute(name="id", required=true)
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
	
	@XmlAttribute
	public int getCorePoolSize() {
		return corePoolSize;
	}
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	@XmlAttribute
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}
	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}
	
	@XmlValue
	public String getAnalyzer() {
		return analyzerClassName;
	}
	public void setAnalyzer(String analyzerClassName) {
		this.analyzerClassName = analyzerClassName;
	}
}
