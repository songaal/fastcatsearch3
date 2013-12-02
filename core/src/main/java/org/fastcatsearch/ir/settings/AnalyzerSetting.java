package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//<analyzer id="korean_index" corePoolSize="10" maximumPoolSize="100" className="com.fastcatsearch.plugin.analysis.korean.StandardKoreanAnalyzer" />

@XmlRootElement(name = "analyzer-list")
@XmlType(propOrder = { "className", "maximumPoolSize", "corePoolSize", "name", "id"})
public class AnalyzerSetting {
	private String id;
	private String name;
	private int corePoolSize;
	private int maximumPoolSize;
	private String className;
	
	public AnalyzerSetting(){}
	
	public AnalyzerSetting(String id, String name, int corePoolSize, int maximumPoolSize, String className){
		this.id = id.toUpperCase();
		this.name = name;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.className = className;
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
	
	@XmlAttribute(name="className", required=true)
	public String getClassName() {
		return className;
	}
	public void setClassName(String analyzerClassName) {
		this.className = analyzerClassName;
	}
}
