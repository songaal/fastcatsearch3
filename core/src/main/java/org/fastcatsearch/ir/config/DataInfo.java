package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <data-info documents="7500" updates="15" deletes="60"> <segment id="0" base="0" > <revision id="0" documents="1000"
 * inserts="990" updates="5" deletes="0" createTime="2013-06-15 15:20:00"> </segment> <segment id="1" base="1000" > <revision
 * id="2" documents="5000" inserts="990" updates="5" deletes="10" createTime="2013-06-15 16:20:00"> </segment> <segment id="2"
 * base="6000" > <revision id="1" documents="1500" inserts="990" updates="5" deletes="50" createTime="2013-06-15 16:30:00"/>
 * </segment> </data-info>
 * */

@XmlRootElement(name = "data-info")
@XmlType(propOrder = { "segmentInfoList", "deletes", "updates", "documents" })
public class DataInfo {
	private static Logger logger = LoggerFactory.getLogger(DataInfo.class);

	private int documents;
	private int updates;
	private int deletes;

	// TODO id 순서대로 list에 추가되도록 adapter만들어야한다.
	private List<SegmentInfo> segmentInfoList;

	public DataInfo() {
		segmentInfoList = new ArrayList<SegmentInfo>();
	}

	public DataInfo copy() {
		DataInfo dataInfo = new DataInfo();
		dataInfo.documents = this.documents;
		dataInfo.updates = this.updates;
		dataInfo.deletes = this.deletes;
		dataInfo.segmentInfoList = new ArrayList<SegmentInfo>();
		for (SegmentInfo segmentInfo : segmentInfoList) {
			dataInfo.segmentInfoList.add(segmentInfo.copy());
		}
		return dataInfo;
	}

	public void update(int documents, int updates, int deletes) {
		this.documents = documents;
		this.updates = updates;
		this.deletes = deletes;
	}

	public void addUpdate(int documents, int updates, int deletes) {
		this.documents += documents;
		this.updates += updates;
		this.deletes += deletes;
	}

	public void addSegmentInfo(SegmentInfo segmentInfo) {
		logger.debug("#### addSegmentInfo >> {}", segmentInfo);
		segmentInfoList.add(segmentInfo);
		RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
		addUpdate(revisionInfo.getInsertCount(), revisionInfo.getUpdateCount(), revisionInfo.getDeleteCount());
	}

	public void updateSegmentInfo(SegmentInfo segmentInfo) {
		if (segmentInfoList.contains(segmentInfo)) {
			int index = segmentInfoList.indexOf(segmentInfo);
			SegmentInfo prevSegmentInfo = segmentInfoList.get(index);
			RevisionInfo prevRevisionInfo = prevSegmentInfo.getRevisionInfo();
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			// 리비전이 다르면 추가된 리비전이므로 갯수를 더한다.
			if (revisionInfo.getId() > prevRevisionInfo.getId()) {
				revisionInfo.updateCount += prevRevisionInfo.updateCount;
				revisionInfo.deleteCount += prevRevisionInfo.deleteCount;
				prevSegmentInfo.update(segmentInfo);
			}
		} else {
			addSegmentInfo(segmentInfo);
		}
	}

	@XmlAttribute
	public int getDocuments() {
		return documents;
	}

	public void setDocuments(int documents) {
		this.documents = documents;
	}

	@XmlAttribute
	public int getUpdates() {
		return updates;
	}

	public void setUpdates(int updates) {
		this.updates = updates;
	}

	@XmlAttribute
	public int getDeletes() {
		return deletes;
	}

	public void setDeletes(int deletes) {
		this.deletes = deletes;
	}

	@XmlElement(name = "segment")
	public List<SegmentInfo> getSegmentInfoList() {
		return segmentInfoList;
	}

	public void setSegmentInfoList(List<SegmentInfo> segmentInfoList) {
		this.segmentInfoList = segmentInfoList;
	}

	public int getSegmentSize() {
		return segmentInfoList.size();
	}

	public int getLastSegmentNumber() {
		return segmentInfoList.size() - 1;
	}

	public SegmentInfo getLastSegmentInfo() {
		if (segmentInfoList.size() == 0) {
			return null;
		}
		return segmentInfoList.get(segmentInfoList.size() - 1);
	}

	public String toString() {
		return ("[DataInfo] documents[" + documents + "] updates[" + updates + "] deletes[" + deletes + "] segments[" + segmentInfoList + "]");
	}

	/**
	 * <segment id="0" base="0" revision="0"> <revision documents="1000" updates="5" deletes="0" createTime="2013-06-15 15:20:00"
	 * /> </segment>
	 * */
	@XmlRootElement(name = "segment")
	@XmlType(propOrder = { "revisionInfo", "baseNumber", "id" })
	// , "revision"
	public static class SegmentInfo {
		private String id;
		private int baseNumber;
		// private int revision;
		private RevisionInfo revisionInfo;

		public SegmentInfo() {
			this.id = "0";
			revisionInfo = new RevisionInfo();
		}

		public SegmentInfo(String id, int baseNumber) {
			this.id = id;
			this.baseNumber = baseNumber;
		}

