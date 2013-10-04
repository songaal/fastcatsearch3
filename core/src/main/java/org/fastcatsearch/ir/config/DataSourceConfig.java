package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datasource")
public class DataSourceConfig {
	
	private List<SingleSourceConfig> fullIndexingSourceConfig;
	private List<SingleSourceConfig> addIndexingSourceConfig;
	private List<JDBCSourceInfo> jdbcSourceInfoList; 
	
	@XmlElementWrapper(name="full-indexing")
	@XmlElement(name="source")
	public List<SingleSourceConfig> getFullIndexingSourceConfig() {
		return fullIndexingSourceConfig;
	}
	public void setFullIndexingSourceConfig(List<SingleSourceConfig> fullIndexingSourceConfig) {
		this.fullIndexingSourceConfig = fullIndexingSourceConfig;
	}
	
	@XmlElementWrapper(name="add-indexing")
	@XmlElement(name="source")
	public List<SingleSourceConfig> getAddIndexingSourceConfig() {
		return addIndexingSourceConfig;
	}
	public void setAddIndexingSourceConfig(List<SingleSourceConfig> addIndexingSourceConfig) {
		this.addIndexingSourceConfig = addIndexingSourceConfig;
	}
	
	@XmlElementWrapper(name="jdbc-sources")
	@XmlElement(name="jdbc-source")
	public List<JDBCSourceInfo> getJdbcSourceInfoList() {
		return jdbcSourceInfoList;
	}
	public void setJdbcSourceInfoList(List<JDBCSourceInfo> jdbcSourceInfoList) {
		this.jdbcSourceInfoList = jdbcSourceInfoList;
	}
	
}
