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
@XmlType(propOrder = {"documents", "deletes", "segmentInfoList"})
public class DataInfo {
	private static Logger logger = LoggerFactory.getLogger(DataInfo.class);

	private int documents;
	private int deletes;

	private List<SegmentInfo> segmentInfoList;

	public DataInfo() {
		segmentInfoList = Collections.synchronizedList(new ArrayList<SegmentInfo>());
	}

	public DataInfo copy() {
		DataInfo dataInfo = new DataInfo();
		dataInfo.documents = this.documents;
		dataInfo.deletes = this.deletes;
		dataInfo.segmentInfoList = new ArrayList<SegmentInfo>();
		// 복사할때도 modification exception을 피하기 위해 동기화 적용.
		synchronized (segmentInfoList) {
			for (SegmentInfo segmentInfo : segmentInfoList) {
				dataInfo.segmentInfoList.add(segmentInfo.copy());
			}
		}
		dataInfo.segmentInfoList = Collections.synchronizedList(dataInfo.segmentInfoList);
		return dataInfo;
	}

	public void update(int documents, int deletes) {
		this.documents = documents;
		this.deletes = deletes;
	}

	public void addUpdate(int documents, int deletes) {
		this.documents += documents;
		this.deletes += deletes;
	}

	public void addSegmentInfo(SegmentInfo segmentInfo) {
		synchronized (segmentInfoList) {
			logger.debug("#### addSegmentInfo >> {}", segmentInfo);
			segmentInfoList.add(segmentInfo);
			addUpdate(segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount());
		}
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
		synchronized (segmentInfoList) {
			Iterator<SegmentInfo> iter = segmentInfoList.iterator();
			while (iter.hasNext()) {
				SegmentInfo si = iter.next();
				if (si.getId().equals(segmentId)) {
					iter.remove();
				}
			}
		}
    }

    public void updateAll() {
        int documents = 0;
        int deletes = 0;
		synchronized (segmentInfoList) {
            logger.debug("---------------------------------------------");
			for (SegmentInfo segmentInfo : segmentInfoList) {
                logger.debug("seg[{}] : doc[{}] del[{}] live[{}]", segmentInfo.getId(), segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), segmentInfo.getLiveCount());
				documents += segmentInfo.getDocumentCount();
				deletes += segmentInfo.getDeleteCount();
			}

			this.documents = documents;
			this.deletes = deletes;
            logger.debug("Total  : doc[{}] del[{}] live[{}]", documents, deletes, documents - deletes);
            logger.debug("---------------------------------------------");
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
		this.segmentInfoList = Collections.synchronizedList(segmentInfoList);
	}

	public int getSegmentSize() {
		return segmentInfoList.size();
	}

	public SegmentInfo getLatestSegmentInfo() {
		if (segmentInfoList.size() == 0) {
			return null;
		}

		SegmentInfo lastSegmentInfo = null;
		synchronized (segmentInfoList) {
			long maxCreateTime = 0;
			for (SegmentInfo segmentInfo : segmentInfoList) {
				if (maxCreateTime < segmentInfo.getCreateTime()) {
					maxCreateTime = segmentInfo.getCreateTime();
					lastSegmentInfo = segmentInfo;
				}
			}
		}

		return lastSegmentInfo;
	}

	public String toString() {
		return ("[DataInfo] documents[" + documents + "] deletes[" + deletes + "] segments[" + segmentInfoList + "]");
	}

	@XmlRootElement(name = "segment")
	@XmlType(propOrder = { "id", "documentCount", "createTime", "deleteCount", "merged" })
	public static class SegmentInfo implements Comparable<SegmentInfo>{
		private String id;
        private int documentCount;
        private int deleteCount;
        private long createTime;

        private long startTime = System.currentTimeMillis();
        private boolean merged;

        public SegmentInfo() {
			this.id = "a0";
		}

		public SegmentInfo(String id) {
			this.id = id;
		}

        public SegmentInfo(String id, int documentCount, int deleteCount, long createTime) {
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
			segmentInfo.startTime = startTime;
			segmentInfo.merged = merged;
			return segmentInfo;
		}

		public void resetCountInfo() {
            documentCount = 0;
            deleteCount = 0;
		}

		public String toString() {
			return "[SegmentInfo] id[" + id + "] doc[" + documentCount + "] del[" + deleteCount + "] merged[" + merged + "]";
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

        public int getLiveCount() {
            return documentCount - deleteCount;
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

        public void setMerged(boolean merged) {
            this.merged = merged;
        }

        @XmlAttribute
        public boolean isMerged() {
            return merged;
        }

		@Override
		public int compareTo(SegmentInfo o) {
			return id.compareTo(o.id);
		}


    }


}
