package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
<collections>
	<collection active="true">sample</collection>
	<collection active="true">sample2</collection>
</collections>

 * */

@XmlRootElement(name = "collections")
public class CollectionsConfig {
	
	private List<Collection> collectionList;
	
	@XmlElement(name="collection")
	public List<Collection> getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(List<Collection> collectionList) {
		this.collectionList = collectionList;
	}

	public boolean contains(String collectionId){
		return collectionList.contains(collectionId);
	}
	
	public void addCollection(String id){
		addCollection(id, true);
	}
	public void addCollection(String id, boolean active){
		if(collectionList == null){
			collectionList = new ArrayList<Collection>();
		}
		collectionList.add(new Collection(id,active));
		
	}

	public static class Collection {
		private String id;
		private boolean active;
		
		public Collection(){ }
		
		public Collection(String id, boolean active){
			this.id = id;
			this.active = active;
		}
		
		@XmlAttribute(name="active")
		public boolean isActive() {
			return active;
		}
		
		@XmlValue
		public String getId() {
			return id;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
		public void setId(String id) {
			this.id = id;
		}
		
	}
	
}


