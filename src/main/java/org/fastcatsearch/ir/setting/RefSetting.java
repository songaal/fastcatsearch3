package org.fastcatsearch.ir.setting;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class RefSetting {
	private String id;
	
	private FieldSetting fieldSetting;//xml에 사용되지는 않음.
	
	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlTransient
	public FieldSetting getFieldSetting() {
		return fieldSetting;
	}

	public void setFieldSetting(FieldSetting fieldSetting) {
		this.fieldSetting = fieldSetting;
	}
	
	
}
