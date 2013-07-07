package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class DataSourceConfig {
	private String readerType;
	private String configType;
	private String sourceModifier;
	
	@XmlAttribute(required=true)
	public String getReaderType(){
		return readerType;
	}
	
	@XmlAttribute(required=true)
	public String getConfigType(){
		return configType;
	}
	
	@XmlElement
	public String getSourceModifier(){
		return sourceModifier;
	}
	
	public void setReaderType(String readerType){
		this.readerType = readerType;
	}
	
	public void setConfigType(String configType){
		this.configType = configType;
	}
	
	public void setSourceModifier(String sourceModifier){
		this.sourceModifier = sourceModifier;
	}
}
