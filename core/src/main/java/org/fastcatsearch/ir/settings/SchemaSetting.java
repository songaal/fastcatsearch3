package org.fastcatsearch.ir.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.fastcatsearch.ir.io.AsciiCharTrie;


@XmlRootElement(name = "schema")
@XmlType(propOrder = { "fieldSettingList", "primaryKeySetting", "indexSettingList", "analyzerSettingList", "fieldIndexSettingList", "groupIndexSettingList" })
public class SchemaSetting {
	
	private List<FieldSetting> fieldSettingList;
	private PrimaryKeySetting primaryKeySetting;
	private List<IndexSetting> indexSettingList;
	private List<AnalyzerSetting> analyzerSettingList;
	private List<FieldIndexSetting> fieldIndexSettingList;
	private List<GroupIndexSetting> groupIndexSettingList;
	
	//@XmlElement는 FieldSetting에서 @XmlRootElement로 선언한 것과 별개로 다시 선언해주어야 한다. 여기서는 field가 root가 아니므로.
	@XmlElementWrapper(name="field-list")
	@XmlElement(name="field")
	public List<FieldSetting> getFieldSettingList() {
		return fieldSettingList;
	}
	public void setFieldSettingList(List<FieldSetting> fieldSettingList) {
		this.fieldSettingList = fieldSettingList;
	}
	
	@XmlElementWrapper(name="index-list")
	@XmlElement(name="index")
	public List<IndexSetting> getIndexSettingList() {
		return indexSettingList;
	}
	public void setIndexSettingList(List<IndexSetting> indexSettingList) {
		this.indexSettingList = indexSettingList;
	}
	
	@XmlElementWrapper(name="analyzer-list")
	@XmlElement(name="analyzer")
	public List<AnalyzerSetting> getAnalyzerSettingList() {
		return analyzerSettingList;
	}
	public void setAnalyzerSettingList(List<AnalyzerSetting> analyzerSettingList) {
		this.analyzerSettingList = analyzerSettingList;
	}
	
	@XmlElementWrapper(name="field-index-list")
	@XmlElement(name="field-index")
	public List<FieldIndexSetting> getFieldIndexSettingList() {
		return fieldIndexSettingList;
	}
	public void setFieldIndexSettingList(List<FieldIndexSetting> fieldIndexSettingList) {
		this.fieldIndexSettingList = fieldIndexSettingList;
	}
	
	@XmlElementWrapper(name="group-index-list")
	@XmlElement(name="group-index")
	public List<GroupIndexSetting> getGroupIndexSettingList() {
		return groupIndexSettingList;
	}
	public void setGroupIndexSettingList(List<GroupIndexSetting> groupSettingList) {
		this.groupIndexSettingList = groupSettingList;
	}
	
	@XmlElement(name="primary-key")
	public PrimaryKeySetting getPrimaryKeySetting() {
		return primaryKeySetting;
	}
	public void setPrimaryKeySetting(PrimaryKeySetting primaryKeySetting) {
		this.primaryKeySetting = primaryKeySetting;
	}
	
}
