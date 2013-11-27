package org.fastcatsearch.ir.config;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fastcatsearch.ir.settings.KeyValueMapAdapter;

@XmlRootElement(name="source")
public class SingleSourceConfig {
	
	private String name;
	private boolean active;
	private String sourceReader;
	private String sourceModifier;
	private Map<String, String> properties;
	
	public SingleSourceConfig(){
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@XmlElement(name="reader", required=true)
	public String getSourceReader(){
		return sourceReader;
	}
	
	@XmlElement(name="modifier")
	public String getSourceModifier(){
		return sourceModifier;
	}
	
	public void setSourceReader(String sourceReader){
		this.sourceReader = sourceReader;
	}
	
	public void setSourceModifier(String sourceModifier){
		this.sourceModifier = sourceModifier;
	}
	
	@XmlElement
	@XmlJavaTypeAdapter(KeyValueMapAdapter.class)
	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	
}
