package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.fastcatsearch.settings.StatisticsSettings.Category;

@XmlRootElement(name="additional-service")
public class KeywordServiceSettings {
	private List<String> serviceNodeList;
	private List<Category> categoryList;
	
	@XmlElementWrapper(name="service-node-list")
	@XmlElement(name="node")
	public List<String> getServiceNodeList() {
		return serviceNodeList;
	}

	public void setServiceNodeList(List<String> serviceNodeList) {
		this.serviceNodeList = serviceNodeList;
	}
	
	@XmlElementWrapper(name="category-list")
	@XmlElement(name="category", required=false)
	public List<Category> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<Category> categoryList) {
		this.categoryList = categoryList;
	}
	
}
