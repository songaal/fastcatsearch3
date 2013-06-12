package org.fastcatsearch.ir.setting;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "removeTag", "ref" })
public class RefSetting {
	private String ref;
	private boolean removeTag;
	
	private FieldSetting fieldSetting;//xml에 사용되지는 않음.
	
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
	
	
	@XmlTransient
	public FieldSetting getFieldSetting() {
		return fieldSetting;
	}

	public void setFieldSetting(FieldSetting fieldSetting) {
		this.fieldSetting = fieldSetting;
	}

	
	
	
}
