package org.fastcatsearch.ir.setting;

import javax.xml.bind.annotation.XmlAttribute;


public class PrimaryKeySetting {
	private String ref;

	public PrimaryKeySetting() {}
	
	public PrimaryKeySetting(String ref) {
		this.ref = ref;
	}
	
	@XmlAttribute
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}
	
}
