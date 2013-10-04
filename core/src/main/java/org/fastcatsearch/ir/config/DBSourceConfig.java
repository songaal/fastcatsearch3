//package org.fastcatsearch.ir.config;
//
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlRootElement;
//
//@XmlRootElement(name = "db")
//public class DBSourceConfig extends SingleSourceConfig {
//
//	private String jdbcSourceId;
//	private int fetchSize;
//	private int bulkSize;
//	private String beforeSQL;
//	private String afterSQL;
//	private String dataSQL;
//	private String deleteIdSQL;
//	private boolean resultBuffering;
//	
//	@XmlElement(name="jdbc-source-id")
//	public String getJdbcSourceId() {
//		return jdbcSourceId;
//	}
//	
//	@XmlElement(name="bulksize")
//	public int getBulkSize() {
//		return bulkSize;
//	}
//
//	@XmlElement(name="after-sql")
//	public String getAfterSQL() {
//		return afterSQL;
//	}
//
//	@XmlElement(name="before-sql")
//	public String getBeforeSQL() {
//		return beforeSQL;
//	}
//
//	@XmlElement(name="delete-id-sql")
//	public String getDeleteIdSQL() {
//		return deleteIdSQL;
//	}
//
//	@XmlElement(name="fetchsize")
//	public int getFetchSize() {
//		return fetchSize;
//	}
//
//	@XmlElement(name="data-sql")
//	public String getDataSQL() {
//		return dataSQL;
//	}
//
//	@XmlElement(name="result-bufferring")
//	public boolean isResultBuffering() {
//		return resultBuffering;
//	}
//	
//	public void setJdbcSourceId(String jdbcSourceId) {
//		this.jdbcSourceId = jdbcSourceId;
//	}
//	
//	public void setBulkSize(int bulkSize) {
//		this.bulkSize = bulkSize;
//	}
//
//	public void setAfterSQL(String afterSQL) {
//		this.afterSQL = afterSQL;
//	}
//
//	public void setBeforeSQL(String beforeSQL) {
//		this.beforeSQL = beforeSQL;
//	}
//
//	public void setDeleteIdSQL(String deleteIdSQL) {
//		this.deleteIdSQL = deleteIdSQL;
//	}
//
//	public void setFetchSize(int fetchSize) {
//		this.fetchSize = fetchSize;
//	}
//
//	public void setDataSQL(String dataSQL) {
//		this.dataSQL = dataSQL;
//	}
//
//	public void setResultBuffering(boolean resultBuffering) {
//		this.resultBuffering = resultBuffering;
//	}
//}