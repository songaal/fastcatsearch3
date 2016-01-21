package org.fastcatsearch.ir.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <data-info documents="7500" updates="15" deletes="60"> <segment id="0"
 * base="0" > <revision id="0" documents="1000" inserts="990" updates="5"
 * deletes="0" createTime="2013-06-15 15:20:00"> </segment> <segment id="1"
 * base="1000" > <revision id="2" documents="5000" inserts="990" updates="5"
 * deletes="10" createTime="2013-06-15 16:20:00"> </segment> <segment id="2"
 * base="6000" > <revision id="1" documents="1500" inserts="990" updates="5"
 * deletes="50" createTime="2013-06-15 16:30:00"/> </segment> </data-info>
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
//		RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
		addUpdate(segmentInfo.getInsertCount(), segmentInfo.getUpdateCount(), segmentInfo.getDeleteCount());
	}

	public void updateSegmentInfo(SegmentInfo segmentInfo) {
		if (segmentInfoList.contains(segmentInfo)) {
			int index = segmentInfoList.indexOf(segmentInfo);
			SegmentInfo prevSegmentInfo = segmentInfoList.get(index);
			//마지막 세그먼트를 덮어쓴다.
			prevSegmentInfo.update(segmentInfo);
//			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			addUpdate(segmentInfo.getInsertCount(), segmentInfo.getUpdateCount(), segmentInfo.getDeleteCount());
			//존재할 경우 리비전만 업데이트한다.
		} else {
			addSegmentInfo(segmentInfo);
		}
	}

    public void removeSegmentInfo(String segmentId) {
        Iterator<SegmentInfo> iter = segmentInfoList.iterator();
        while(iter.hasNext()) {
            SegmentInfo si = iter.next();
            if(si.getId().equals(segmentId)) {
                iter.remove();
            }
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
		//여기서 id가 0,1,2,순으로 정렬이 보장되야한다.
		Collections.sort(segmentInfoList);
		this.segmentInfoList = segmentInfoList;
	}

	public int getSegmentSize() {
		return segmentInfoList.size();
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
	 * <segment id="0" base="0" revision="0"> <revision documents="1000"
	 * updates="5" deletes="0" createTime="2013-06-15 15:20:00" /> </segment>
	 * */
	@XmlRootElement(name = "segment")
	@XmlType(propOrder = { "createTime", "deleteCount", "updateCount", "insertCount", "documentCount", "id" })
	public static class SegmentInfo implements Comparable<SegmentInfo>{
		private String id;
        private int documentCount;
        private int insertCount;
        private int updateCount;
        private int deleteCount;
        private long createTime;

        private long startTime = System.currentTimeMillis();

        public SegmentInfo() {
			this.id = "a0";
		}

		public SegmentInfo(String id) {
			this.id = id;
		}

        public SegmentInfo(String id, String uuid, int documentCount, int insertCount, int updateCount, int deleteCount, long createTime) {
            this.id = id;
            this.documentCount = documentCount;
            this.insertCount = insertCount;
            this.updateCount = updateCount;
            this.deleteCount = deleteCount;
            this.createTime = createTime;
        }

        @Override
		public boolean equals(Object other) {
			// id가 동일하면 같은 SegmentInfo이다.
			return other != null && this.id.equals(((SegmentInfo) other).id);
		}

		public SegmentInfo copy() {
			SegmentInfo segmentInfo = new SegmentInfo(id);
            segmentInfo.documentCount = documentCount;
            segmentInfo.insertCount = insertCount;
            segmentInfo.updateCount = updateCount;
            segmentInfo.deleteCount = deleteCount;
            segmentInfo.createTime = createTime;
			return segmentInfo;
		}

		public void resetCountInfo() {
            documentCount = 0;
            insertCount = 0;
            updateCount = 0;
            deleteCount = 0;
		}

		public String toString() {
			return "[SegmentInfo] id[" + id + "]";
		}

		public void update(SegmentInfo segmentInfo) {
			this.id = segmentInfo.id;
            this.documentCount = segmentInfo.documentCount;
            this.insertCount = segmentInfo.insertCount;
            this.updateCount = segmentInfo.updateCount;
            this.deleteCount = segmentInfo.deleteCount;
            this.createTime = segmentInfo.createTime;
		}

		@XmlAttribute
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

        public int getDocumentCount() {
            return documentCount;
        }

        public void setDocumentCount(int documentCount) {
            this.documentCount = documentCount;
        }

        public int getInsertCount() {
            return insertCount;
        }

        public void setInsertCount(int insertCount) {
            this.insertCount = insertCount;
        }

        public int getUpdateCount() {
            return updateCount;
        }

        public void setUpdateCount(int updateCount) {
            this.updateCount = updateCount;
        }

        public int getDeleteCount() {
            return deleteCount;
        }

        public void setDeleteCount(int deleteCount) {
            this.deleteCount = deleteCount;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public void add(SegmentInfo segmentInfo) {
			this.documentCount += segmentInfo.documentCount;
			this.insertCount += segmentInfo.insertCount;
			this.updateCount += segmentInfo.updateCount;
			this.deleteCount += segmentInfo.deleteCount;
		}

        public long getStartTime() {
            return startTime;
        }

		@Override
		public int compareTo(SegmentInfo o) {
			return id.compareTo(o.id);
		}
	}


}
