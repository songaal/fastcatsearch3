package org.fastcatsearch.ir.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;


public class PrimaryKeySetting {
	
	private String id;
	private List<PkRefSetting> fieldList;

	public PrimaryKeySetting() {}
	
	public PrimaryKeySetting(String id) {
		this.id = id;
	}
	
	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement(name="field")
	public List<PkRefSetting> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<PkRefSetting> fieldList) {
		this.fieldList = fieldList;
	}
	
}
