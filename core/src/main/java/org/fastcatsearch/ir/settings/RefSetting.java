package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
	
	@Override
	public String toString(){
		return "[RefSetting]"+ref+", "+removeTag;
	}
	
	
	@XmlAttribute(required = true)
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(Boolean removeTag) {
		this.removeTag = removeTag;
	}
	
}
