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
	
	public int getDataSequence(){
		return collectionStatus.getSequence();
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

	public void updateCollectionStatus(IndexingType indexingType, RevisionInfo revisionInfo, long startTime, long endTime){
		IndexStatus indexStatus = null;
		if(indexingType == IndexingType.FULL_INDEXING){
			indexStatus = collectionStatus.getFullIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionStatus.setFullIndexStatus(indexStatus);
			}
			//전체색인시 증분색인 status는 지워준다.
			collectionStatus.setAddIndexStatus(null);
		}else{
			indexStatus = collectionStatus.getAddIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionStatus.setAddIndexStatus(indexStatus);
			}
		}
		indexStatus.setDocumentCount(revisionInfo.getDocumentCount());
		indexStatus.setInsertCount(revisionInfo.getInsertCount());
		indexStatus.setUpdateCount(revisionInfo.getUpdateCount());
		indexStatus.setDeleteCount(revisionInfo.getDeleteCount());
		indexStatus.setStartTime(Formatter.formatDate(new Date(startTime)));
		indexStatus.setEndTime(Formatter.formatDate(new Date(endTime)));
		indexStatus.setDuration(Formatter.getFormatTime(endTime - startTime));
	}

	public void updateSegmentInfo(SegmentInfo segmentInfo) {
		dataInfo.updateSegmentInfo(segmentInfo);
	}
	public void addSegmentInfo(SegmentInfo segmentInfo) {
		dataInfo.addSegmentInfo(segmentInfo);
	}

	public void clearDataInfoAndStatus() {
		dataInfo.getSegmentInfoList().clear();
		collectionStatus.clear();
	}
}
