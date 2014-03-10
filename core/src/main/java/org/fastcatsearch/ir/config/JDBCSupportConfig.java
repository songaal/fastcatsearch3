package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
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
	
	public static class JDBCDriverInfo {

		private String id;
		private String name;
		private String driver;
		private String urlTemplate;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		
		@XmlAttribute
		public String getName() {
			return name;
		}
		
		@XmlAttribute
		public String getDriver() {
			return driver;
		}
		
		@XmlAttribute
		public String getUrlTemplate() {
			return urlTemplate;
		}

		public void setId(String id) {
			this.id = id;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public void setDriver(String driver) {
			this.driver = driver;
		}
		
		public void setUrlTemplate(String urlTemplate) {
			this.urlTemplate = urlTemplate;
		}

	}
}