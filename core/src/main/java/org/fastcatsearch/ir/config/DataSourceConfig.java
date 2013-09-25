package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datasource")
public class DataSourceConfig {
	
	private IndexingSourceConfig fullIndexingSourceConfig;
	private IndexingSourceConfig addIndexingSourceConfig;
	private List<JDBCSourceInfo> jdbcSourceInfoList; 
	
	@XmlElement(name="full-indexing")
	public IndexingSourceConfig getFullIndexingSourceConfig() {
		return fullIndexingSourceConfig;
	}
	public void setFullIndexingSourceConfig(IndexingSourceConfig fullIndexingSourceConfig) {
		this.fullIndexingSourceConfig = fullIndexingSourceConfig;
	}
	
	@XmlElement(name="add-indexing")
	public IndexingSourceConfig getAddIndexingSourceConfig() {
		return addIndexingSourceConfig;
	}
	public void setAddIndexingSourceConfig(IndexingSourceConfig addIndexingSourceConfig) {
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
