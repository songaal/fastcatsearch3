package org.fastcatsearch.ir.setting;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 각 컬렉션별 셋팅을 가지고 있다.
 * collection/컬렉션명/setting.xml
 * 
 * <name>샘플</name>
	<active>false</<active>
	<data-sequence-cycle>2</data-sequence-cycle>
	<index>
		<pk-term-interval>64</pk-term-interval>
		<pk-bucket-size>64K</pk-bucket-size>
		<term-interval>64</term-interval>
		<bucket-size>64K</bucket-size>
		<work-memory-size>128M</work-memory-size>
		<work-bucket-size>256</work-bucket-size>
	</<index>
	<segment>
		<separate-inc-indexing>true</separate-inc-indexing>
		<document-limit>2000000</document-limit>
		<segment-revision-backup-size>2</segment-revision-backup-size>
	</segment>
	<document>
		<read-buffer-size>3M</read-buffer-size>
		<write-buffer-size>3M</write-buffer-size>
		<block-size>8</block-size>
	</document>
	
	<data-plan>
		<index-node>
			<node>node1</node>
		</index-node>
		<data-node>
			<node>node2</node>
			<node>node3</node>
		</data-node>
		<shard-size>1</shard-size>
		<replica-size>*</replica-size>
	</data-plan>
 * */

@XmlRootElement(name = "collection-setting")
@XmlType(propOrder = { 
		"fieldSettingList", "indexSettingList", "sortSettingList", 
		"columnSettingList", "groupSettingList", "filterSettingList", 
})
public class CollectionSetting {

	private boolean isActive;
	private int dataSequenceCycle;
	private int pkTermInterval;
	private int pkBucketSize;

	private int indexTermInterval;
	private int indexBucketSize;
	private int indexWorkBucketSize;
	private int indexWorkMemorySize;
	
	private boolean isSeparateIncIndexing;
	private int documentLimit;
	private int segmentRevisionBackupSize;
	
	private int documentReadBufferSize;
	private int documentWriteBufferSize;
	private int documentBlockSize;
	
	private String indexNode;
	private List<String> dataNodeList;
	private int shardSize;
	private int replicaSize;
	
	@XmlElement(name="active")
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	@XmlElement(name="data-sequence-cycle")
	public int getDataSequenceCycle() {
		return dataSequenceCycle;
	}
	public void setDataSequenceCycle(int dataSequenceCycle) {
		this.dataSequenceCycle = dataSequenceCycle;
	}
	public int getPkTermInterval() {
		return pkTermInterval;
	}
	public void setPkTermInterval(int pkTermInterval) {
		this.pkTermInterval = pkTermInterval;
	}
	public int getPkBucketSize() {
		return pkBucketSize;
	}
	public void setPkBucketSize(int pkBucketSize) {
		this.pkBucketSize = pkBucketSize;
	}
	public int getIndexTermInterval() {
		return indexTermInterval;
	}
	public void setIndexTermInterval(int indexTermInterval) {
		this.indexTermInterval = indexTermInterval;
	}
	public int getIndexBucketSize() {
		return indexBucketSize;
	}
	public void setIndexBucketSize(int indexBucketSize) {
		this.indexBucketSize = indexBucketSize;
	}
	public int getIndexWorkBucketSize() {
		return indexWorkBucketSize;
	}
	public void setIndexWorkBucketSize(int indexWorkBucketSize) {
		this.indexWorkBucketSize = indexWorkBucketSize;
	}
	public int getIndexWorkMemorySize() {
		return indexWorkMemorySize;
	}
	public void setIndexWorkMemorySize(int indexWorkMemorySize) {
		this.indexWorkMemorySize = indexWorkMemorySize;
	}
	public boolean isSeparateIncIndexing() {
		return isSeparateIncIndexing;
	}
	public void setSeparateIncIndexing(boolean isSeparateIncIndexing) {
		this.isSeparateIncIndexing = isSeparateIncIndexing;
	}
	public int getDocumentLimit() {
		return documentLimit;
	}
	public void setDocumentLimit(int documentLimit) {
		this.documentLimit = documentLimit;
	}
	public int getSegmentRevisionBackupSize() {
		return segmentRevisionBackupSize;
	}
	public void setSegmentRevisionBackupSize(int segmentRevisionBackupSize) {
		this.segmentRevisionBackupSize = segmentRevisionBackupSize;
	}
	public int getDocumentReadBufferSize() {
		return documentReadBufferSize;
	}
	public void setDocumentReadBufferSize(int documentReadBufferSize) {
		this.documentReadBufferSize = documentReadBufferSize;
	}
	public int getDocumentWriteBufferSize() {
		return documentWriteBufferSize;
	}
	public void setDocumentWriteBufferSize(int documentWriteBufferSize) {
		this.documentWriteBufferSize = documentWriteBufferSize;
	}
	public int getDocumentBlockSize() {
		return documentBlockSize;
	}
	public void setDocumentBlockSize(int documentBlockSize) {
		this.documentBlockSize = documentBlockSize;
	}
	public String getIndexNode() {
		return indexNode;
	}
	public void setIndexNode(String indexNode) {
		this.indexNode = indexNode;
	}
	public List<String> getDataNodeList() {
		return dataNodeList;
	}
	public void setDataNodeList(List<String> dataNodeList) {
		this.dataNodeList = dataNodeList;
	}
	public int getShardSize() {
		return shardSize;
	}
	public void setShardSize(int shardSize) {
		this.shardSize = shardSize;
	}
	public int getReplicaSize() {
		return replicaSize;
	}
	public void setReplicaSize(int replicaSize) {
		this.replicaSize = replicaSize;
	}
	
}
