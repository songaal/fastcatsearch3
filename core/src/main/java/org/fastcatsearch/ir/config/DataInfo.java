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
 * */

@XmlRootElement(name = "data-info")
@XmlType(propOrder = { "segmentInfoList", "deletes", "documents" })
public class DataInfo {
	private static Logger logger = LoggerFactory.getLogger(DataInfo.class);

	private int documents;
	private int deletes;

	// TODO id 순서대로 list에 추가되도록 adapter만들어야한다.
	private List<SegmentInfo> segmentInfoList;

	public DataInfo() {
		segmentInfoList = new ArrayList<SegmentInfo>();
	}

	public DataInfo copy() {
		DataInfo dataInfo = new DataInfo();
		dataInfo.documents = this.documents;
		dataInfo.deletes = this.deletes;
		dataInfo.segmentInfoList = new ArrayList<SegmentInfo>();
		for (SegmentInfo segmentInfo : segmentInfoList) {
			dataInfo.segmentInfoList.add(segmentInfo.copy());
		}
		return dataInfo;
	}

	public void update(int documents, int updates, int deletes) {
		this.documents = documents;
		this.deletes = deletes;
	}

	public void addUpdate(int documents, int deletes) {
		this.documents += documents;
		this.deletes += deletes;
	}

	public void addSegmentInfo(SegmentInfo segmentInfo) {
		logger.debug("#### addSegmentInfo >> {}", segmentInfo);
		segmentInfoList.add(segmentInfo);
		addUpdate(segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount());
	}

	public void updateSegmentInfo(SegmentInfo segmentInfo) {
		if (segmentInfoList.contains(segmentInfo)) {
			int index = segmentInfoList.indexOf(segmentInfo);
			SegmentInfo prevSegmentInfo = segmentInfoList.get(index);
			//마지막 세그먼트를 덮어쓴다.
			prevSegmentInfo.update(segmentInfo);
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

    public void updateAll() {
        int documents = 0;
        int deletes = 0;
        for (SegmentInfo segmentInfo : segmentInfoList) {
            documents += segmentInfo.getDocumentCount();
            deletes += segmentInfo.getDeleteCount();
        }
        this.documents = documents;
        this.deletes = deletes;
    }

	@XmlAttribute
	public int getDocuments() {
		return documents;
	}

	public void setDocuments(int documents) {
		this.documents = documents;
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
		return ("[DataInfo] documents[" + documents + "] deletes[" + deletes + "] segments[" + segmentInfoList + "]");
	}

	@XmlRootElement(name = "segment")
	@XmlType(propOrder = { "createTime", "deleteCount", "documentCount", "id" })
	public static class SegmentInfo implements Comparable<SegmentInfo>{
		private String id;
        private int documentCount;
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
            segmentInfo.deleteCount = deleteCount;
            segmentInfo.createTime = createTime;
			return segmentInfo;
		}

		public void resetCountInfo() {
            documentCount = 0;
            deleteCount = 0;
		}

		public String toString() {
			return "[SegmentInfo] id[" + id + "]";
		}

		public void update(SegmentInfo segmentInfo) {
			this.id = segmentInfo.id;
            this.documentCount = segmentInfo.documentCount;
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
