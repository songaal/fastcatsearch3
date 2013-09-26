package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <shard-config>
	<name>sample1</name>
	<filter>year&gt;2013&lt;2013 OR price='2000' OR div=(1234,4556,2346,4535,2432)</filter>
	<data-node>
		<node>node1</node>
		<node>node2</node>
	</data-node>
</shard-config>
 * */

@XmlRootElement(name = "shard-config")
public class ShardConfig {
	private String id;
	private String name;
	private String filter;
	private List<String> dataNodeList;
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="filter")
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	@XmlElementWrapper(name="data-node")
	@XmlElement(name="node")
	public List<String> getDataNodeList() {
		return dataNodeList;
	}
	public void setDataNodeList(List<String> dataNodeList) {
		this.dataNodeList = dataNodeList;
	}
}
