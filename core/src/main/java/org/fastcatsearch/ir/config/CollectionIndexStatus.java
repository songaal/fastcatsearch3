package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "collection-index-status")
@XmlType(propOrder = { "fullIndexStatus", "addIndexStatus", "sequence"})
public class CollectionIndexStatus {
	protected IndexStatus fullIndexStatus;
	protected IndexStatus addIndexStatus;
	private int sequence;
	
	public CollectionIndexStatus copy() {
		CollectionIndexStatus collectionIndexStatus = new CollectionIndexStatus();
		collectionIndexStatus.sequence = sequence;
		if (fullIndexStatus != null) {
			collectionIndexStatus.fullIndexStatus = fullIndexStatus.copy();
		}
		if (addIndexStatus != null) {
			collectionIndexStatus.addIndexStatus = addIndexStatus.copy();
		}
		return collectionIndexStatus;
	}
	
	public boolean isEmpty() {
		return fullIndexStatus == null && addIndexStatus == null;
	}

	public void clear() {
		fullIndexStatus = null;
		addIndexStatus = null;
	}
	
	@Override
	public String toString() {
		return "["+getClass().getSimpleName()+"] sequence[" + sequence + "] last-full=[" + fullIndexStatus + "] last-add=[" + addIndexStatus + "]";
	}
	
	@XmlAttribute(name="sequence")
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	@XmlElement(name = "last-full-indexing")
	public IndexStatus getFullIndexStatus() {
		return fullIndexStatus;
	}

	public void setFullIndexStatus(IndexStatus fullIndexStatus) {
		this.fullIndexStatus = fullIndexStatus;
	}
	
	@XmlElement(name = "last-add-indexing")
	public IndexStatus getAddIndexStatus() {
		return addIndexStatus;
	}

	public void setAddIndexStatus(IndexStatus addIndexStatus) {
		this.addIndexStatus = addIndexStatus;
	}
	
	@XmlType(propOrder = { "duration", "endTime", "startTime", "deleteCount", "documentCount" })
	public static class IndexStatus {
		private int documentCount;
		private int deleteCount;
		private String startTime;
		private String endTime;
		private String duration;
		
		public IndexStatus(){
		}
		
		public IndexStatus(int documentCount, int deleteCount, String startTime, String endTime, String duration){
			this.documentCount = documentCount;
			this.deleteCount = deleteCount;
			this.startTime = startTime;
			this.endTime = endTime;
			this.duration = duration;
		}
		
		public IndexStatus copy() {
			IndexStatus indexStatus = new IndexStatus();
			indexStatus.documentCount = documentCount;
			indexStatus.deleteCount = deleteCount;
			indexStatus.startTime = startTime;
			indexStatus.endTime = endTime;
			indexStatus.duration = duration;
			return indexStatus;
		}

		@Override
		public String toString() {
			return "[IndexStatus] docs[" + documentCount + "] deletes[" + deleteCount + "] start[" + startTime + "]"
					+ "] end[" + endTime + "]" + "] duration[" + duration + "]";
		}

		@XmlAttribute(name = "documents")
		public int getDocumentCount() {
			return documentCount;
		}

		public void setDocumentCount(int documentCount) {
			this.documentCount = documentCount;
		}

		@XmlAttribute(name = "deletes")
		public int getDeleteCount() {
			return deleteCount;
		}

		public void setDeleteCount(int deleteCount) {
			this.deleteCount = deleteCount;
		}

		@XmlAttribute(name = "start")
		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		@XmlAttribute(name = "end")
		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		@XmlAttribute(name = "duration")
		public String getDuration() {
			return duration;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

	}
}
