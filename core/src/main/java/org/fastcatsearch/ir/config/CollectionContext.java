package org.fastcatsearch.ir.config;

import java.util.Date;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.CollectionFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContext {
	protected static Logger logger = LoggerFactory.getLogger(CollectionContext.class);
	
	private String collectionId;
	private CollectionFilePaths collectionFilePaths;
	private Schema schema;
	private Schema workSchema;
	private CollectionConfig collectionConfig;
	private DataSourceConfig dataSourceConfig;
	private CollectionStatus collectionStatus;
	private DataInfo dataInfo;
	
	public CollectionContext(String collectionId, CollectionFilePaths collectionFilePaths) {
		this.collectionId = collectionId;
		this.collectionFilePaths = collectionFilePaths;
	}

	public void init(Schema schema, Schema workSchema, CollectionConfig collectionConfig, DataSourceConfig dataSourceConfig
			, CollectionStatus collectionStatus, DataInfo dataInfo){
		this.schema = schema;
		this.workSchema = workSchema;
		this.collectionConfig = collectionConfig;
		this.dataSourceConfig = dataSourceConfig;
		this.collectionStatus = collectionStatus;
		this.dataInfo = dataInfo;
	}
	
	
	public CollectionContext copy(){
		CollectionContext collectionContext = new CollectionContext(collectionId, collectionFilePaths);
		collectionContext.init(schema, workSchema, collectionConfig, dataSourceConfig, collectionStatus.copy(), dataInfo.copy());
		return collectionContext;
	}
	
	public String collectionId(){
		return collectionId;
	}
	
	public CollectionFilePaths collectionFilePaths(){
		return collectionFilePaths;
	}
	
	public Schema schema(){
		return schema;
	}
	
	public Schema workSchema(){
		return workSchema;
	}
	
	public void setWorkSchema(Schema schema){
		workSchema = schema;
	}
	
	public int nextDataSequence(){
		int currentDataSequence = collectionStatus.getSequence();
		int dataSequenceCycle = collectionConfig.getDataPlanConfig().getDataSequenceCycle();
		int nextDataSequence = (currentDataSequence + 1) % dataSequenceCycle;
		collectionStatus.setSequence(nextDataSequence);
		return nextDataSequence;
	}
	
	public String getLastIndexTime(){
		if(collectionStatus.getAddIndexStatus() != null){
			return collectionStatus.getAddIndexStatus().getStartTime();
		}else{
			if(collectionStatus.getFullIndexStatus() != null){
				return collectionStatus.getFullIndexStatus().getStartTime();	
			}
		}
		return null;
	}
	
	public CollectionConfig collectionConfig(){
		return collectionConfig;
	}
	
	public DataSourceConfig dataSourceConfig(){
		return dataSourceConfig;
	}
	
	public CollectionStatus collectionStatus(){
		return collectionStatus;
	}
	
	public DataInfo dataInfo(){
		return dataInfo;
	}

	public void updateCollectionStatus(IndexingType indexingType, int totalCount, int updateCount, int deleteCount, long startTime, long endTime){
		if(indexingType == IndexingType.FULL_INDEXING){
			IndexStatus indexStatus = collectionStatus.getFullIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionStatus.setFullIndexStatus(indexStatus);
			}
			indexStatus.setDocumentCount(totalCount);
			indexStatus.setUpdateCount(updateCount);
			indexStatus.setDeleteCount(deleteCount);
			indexStatus.setStartTime(Formatter.formatDate(new Date(startTime)));
			indexStatus.setEndTime(Formatter.formatDate(new Date(endTime)));
			indexStatus.setDuration(Formatter.getFormatTime(endTime - startTime));
			collectionStatus.setAddIndexStatus(null);
		}else{
			IndexStatus indexStatus = collectionStatus.getAddIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionStatus.setAddIndexStatus(indexStatus);
			}
			indexStatus.setDocumentCount(totalCount);
			indexStatus.setUpdateCount(updateCount);
			indexStatus.setDeleteCount(deleteCount);
			indexStatus.setStartTime(Formatter.formatDate(new Date(startTime)));
			indexStatus.setEndTime(Formatter.formatDate(new Date(endTime)));
			indexStatus.setDuration(Formatter.getFormatTime(endTime - startTime));
		}
	}

	public void addSegmentInfo(SegmentInfo segmentInfo) {
		addSegmentInfo(segmentInfo, false);
	}
	//isFullIndexing == true이면 기존 dataInfo를 없애고 새로 생성.
	public void addSegmentInfo(SegmentInfo segmentInfo, boolean isFullIndexing) {
		
		logger.debug("ADD segmentInfo >> {}", segmentInfo);
		if(isFullIndexing || dataInfo == null){
			dataInfo = new DataInfo();
		}
		dataInfo.addSegmentInfo(segmentInfo);
	}		
}
