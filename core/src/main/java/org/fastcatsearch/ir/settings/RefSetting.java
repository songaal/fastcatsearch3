package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "removeTag", "ref" })
public class RefSetting {
	private String ref;
	private boolean removeTag;
	
	public RefSetting() {}
	
	public RefSetting(String ref) {
		this.ref = ref;
	}
	
	public RefSetting(String ref, boolean removeTag) {
		this.ref = ref;
		this.removeTag = removeTag;
	}
	
	
	@XmlAttribute(required = true)
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	@XmlAttribute
	public boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}
	
	
}
