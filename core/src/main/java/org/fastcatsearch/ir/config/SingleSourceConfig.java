package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class SingleSourceConfig {
	
	private boolean active;
	private String sourceReader;
	private String sourceModifier;
	
	@XmlAttribute
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@XmlElement(name="source-reader", required=true)
	public String getSourceReader(){
		return sourceReader;
	}
	
	@XmlElement(name="source-modifier")
	public String getSourceModifier(){
		return sourceModifier;
	}
	
	public void setSourceReader(String sourceReader){
		this.sourceReader = sourceReader;
	}
	
	public void setSourceModifier(String sourceModifier){
		this.sourceModifier = sourceModifier;
	}

	
}
