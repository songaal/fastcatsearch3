package org.fastcatsearch.ir.setting;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "schema")
@XmlSeeAlso({ArrayList.class, FieldSetting.class})
@XmlType(propOrder = { 
		"fieldSettingList", "indexSettingList", "sortSettingList", 
		"columnSettingList", "groupSettingList", "filterSettingList", 
})
public class SchemaSetting {
	
	private List<FieldSetting> fieldSettingList;
	private List<IndexSetting> indexSettingList;
	private List<SortSetting> sortSettingList;
	private List<ColumnSetting> columnSettingList;
	private List<GroupSetting> groupSettingList;
	private List<FilterSetting> filterSettingList;
	
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
	
	@XmlElementWrapper(name="sort-list")
	@XmlElement(name="sort")
	public List<SortSetting> getSortSettingList() {
		return sortSettingList;
	}
	public void setSortSettingList(List<SortSetting> sortSettingList) {
		this.sortSettingList = sortSettingList;
	}
	
	@XmlElementWrapper(name="column-list")
	@XmlElement(name="column")
	public List<ColumnSetting> getColumnSettingList() {
		return columnSettingList;
	}
	public void setColumnSettingList(List<ColumnSetting> columnSettingList) {
		this.columnSettingList = columnSettingList;
	}
	
	@XmlElementWrapper(name="group-list")
	@XmlElement(name="group")
	public List<GroupSetting> getGroupSettingList() {
		return groupSettingList;
	}
	public void setGroupSettingList(List<GroupSetting> groupSettingList) {
		this.groupSettingList = groupSettingList;
	}
	
	@XmlElementWrapper(name="filter-list")
	@XmlElement(name="filter")
	public List<FilterSetting> getFilterSettingList() {
		return filterSettingList;
	}

	public void setFilterSettingList(List<FilterSetting> filterSettingList) {
		this.filterSettingList = filterSettingList;
	}
}
