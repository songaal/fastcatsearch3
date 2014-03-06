package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jdbc-source-list")
public class JDBCSourceConfig {
	
	public JDBCSourceConfig() {
		jdbcSourceInfoList = new ArrayList<JDBCSourceInfo>();
	}
	
	private List<JDBCSourceInfo> jdbcSourceInfoList; 
	
	@XmlElementWrapper(name="jdbc-sources")
	@XmlElement(name="jdbc-source")
	public List<JDBCSourceInfo> getJdbcSourceInfoList() {
		return jdbcSourceInfoList;
	}
	
	public void setJdbcSourceInfoList(List<JDBCSourceInfo> jdbcSourceInfoList) {
		this.jdbcSourceInfoList = jdbcSourceInfoList;
	}
}