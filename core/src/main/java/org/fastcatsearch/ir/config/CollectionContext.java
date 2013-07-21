package org.fastcatsearch.ir.config;

import java.util.Date;

import org.fastcatsearch.ir.common.IndexingType;
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
	
	
	public CollectionContext newCollectionContext(){
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
	
	public int getNextDataSequence(){
		int currentDataSequence = collectionStatus.getSequence();
		int dataSequenceCycle = collectionConfig.getDataPlanConfig().getDataSequenceCycle();
		return (currentDataSequence + 1) % dataSequenceCycle;
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

	public void applyWorkSchema() {
		if(schema != workSchema && workSchema != null){
			logger.debug("applyWorkSchema >> {}", schema);
			logger.debug("applyWorkSchema workSchema>> {}", workSchema);
//			schema.update(workSchema);
		}
	}
	
	public void updateCollectionStatus(IndexingType indexingType, int dataSequence, int totalCount, int updateCount, int deleteCount, long startTime, long endTime){
		collectionStatus.setSequence(dataSequence);
		if(indexingType == IndexingType.FULL_INDEXING){
			collectionStatus.getFullIndexStatus().setDocumentCount(totalCount);
			collectionStatus.getFullIndexStatus().setUpdateCount(updateCount);
			collectionStatus.getFullIndexStatus().setDeleteCount(deleteCount);
			collectionStatus.getFullIndexStatus().setStartTime(Formatter.formatDate(new Date(startTime)));
			collectionStatus.getFullIndexStatus().setEndTime(Formatter.formatDate(new Date(endTime)));
			collectionStatus.getFullIndexStatus().setDuration(Formatter.getFormatTime(endTime - startTime));
			collectionStatus.setAddIndexStatus(null);
		}else{
			collectionStatus.getAddIndexStatus().setDocumentCount(totalCount);
			collectionStatus.getAddIndexStatus().setUpdateCount(updateCount);
			collectionStatus.getAddIndexStatus().setDeleteCount(deleteCount);
			collectionStatus.getAddIndexStatus().setStartTime(Formatter.formatDate(new Date(startTime)));
			collectionStatus.getAddIndexStatus().setEndTime(Formatter.formatDate(new Date(endTime)));
			collectionStatus.getAddIndexStatus().setDuration(Formatter.getFormatTime(endTime - startTime));
		}
	}

	public void addSegmentInfo(SegmentInfo segmentInfo) {
		if(dataInfo == null){
			dataInfo = new DataInfo();
		}
		dataInfo.getSegmentInfoList().add(segmentInfo);
	}		
}
