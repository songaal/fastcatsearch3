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
	private String javascript;
	private String css;
	
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

	@XmlElement
	public String getJavascript() {
		return javascript;
	}

	public void setJavascript(String javascript) {
		this.javascript = javascript;
	}
	
	@XmlElement
	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}


	public static class SearchCategorySetting {
		private String order;
		private String name;
		private String id;
		private String searchQuery;
		private String thumbnailField;
		private String titleField;
		private String bodyField;
		private String bundleField;
		
		@XmlAttribute
		public String getOrder() {
			return order;
		}
		public void setOrder(String order) {
			this.order = order;
		}
		
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
		
		@XmlElement(name="thumbnail-field")
		public String getThumbnailField() {
			return thumbnailField;
		}
		public void setThumbnailField(String thumbnailField) {
			this.thumbnailField = thumbnailField;
		}
		
		@XmlElement(name="title-field")
		public String getTitleField() {
			return titleField;
		}
		public void setTitleField(String titleField) {
			this.titleField = titleField;
		}
		
		@XmlElement(name="body-field")
		public String getBodyField() {
			return bodyField;
		}
		public void setBodyField(String bodyField) {
			this.bodyField = bodyField;
		}
		
		@XmlElement(name="bundle-field")
		public String getBundleField() {
			return bundleField;
		}
		public void setBundleField(String bundleField) {
			this.bundleField = bundleField;
		}
	}
}
