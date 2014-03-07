package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jdbc-support")
public class JDBCSupportConfig {
	
	private List<JDBCDriverInfo> jdbcDriverInfoList;

	public JDBCSupportConfig() {
		jdbcDriverInfoList = new ArrayList<JDBCDriverInfo>();
	}
	
	@XmlElementWrapper(name="jdbc-drivers")
	@XmlElement(name="jdbc-driver")
	public List<JDBCDriverInfo> getJdbcSourceInfoList() {
		return jdbcDriverInfoList;
	}
	
	public void setJdbcDriverInfoList(List<JDBCDriverInfo> jdbcDriverInfoList) {
		this.jdbcDriverInfoList = jdbcDriverInfoList;
	}
}