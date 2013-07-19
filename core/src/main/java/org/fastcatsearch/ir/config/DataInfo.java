package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
<data-info documents="7500" deletes="300">
	<segment id="0" base="0" revision="2" documents="6000" deletes="250" createTime="2013-06-15 15:30:00" />
	<segment id="1" base="6000" revision="1" documents="1500" deletes="50" createTime="2013-06-15 16:30:00" />
</data-info>

<data-info documents="7500" deletes="60">
	<segment id="0" base="0" revision="0">
		<revision documents="1000" deletes="0" createTime="2013-06-15 15:20:00">
	</segment>
	<segment id="1" base="1000" revision="2">
		<revision documents="5000" deletes="10" createTime="2013-06-15 16:20:00">
	</segment>
	<segment id="2" base="6000" revision="1">
		<revision documents="1500" deletes="50" createTime="2013-06-15 16:30:00"/>
	</segment>
</data-info>
 * */

@XmlRootElement(name = "data-info")
public class DataInfo {
	private int documents;
	private int deletes;
	private List<SegmentInfo> segmentInfoList;
	
	
	public DataInfo(){ }

	public DataInfo copy(){
		DataInfo dataInfo = new DataInfo();
		dataInfo.documents = this.documents;
		dataInfo.deletes = this.deletes;
		dataInfo.segmentInfoList = new ArrayList<SegmentInfo>();
		dataInfo.segmentInfoList.addAll(segmentInfoList);
		return dataInfo;
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

	@XmlElement(name="segment")
	public List<SegmentInfo> getSegmentInfoList() {
		return segmentInfoList;
	}

	public void setSegmentInfoList(List<SegmentInfo> segmentInfoList) {
		this.segmentInfoList = segmentInfoList;
	}
	
	public int getSegmentSize(){
		return segmentInfoList.size();
	}
	
	public int getLastSegmentNumber(){
		return segmentInfoList.size() - 1;
	}
	public SegmentInfo getLastSegmentInfo(){
		if(segmentInfoList.size() == 0){
			return null;
		}
		return segmentInfoList.get(segmentInfoList.size() - 1);
	}
	
	public String toString(){
		return ("[DataInfo] documents["+documents+"] docCount["+deletes+"] segments["+segmentInfoList+"]");
	}
	
	/**
	 <segment id="0" base="0" revision="2" documents="6000" updates="100" deletes="250" createTime="2013-06-15 15:30:00" />
	 
	 <segment id="0" base="0" revision="0">
		<revision documents="1000" deletes="0" createTime="2013-06-15 15:20:00">
	</segment>
	
	 * */
	@XmlRootElement(name = "segment")
	@XmlType(propOrder = { "createTime", "deleteCount", "updateCount", "documentCount", "revision", "baseNumber", "id" })
	public static class SegmentInfo {
		private String id;
		private int baseNumber;
		private int revision;
		private RevisionInfo revisionInfo;
		
		public SegmentInfo() {
			this.id = "0";
		}
		
		public SegmentInfo(String id, int baseNumber) { 
			this.id = id;
			this.baseNumber = baseNumber;
		}
		
		public String toString(){
			return "[SegmentInfo] id["+id+"] base["+baseNumber+"] revision["+revision+"]";
		}
		
		public void update(int revision, int documents, int deletes, String createTime){
			this.revision = revision;
			revisionInfo.documentCount = documents;
			revisionInfo.deleteCount = deletes;
			revisionInfo.createTime = createTime;
		}
		
		//id와 baseNumber는 변경되지 않는다.
		//TODO 상위 data info 의 문서수도 변경되야 한다.
		public void update(SegmentInfo segmentInfo){
			update(segmentInfo.revision, segmentInfo.revisionInfo.documentCount, segmentInfo.revisionInfo.deleteCount, segmentInfo.revisionInfo.createTime);
		}
		
		@XmlAttribute
		public String getId() {
			return id;
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
		
		@XmlAttribute
		public int getRevision() {
			return revision;
		}
		public void setRevision(int revision) {
			this.revision = revision;
		}
		
		@XmlElement(name = "revision")
		public RevisionInfo getRevisionInfo() {
			return revisionInfo;
		}

		public void setRevisionInfo(RevisionInfo revisionInfo) {
			this.revisionInfo = revisionInfo;
		}

		public String getNextId(){
			return Integer.toString(Integer.parseInt(id) + 1);
		}
		
		public int getNextRevision(){
			return revision + 1;
		}
		
		public int getNextBaseNumber(){
			return baseNumber + revisionInfo.documentCount;
		}
	}
	
	/**
	 <revision documents="1000" deletes="0" createTime="2013-06-15 15:20:00">
	 * */
	@XmlRootElement(name = "revision")
	public static class RevisionInfo {
		
		private int documentCount;
		private int updateCount;
		private int deleteCount;
		private String createTime;
		
		public String toString(){
			return "[RevisionInfo] documents["+documentCount+"] deletes["+deleteCount+"] createTime["+createTime+"]";
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
		
		@XmlAttribute
		public String getCreateTime() {
			return createTime;
		}
		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}
	}

	
}
