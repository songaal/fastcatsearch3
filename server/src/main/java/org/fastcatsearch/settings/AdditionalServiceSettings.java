package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="additional-service")
public class AdditionalServiceSettings {
	private List<String> serviceNodeList;
	
	@XmlElementWrapper(name="service-node-list")
	@XmlElement(name="node")
	public List<String> getServiceNodeList() {
		return serviceNodeList;
	}

	public void setServiceNodeList(List<String> serviceNodeList) {
		this.serviceNodeList = serviceNodeList;
	}
	

	
}
