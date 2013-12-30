package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="keyword-service")
public class KeywordServiceSettings {
	private List<String> serviceNodeList;
	private List<KeywordServiceCategory> categoryList;
	
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
	public List<KeywordServiceCategory> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<KeywordServiceCategory> categoryList) {
		this.categoryList = categoryList;
	}
	
	
	@XmlType(propOrder = { "serviceRelateKeyword", "popularKeywordServiceType", "servicePopularKeyword", "serviceRealTimePopularKeyword", "name", "id" })
	public static class KeywordServiceCategory {
		private String id;
		private String name;
		private Boolean serviceRealTimePopularKeyword;
		private Boolean servicePopularKeyword;
		private String popularKeywordServiceType;
		private Boolean serviceRelateKeyword;
		
		
		public KeywordServiceCategory(){
		}
		
		public KeywordServiceCategory(String id, String name, Boolean serviceRealTimePopularKeyword, Boolean servicePopularKeyword, String popularKeywordServiceType, Boolean serviceRelateKeyword){
			this.id = id;
			this.name = name;
			this.serviceRealTimePopularKeyword = serviceRealTimePopularKeyword;
			this.servicePopularKeyword = servicePopularKeyword;
			this.popularKeywordServiceType = popularKeywordServiceType;
			this.serviceRelateKeyword = serviceRelateKeyword;
		}
		
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
		public Boolean isServiceRealTimePopularKeyword() {
			return serviceRealTimePopularKeyword;
		}
		public void setServiceRealTimePopularKeyword(Boolean serviceRealTimePopularKeyword) {
			this.serviceRealTimePopularKeyword = serviceRealTimePopularKeyword;
		}
		
		@XmlAttribute
		public Boolean isServicePopularKeyword() {
			return servicePopularKeyword;
		}
		public void setServicePopularKeyword(Boolean servicePopularKeyword) {
			this.servicePopularKeyword = servicePopularKeyword;
		}
		
		@XmlAttribute
		public String getPopularKeywordServiceType() {
			return popularKeywordServiceType;
		}
		public void setPopularKeywordServiceType(String popularKeywordServiceType) {
			this.popularKeywordServiceType = popularKeywordServiceType;
		}
		
		@XmlAttribute
		public Boolean isServiceRelateKeyword() {
			return serviceRelateKeyword;
		}
		public void setServiceRelateKeyword(Boolean serviceRelateKeyword) {
			this.serviceRelateKeyword = serviceRelateKeyword;
		}
		
		
	}
}
