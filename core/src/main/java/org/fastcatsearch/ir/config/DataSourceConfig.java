package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datasource")
public class DataSourceConfig {
	
	private List<SingleSourceConfig> fullIndexingSourceConfig;
	private List<SingleSourceConfig> addIndexingSourceConfig;
	
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
}
