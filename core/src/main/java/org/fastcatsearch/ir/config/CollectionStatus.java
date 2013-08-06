package org.fastcatsearch.ir.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <collection-status sequence="2"> <index-full documents="50" updates ="1" deletes="20" start="2013-05-20 13:03:32"
 * end="2013-05-20 13:03:32" duration="365ms" /> <index-add documents="5" updates ="1" deletes="20" start="2013-05-20 13:03:32"
 * end="2013-05-20 13:03:32" duration="365ms" /> </collection-status>
 * */
@XmlRootElement(name = "collection-status")
@XmlType(propOrder = { "sequence", "addIndexStatus", "fullIndexStatus" })
public class CollectionStatus {
	private int sequence;
	private IndexStatus fullIndexStatus;
	private IndexStatus addIndexStatus;

	public CollectionStatus copy() {
		CollectionStatus collectionStatus = new CollectionStatus();
		collectionStatus.sequence = sequence;
		if (fullIndexStatus != null) {
			collectionStatus.fullIndexStatus = fullIndexStatus.copy();
		}
		if (addIndexStatus != null) {
			collectionStatus.addIndexStatus = addIndexStatus.copy();
		}
		return collectionStatus;
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
		return "[CollectionStatus] seq[" + sequence + "] last-full=[" + fullIndexStatus + "] last-add=[" + addIndexStatus + "]";
	}

	@XmlAttribute
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getPathName() {
		return "data" + sequence;
	}

	public String getPathName(int seq) {
		if (seq != -1) {
			return "data" + seq;
		} else {
			return getPathName();
		}
	}

	@XmlElement(name = "last-indexing-full")
	public IndexStatus getFullIndexStatus() {
		return fullIndexStatus;
	}

	@XmlElement(name = "last-indexing-add")
	public IndexStatus getAddIndexStatus() {
		return addIndexStatus;
	}

	public void setFullIndexStatus(IndexStatus fullIndexStatus) {
		this.fullIndexStatus = fullIndexStatus;
	}

	public void setAddIndexStatus(IndexStatus addIndexStatus) {
		this.addIndexStatus = addIndexStatus;
	}

	@XmlType(propOrder = { "duration", "endTime", "startTime", "deleteCount", "updateCount", "insertCount", "documentCount" })
	public static class IndexStatus {
		private int documentCount;
		private int insertCount;
		private int updateCount;
		private int deleteCount;
		private String startTime;
		private String endTime;
		private String duration;

		public IndexStatus copy() {
			IndexStatus indexStatus = new IndexStatus();
			indexStatus.documentCount = documentCount;
			indexStatus.insertCount = insertCount;
			indexStatus.updateCount = updateCount;
			indexStatus.deleteCount = deleteCount;
			indexStatus.startTime = startTime;
			indexStatus.endTime = endTime;
			indexStatus.duration = duration;
			return indexStatus;
		}

		@Override
		public String toString() {
			return "[IndexStatus] docs[" + documentCount + "] inserts[" + insertCount  + "] updates[" + updateCount + "] deletes[" + deleteCount + "] start[" + startTime + "]"
					+ "] end[" + endTime + "]" + "] duration[" + duration + "]";
		}

		@XmlAttribute(name = "documents")
		public int getDocumentCount() {
			return documentCount;
		}

		public void setDocumentCount(int documentCount) {
			this.documentCount = documentCount;
		}

		@XmlAttribute(name = "inserts")
		public int getInsertCount() {
			return insertCount;
		}

		public void setInsertCount(int insertCount) {
			this.insertCount = insertCount;
		}
		
		@XmlAttribute(name = "updates")
		public int getUpdateCount() {
			return updateCount;
		}

		public void setUpdateCount(int updateCount) {
			this.updateCount = updateCount;
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
