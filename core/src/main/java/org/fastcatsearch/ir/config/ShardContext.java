//package org.fastcatsearch.ir.config;
//
//import java.util.Date;
//
//import org.fastcatsearch.ir.common.IndexingType;
//import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
//import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
//import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
//import org.fastcatsearch.ir.util.Formatter;
//import org.fastcatsearch.util.FilePaths;
//
//public class ShardContext {
//	
//	private String collectionId;
//	private String shardId;
//	private FilePaths filePaths;
//	private IndexConfig indexConfig;
//	private DataPlanConfig dataPlanConfig;
//	private ShardConfig shardConfig;
//	private ShardIndexStatus shardIndexStatus;
//	private DataInfo dataInfo;
//	
//	public ShardContext(String collectionId, String shardId, FilePaths filePaths){
//		this.collectionId = collectionId;
//		this.shardId = shardId;
//		this.filePaths = filePaths;
//	}
//	
//	public void init(IndexConfig indexConfig, DataPlanConfig dataPlanConfig, ShardConfig shardConfig
//			, ShardIndexStatus shardIndexStatus, DataInfo dataInfo){
//		this.indexConfig = indexConfig;
//		this.dataPlanConfig = dataPlanConfig;
//		this.shardConfig = shardConfig;
//		this.shardIndexStatus = shardIndexStatus;
//		this.dataInfo = dataInfo;
//	}
//	
//	public FilePaths filePaths(){
//		return filePaths;
//	}
//	public FilePaths indexFilePaths(){
//		return new FilePaths(filePaths.indexDirFile(shardIndexStatus.getSequence()));
//	}
//	public String collectionId(){
//		return collectionId;
//	}
//	
//	public String shardId(){
//		return shardId;
//	}
//	
//	public IndexConfig indexConfig(){
//		return indexConfig;
//	}
//	
//	public DataPlanConfig dataPlanConfig(){
//		return dataPlanConfig;
//	}
//	
//	public ShardConfig shardConfig(){
//		return shardConfig;
//	}
//	
//	public ShardIndexStatus indexStatus(){
//		return shardIndexStatus;
//	}
//	
//	public DataInfo dataInfo(){
//		return dataInfo;
//	}
//	
//	public void updateIndexingStatus(IndexingType indexingType, RevisionInfo revisionInfo, long startTime, long endTime){
//		IndexStatus indexStatus = null;
//		if(indexingType == IndexingType.FULL){
//			indexStatus = shardIndexStatus.getFullIndexStatus();
//			if(indexStatus == null){
//				indexStatus = new IndexStatus();
//				shardIndexStatus.setFullIndexStatus(indexStatus);
//			}
//			//전체색인시 증분색인 status는 지워준다.
//			shardIndexStatus.setAddIndexStatus(null);
//		}else{
//			indexStatus = shardIndexStatus.getAddIndexStatus();
//			if(indexStatus == null){
//				indexStatus = new IndexStatus();
//				shardIndexStatus.setAddIndexStatus(indexStatus);
//			}
//		}
//		indexStatus.setDocumentCount(revisionInfo.getDocumentCount());
//		indexStatus.setInsertCount(revisionInfo.getInsertCount());
//		indexStatus.setUpdateCount(revisionInfo.getUpdateCount());
//		indexStatus.setDeleteCount(revisionInfo.getDeleteCount());
//		indexStatus.setStartTime(Formatter.formatDate(new Date(startTime)));
//		indexStatus.setEndTime(Formatter.formatDate(new Date(endTime)));
//		indexStatus.setDuration(Formatter.getFormatTime(endTime - startTime));
//	}
//	
//	public int nextDataSequence(){
//		int currentDataSequence = shardIndexStatus.getSequence();
//		int dataSequenceCycle = dataPlanConfig.getDataSequenceCycle();
//		int nextDataSequence = (currentDataSequence + 1) % dataSequenceCycle;
//		shardIndexStatus.setSequence(nextDataSequence);
//		return nextDataSequence;
//	}
//	
//	public int getIndexSequence(){
//		return shardIndexStatus.getSequence();
//	}
//	
//	public void updateSegmentInfo(SegmentInfo segmentInfo) {
//		dataInfo.updateSegmentInfo(segmentInfo);
//	}
//	public void addSegmentInfo(SegmentInfo segmentInfo) {
//		dataInfo.addSegmentInfo(segmentInfo);
//	}
//
//	public void clearDataInfoAndStatus() {
//		dataInfo = new DataInfo();
//		shardIndexStatus.clear();
//	}
//
//	public ShardContext copy() {
//		ShardContext newShardContext = new ShardContext(collectionId, shardId, filePaths);
//		newShardContext.indexConfig = indexConfig;
//		newShardContext.dataPlanConfig = dataPlanConfig;
//		newShardContext.shardConfig = shardConfig;
//		newShardContext.shardIndexStatus = shardIndexStatus.copy();
//		newShardContext.dataInfo = dataInfo;
//		return newShardContext;
//	}
//
//
//}
