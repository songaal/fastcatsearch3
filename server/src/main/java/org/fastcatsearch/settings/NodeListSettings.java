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
	
	public int findNodeById(String id) {
		int ret = -1;
		for(int inx=0; inx < nodeList.size(); inx++) {
			NodeSettings node = nodeList.get(inx);
			if(id!=null && id.equals(node.id)) {
				ret = inx;
				break;
			}
		}
		return ret;
	}

	public static class NodeSettings {
		private String id;
		private String name;
		private String address;
		private Integer port;
		private String dataAddress;
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
		public Integer getPort() {
			return port;
		}
		public void setPort(Integer port) {
			this.port = port;
		}

		@XmlAttribute(required = false)
		public String getDataAddress() {
			if ("".equals(dataAddress)) {
				return null;
			}
			return dataAddress;
		}
		public void setDataAddress(String dataAddress) {
			this.dataAddress = dataAddress;
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
