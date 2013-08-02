package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;

public class PkRefSetting {
	private String ref;
	
	public PkRefSetting() {}
	
	public PkRefSetting(String ref) {
		this.ref = ref;
	}
	
	@XmlAttribute(required = true)
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

}
