package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * <collections> <collection active="true">sample</collection> <collection
 * active="true">sample2</collection> </collections>
 * 
 * */

@XmlRootElement(name = "collections")
public class CollectionsConfig {

	private List<Collection> collectionList;

	@XmlElement(name = "collection")
	public List<Collection> getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(List<Collection> collectionList) {
		this.collectionList = collectionList;
	}

	public boolean contains(String collectionId) {
		if (collectionList != null) {
			for (int inx = 0; inx < collectionList.size(); inx++) {
				Collection collection = collectionList.get(inx);
				if (collection.getId().equals(collectionId)) {
					return true;
				}
			}
		}
		return false;
	}

	public void addCollection(String collectionId) {
		if (collectionList == null) {
			collectionList = new ArrayList<Collection>();
		}
		collectionList.add(new Collection(collectionId));
	}

	public boolean removeCollection(String collectionId) {
		if (collectionList != null) {
			for (int inx = 0; inx < collectionList.size(); inx++) {
				Collection collection = collectionList.get(inx);
				if (collection.getId().equals(collectionId)) {
					collectionList.remove(inx);
					return true;
				}
			}
		}
		return false;
	}

	public static class Collection {
		private String id;

		// private boolean active;

		public Collection() {
		}

		public Collection(String id) {
			this.id = id;
		}

		@XmlValue
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		// @XmlAttribute(name="active")
		// public boolean isActive() {
		// return active;
		// }
		// public void setActive(boolean active) {
		// this.active = active;
		// }

	}

}
