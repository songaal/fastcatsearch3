package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
<collection-status sequence="2">
	<index-full documents="50" updates ="1" deletes="20" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />
	<index-add documents="5" updates ="1" deletes="20" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />	
</collection-status>
 * */
@XmlRootElement(name = "collection-status")
public class CollectionStatus {
	private int sequence;
	private IndexStatus fullIndexStatus;
	private IndexStatus addIndexStatus;
	
	
	public CollectionStatus copy(){
		CollectionStatus collectionStatus = new CollectionStatus();
		collectionStatus.sequence = sequence;
		collectionStatus.fullIndexStatus = fullIndexStatus.copy();
		collectionStatus.addIndexStatus = addIndexStatus.copy();
		return collectionStatus;
	}
	
	@XmlAttribute
	public int getSequence(){
		return sequence;
	}
	
	public void setSequence(int sequence){
		this.sequence = sequence;
	}
	
	public String getPathName(){
		return "data"+sequence;
	}
	
	public String getPathName(int seq){
		if(seq != -1){
			return "data"+seq;
		}else{
			return getPathName();
		}
	}
	
	@XmlElement(name="index-full")
	public IndexStatus getFullIndexStatus(){
		return fullIndexStatus;
	}
	
	@XmlElement(name="index-add")
	public IndexStatus getAddIndexStatus(){
		return addIndexStatus;
	}
	
	public void setFullIndexStatus(IndexStatus fullIndexStatus){
		this.fullIndexStatus = fullIndexStatus;
	}
	
	public void setAddIndexStatus(IndexStatus addIndexStatus){
		this.addIndexStatus = addIndexStatus;
	}
	
	public static class IndexStatus {
		private int documentCount;
		private int updateCount;
		private int deleteCount;
		private String startTime;
		private String endTime;
		private String duration;
		
		public IndexStatus copy(){
			IndexStatus indexStatus = new IndexStatus();
			indexStatus.documentCount = documentCount;
			indexStatus.updateCount = updateCount;
			indexStatus.deleteCount = deleteCount;
			indexStatus.startTime = startTime;
			indexStatus.endTime = endTime;
			indexStatus.duration = duration;
			return indexStatus;
		}
		
		@XmlAttribute(name="documents")
		public int getDocumentCount() {
			return documentCount;
		}
		public void setDocumentCount(int documentCount) {
			this.documentCount = documentCount;
		}
		
		@XmlAttribute(name="updates")
		public int getUpdateCount() {
			return updateCount;
		}
		public void setUpdateCount(int updateCount) {
			this.updateCount = updateCount;
		}
		
		@XmlAttribute(name="deletes")
		public int getDeleteCount() {
			return deleteCount;
		}
		public void setDeleteCount(int deleteCount) {
			this.deleteCount = deleteCount;
		}
		
		@XmlAttribute(name="start")
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		
		@XmlAttribute(name="end")
		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		@XmlAttribute(name="duration")
		public String getDuration() {
			return duration;
		}
		public void setDuration(String duration) {
			this.duration = duration;
		}
		
		
	}
}
