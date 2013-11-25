//package org.fastcatsearch.ir.config;
//
//import java.util.List;
//
//import javax.xml.bind.annotation.XmlAttribute;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlElementWrapper;
//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlType;
//
///**
// * 
// * <shard-config>
// 	<id>sample1</id>
//	<name>sample1</name>
//	<filter>year&gt;2013&lt;2013 OR price='2000' OR div=(1234,4556,2346,4535,2432)</filter>
//	<data-node>
//		<node>node1</node>
//		<node>node2</node>
//	</data-node>
//</shard-config>
// * */
//
//@XmlRootElement(name = "shard-config")
//@XmlType(propOrder={"id", "name", "filter", "dataNodeList"})
//public class ShardConfig {
//	
//	public static final String BASE_SHARD_ID = "BASE";
//	
//	private String id;
//	private String name;
//	private String filter;
//	private List<String> dataNodeList;
//	
//	@XmlElement(required = true)
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
//	
//	@XmlElement(name="name")
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	
//	@XmlElement(name="filter")
//	public String getFilter() {
//		return filter;
//	}
//	public void setFilter(String filter) {
//		this.filter = filter;
//	}
//	
//	@XmlElementWrapper(name="data-node")
//	@XmlElement(name="node")
//	public List<String> getDataNodeList() {
//		return dataNodeList;
//	}
//	public void setDataNodeList(List<String> dataNodeList) {
//		this.dataNodeList = dataNodeList;
//	}
//}
