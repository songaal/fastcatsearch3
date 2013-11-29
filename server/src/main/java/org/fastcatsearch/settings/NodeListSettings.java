package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "node-list")
public class NodeListSettings {
	
	private List<NodeSettings> nodeList;
	
	@XmlElement(name = "node")
	public List<NodeSettings> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<NodeSettings> nodeList) {
		this.nodeList = nodeList;
	}

	public static class NodeSettings {
		private String id;
		private String name;
		private String address;
		private int port;
		private boolean enabled;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@XmlAttribute
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		@XmlAttribute
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
		@XmlAttribute
		public boolean isEnabled() {
			return enabled;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		
	}
}