		@Override
		public boolean equals(Object other) {
			// id가 동일하면 같은 SegmentInfo이다.
			return this.id.equals(((SegmentInfo) other).id);
		}

		public SegmentInfo copy() {
			SegmentInfo segmentInfo = new SegmentInfo();
			segmentInfo.id = id;
			segmentInfo.baseNumber = baseNumber;
			// segmentInfo.revision = revision;
			segmentInfo.revisionInfo = revisionInfo.copy();
			return segmentInfo;
		}

		public String toString() {
			return "[SegmentInfo] id[" + id + "] base[" + baseNumber + "] " + revisionInfo;
		}

		public void update(SegmentInfo segmentInfo) {
			this.id = segmentInfo.id;
			this.baseNumber = segmentInfo.baseNumber;
			// this.revision = segmentInfo.revision;
			this.revisionInfo = segmentInfo.revisionInfo;
		}

		// id와 baseNumber는 변경되지 않는다.
		// TODO 상위 data info 의 문서수도 변경되야 한다.
		public void updateRevision(RevisionInfo revisionInfo) {
			logger.debug("updateRevision > {}", revisionInfo);
			if (revisionInfo == null) {
				return;
			}

			// this.revision = revisionInfo.id;
			if (this.revisionInfo != null) {
				// 누적숫자로 유지한다.
				revisionInfo.updateCount += this.revisionInfo.updateCount;
				revisionInfo.deleteCount += this.revisionInfo.deleteCount;
			}
			this.revisionInfo = revisionInfo;
		}

		@XmlAttribute
		public String getId() {
			return id;
		}

		public int getIntId() {
			return Integer.parseInt(id);
		}

		public void setId(String id) {
			this.id = id;
		}

		@XmlAttribute(name = "base")
		public int getBaseNumber() {
			return baseNumber;
		}

		public void setBaseNumber(int baseNumber) {
			this.baseNumber = baseNumber;
		}

		public int getRevision() {
			return revisionInfo.getId();
		}

		public String getRevisionName() {
			return Integer.toString(revisionInfo.getId());
		}

		// public void setRevision(int revision) {
		// this.revision = revision;
		// }

		@XmlElement(name = "revision")
		public RevisionInfo getRevisionInfo() {
			return revisionInfo;
		}

		public void setRevisionInfo(RevisionInfo revisionInfo) {
			this.revisionInfo = revisionInfo;
		}

		public String getNextId() {
			return Integer.toString(Integer.parseInt(id) + 1);
		}

		public int nextRevision() {
			if (revisionInfo != null) {
				return revisionInfo.nextRevision();
			} else {
				revisionInfo = new RevisionInfo();
				return 0;
			}
		}

		public int getNextBaseNumber() {
			return baseNumber + revisionInfo.documentCount;
		}

		public SegmentInfo getNextSegmentInfo() {
			SegmentInfo nextSegmentInfo = new SegmentInfo();
			nextSegmentInfo.id = getNextId();
			nextSegmentInfo.baseNumber = getNextBaseNumber();
			return nextSegmentInfo;
		}
	}

	/**
	 * <revision id="1" ref="0" documents="1000" insertCount="990" updates="10" deletes="0" createTime="2013-06-15 15:20:00">
	 * */
	@XmlType(propOrder = { "createTime", "deleteCount", "updateCount", "insertCount", "documentCount", "ref", "id" })
	@XmlRootElement(name = "revision")
	public static class RevisionInfo {

		private int id;
		private int ref;
		private int documentCount;
		private int insertCount;
		private int updateCount;
		private int deleteCount;
		private String createTime;

		public RevisionInfo() {
		}

		public RevisionInfo(int id, int documentCount, int insertCount, int updateCount, int deleteCount, String createTime) {
			this.id = id;
			this.ref = id;
			this.documentCount = documentCount;
			this.insertCount = insertCount;
			this.updateCount = updateCount;
			this.deleteCount = deleteCount;
			this.createTime = createTime;
		}

		public boolean isAppend(){
			return id > 0;
		}
		public RevisionInfo copy() {
			RevisionInfo revisionInfo = new RevisionInfo();
			revisionInfo.id = id;
			revisionInfo.ref = ref;
			revisionInfo.documentCount = documentCount;
			revisionInfo.insertCount = insertCount;
			revisionInfo.updateCount = updateCount;
			revisionInfo.deleteCount = deleteCount;
			revisionInfo.createTime = createTime;
			return revisionInfo;
		}

		public int nextRevision() {
			id++;
			return id;
		}

//		public void setRefPreviousRevision() {
//			ref--;
//		}

		//ref와 revision을 동일하게 맞춘다.
		public void setRefWithRevision(){
			ref = id;
		}
		public String toString() {
			return "[RevisionInfo] id[" + id + "] ref[" + ref + "] documents[" + documentCount + "] inserts[" + insertCount + "] updates["
					+ updateCount + "] deletes[" + deleteCount + "] createTime[" + createTime + "]";
		}

		@XmlAttribute(name = "id")
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		@XmlAttribute(name = "ref")
		public int getRef() {
			return ref;
		}

		public void setRef(int ref) {
			this.ref = ref;
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

		@XmlAttribute
		public String getCreateTime() {
			return createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}
	}

}
