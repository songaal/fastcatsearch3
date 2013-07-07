package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
<collection-status>
	<data-status sequence="2"/>
	<index-full documents="50" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />
	<index-add documents="5" start="2013-05-20 13:03:32" end="2013-05-20 13:03:32" duration="365ms" />	
</collection-status>
 * */
@XmlRootElement(name = "collection-status")
public class CollectionStatus {
	private DataStatus dataStatus;
	private IndexStatus fullIndexStatus;
	private IndexStatus addIndexStatus;
	
	@XmlElement(name="data-status")
	public DataStatus getDataStatus(){
		return dataStatus;
	}
	
	@XmlElement(name="index-full")
	public IndexStatus getFullIndexStatus(){
		return fullIndexStatus;
	}
	
	@XmlElement(name="index-add")
	public IndexStatus getAddIndexStatus(){
		return addIndexStatus;
	}
	
	public void setDataStatus(DataStatus dataStatus){
		this.dataStatus = dataStatus;
	}
	
	public void setFullIndexStatus(IndexStatus fullIndexStatus){
		this.fullIndexStatus = fullIndexStatus;
	}
	
	public void setAddIndexStatus(IndexStatus addIndexStatus){
		this.addIndexStatus = addIndexStatus;
	}
	
	public class DataStatus {
		private int sequence;
		
		@XmlAttribute(name="data-status")
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
	}
	
	public class IndexStatus {
		private int documentCount;
		private String startTime;
		private String endTime;
		private String duration;
		
		
		@XmlAttribute(name="documents")
		public int getDocumentCount() {
			return documentCount;
		}
		public void setDocumentCount(int documentCount) {
			this.documentCount = documentCount;
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
