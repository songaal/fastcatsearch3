package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;

public class RefSetting {
	private String ref;
	
	public RefSetting() {}
	
	public RefSetting(String ref) {
		this.ref = ref.toUpperCase();
	}
	
	@Override
	public String toString(){
		return "[RefSetting]"+ref;
	}
	
	
	@XmlAttribute(required = true)
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref.toUpperCase();
	}
	
}
