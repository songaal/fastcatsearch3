package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datasource")
public class DataSourceConfig {
	private List<DBSourceConfig> dbSourceConfigList;
	private List<FileSourceConfig> fileSourceConfigList;

	@XmlElement(name = "db")
	public List<DBSourceConfig> getDBSourceConfigList() {
		return dbSourceConfigList;
	}

	public void setDBSourceConfigList(List<DBSourceConfig> dbSourceConfigList) {
		this.dbSourceConfigList = dbSourceConfigList;
	}

	@XmlElement(name = "file")
	public List<FileSourceConfig> getFileSourceConfigList() {
		return fileSourceConfigList;
	}

	public void setFileSourceConfigList(List<FileSourceConfig> fileSourceConfigList) {
		this.fileSourceConfigList = fileSourceConfigList;
	}

	public List<SingleSourceConfig> getSourceConfigList() {
		List<SingleSourceConfig> list = new ArrayList<SingleSourceConfig>();
		if (dbSourceConfigList != null) {
			list.addAll(dbSourceConfigList);
		}
		if (fileSourceConfigList != null) {
			list.addAll(fileSourceConfigList);
		}
		return list;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (dbSourceConfigList != null) {
			for (DBSourceConfig sourceConfig : dbSourceConfigList) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(sourceConfig.getClass());
			}
		}
		if (fileSourceConfigList != null) {
			for (FileSourceConfig sourceConfig : fileSourceConfigList) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(sourceConfig.getClass());
			}
		}
		return sb.toString();
	}
}
