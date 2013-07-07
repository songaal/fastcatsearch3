package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "source")
public class DBReaderConfig extends DataSourceConfig {

	private String driver;
	private String url;
	private String user;
	private String password;
	private int fetchSize;
	private int bulkSize;
	private String beforeIncQuery;
	private String afterIncQuery;
	private String beforeFullQuery;
	private String afterFullQuery;
	private String fullQuery;
	private String incQuery;
	private String deleteIdQuery;
	private String fullBackupPath;
	private String incBackupPath;
	private String backupFileEncoding;
	private boolean resultBuffering;
	
	@XmlElement
	public int getBulkSize() {
		return bulkSize;
	}

	@XmlElement
	public String getAfterIncQuery() {
		return afterIncQuery;
	}

	@XmlElement
	public String getBeforeIncQuery() {
		return beforeIncQuery;
	}

	@XmlElement
	public String getAfterFullQuery() {
		return afterFullQuery;
	}

	@XmlElement
	public String getBeforeFullQuery() {
		return beforeFullQuery;
	}

	@XmlElement
	public String getIncBackupPath() {
		return incBackupPath;
	}

	@XmlElement
	public String getIncQuery() {
		return incQuery;
	}

	@XmlElement
	public String getDeleteIdQuery() {
		return deleteIdQuery;
	}

	@XmlElement
	public String getBackupFileEncoding() {
		return backupFileEncoding;
	}

	@XmlElement
	public String getFullBackupPath() {
		return fullBackupPath;
	}

	@XmlElement
	public int getFetchSize() {
		return fetchSize;
	}

	@XmlElement
	public String getFullQuery() {
		return fullQuery;
	}

	@XmlElement
	public String getJdbcUrl() {
		return url;
	}

	@XmlElement
	public String getJdbcPassword() {
		return password;
	}

	@XmlElement
	public String getJdbcUser() {
		return user;
	}

	@XmlElement
	public String getJdbcDriver() {
		return driver;
	}
	
	@XmlElement
	public boolean isResultBuffering() {
		return resultBuffering;
	}
	
	public void setBulkSize(int bulkSize) {
		this.bulkSize = bulkSize;
	}

	public void setAfterIncQuery(String afterIncQuery) {
		this.afterIncQuery = afterIncQuery;
	}

	public void setBeforeIncQuery(String beforeIncQuery) {
		this.beforeIncQuery = beforeIncQuery;
	}

	public void setAfterFullQuery(String afterFullQuery) {
		this.afterFullQuery = afterFullQuery;
	}

	public void setBeforeFullQuery(String beforeFullQuery) {
		this.beforeFullQuery = beforeFullQuery;
	}

	public void setIncBackupPath(String incBackupPath) {
		this.incBackupPath = incBackupPath;
	}

	public void setIncQuery(String incQuery) {
		this.incQuery = incQuery;
	}

	public void setDeleteIdQuery(String deleteIdQuery) {
		this.deleteIdQuery = deleteIdQuery;
	}

	public void setBackupFileEncoding(String backupFileEncoding) {
		this.backupFileEncoding = backupFileEncoding;
	}

	public void setFullBackupPath(String fullBackupPath) {
		this.fullBackupPath = fullBackupPath;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public void setFullQuery(String fullQuery) {
		this.fullQuery = fullQuery;
	}

	public void setJdbcUrl(String url) {
		this.url = url;
	}

	public void setJdbcPassword(String password) {
		this.password = password;
	}

	public void setJdbcUser(String user) {
		this.user = user;
	}

	public void setJdbcDriver(String driver) {
		this.driver = driver;
	}

	public void setResultBuffering(boolean resultBuffering) {
		this.resultBuffering = resultBuffering;
	}
}