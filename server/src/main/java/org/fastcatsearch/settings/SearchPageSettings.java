package org.fastcatsearch.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "search-page")
public class SearchPageSettings {
	
	private int totalSearchListSize;
	private int searchListSize;
	private String relateKeywordURL;
	private String realtimePopularKeywordURL;
	private List<SearchCategorySetting> searchCategorySettingList;
	
	@XmlElement(name="total-search-list-size")
	public int getTotalSearchListSize() {
		return totalSearchListSize;
	}

	public void setTotalSearchListSize(int totalSearchListSize) {
		this.totalSearchListSize = totalSearchListSize;
	}
	
	@XmlElement(name="search-list-size")
	public int getSearchListSize() {
		return searchListSize;
	}

	public void setSearchListSize(int searchListSize) {
		this.searchListSize = searchListSize;
	}

	@XmlElement(name="relate-keyword-url")
	public String getRelateKeywordURL() {
		return relateKeywordURL;
	}

	public void setRelateKeywordURL(String relateKeywordURL) {
		this.relateKeywordURL = relateKeywordURL;
	}

	@XmlElement(name="realtime-popular-keyword-url")
	public String getRealtimePopularKeywordURL() {
		return realtimePopularKeywordURL;
	}

	public void setRealtimePopularKeywordURL(String realtimePopularKeywordURL) {
		this.realtimePopularKeywordURL = realtimePopularKeywordURL;
	}

	
	@XmlElementWrapper(name="search-category-list")
	@XmlElement(name="search-category")
	public List<SearchCategorySetting> getSearchCategorySettingList() {
		return searchCategorySettingList;
	}

	public void setSearchCategorySettingList(List<SearchCategorySetting> searchCategorySettingList) {
		this.searchCategorySettingList = searchCategorySettingList;
	}


	public static class SearchCategorySetting {
		private String name;
		private String id;
		private String searchQuery;
		private String titleFieldId;
		private String bodyFieldId;
		private List<String> etcFieldIdList;
		
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		@XmlElement(name="search-query")
		public String getSearchQuery() {
			return searchQuery;
		}
		public void setSearchQuery(String searchQuery) {
			this.searchQuery = searchQuery;
		}
		
		@XmlElement(name="title-field")
		public String getTitleFieldId() {
			return titleFieldId;
		}
		public void setTitleFieldId(String titleFieldId) {
			this.titleFieldId = titleFieldId;
		}
		
		@XmlElement(name="body-field")
		public String getBodyFieldId() {
			return bodyFieldId;
		}
		public void setBodyFieldId(String bodyFieldId) {
			this.bodyFieldId = bodyFieldId;
		}
		
		@XmlElementWrapper(name="etc-field-list")
		@XmlElement(name="etc-field")
		public List<String> getEtcFieldIdList() {
			return etcFieldIdList;
		}
		public void setEtcFieldIdList(List<String> etcFieldIdList) {
			this.etcFieldIdList = etcFieldIdList;
		}
		
	}
}
