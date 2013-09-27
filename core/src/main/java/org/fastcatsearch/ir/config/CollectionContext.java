package org.fastcatsearch.ir.config;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContext {
	protected static Logger logger = LoggerFactory.getLogger(CollectionContext.class);
	
	private String id;
	private FilePaths indexFilePaths;
	private Schema schema;
	private Schema workSchema;
	private CollectionConfig collectionConfig;
	private DataSourceConfig dataSourceConfig;
	private CollectionIndexStatus collectionIndexStatus;
	private Map<String, ShardContext> shardContextMap;
	
	public CollectionContext(String collectionId, FilePaths indexFilePaths) {
		this.id = collectionId;
		this.indexFilePaths = indexFilePaths;
	}

	public void init(Schema schema, Schema workSchema, CollectionConfig collectionConfig, DataSourceConfig dataSourceConfig
			, CollectionIndexStatus collectionStatus){
		this.schema = schema;
		this.workSchema = workSchema;
		this.collectionConfig = collectionConfig;
		this.dataSourceConfig = dataSourceConfig;
		this.collectionIndexStatus = collectionStatus;
		this.shardContextMap = new HashMap<String, ShardContext>();
	}
	
	
	public CollectionContext copy(){
		CollectionContext collectionContext = new CollectionContext(id, indexFilePaths);
		collectionContext.init(schema, workSchema, collectionConfig, /*clusterConfig, */dataSourceConfig, collectionIndexStatus.copy());
		return collectionContext;
	}
	
	public String collectionId(){
		return id;
	}
	
	public FilePaths indexFilePaths(){
		return indexFilePaths;
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
	
	public CollectionConfig collectionConfig(){
		return collectionConfig;
	}
	
	public DataSourceConfig dataSourceConfig(){
		return dataSourceConfig;
	}
	
	public CollectionIndexStatus indexStatus(){
		return collectionIndexStatus;
	}

	public String getLastIndexTime() {
		if (collectionIndexStatus.getAddIndexStatus() != null) {
			return collectionIndexStatus.getAddIndexStatus().getStartTime();
		} else {
			if (collectionIndexStatus.getFullIndexStatus() != null) {
				return collectionIndexStatus.getFullIndexStatus().getStartTime();
			}
		}
		return null;
	}
	
	public void updateCollectionStatus(IndexingType indexingType, RevisionInfo revisionInfo, long startTime, long endTime){
		IndexStatus indexStatus = null;
		if(indexingType == IndexingType.FULL){
			indexStatus = collectionIndexStatus.getFullIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionIndexStatus.setFullIndexStatus(indexStatus);
			}
			//전체색인시 증분색인 status는 지워준다.
			collectionIndexStatus.setAddIndexStatus(null);
		}else{
			indexStatus = collectionIndexStatus.getAddIndexStatus();
			if(indexStatus == null){
				indexStatus = new IndexStatus();
				collectionIndexStatus.setAddIndexStatus(indexStatus);
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

	public Collection<ShardContext> getShardContextList(){
		return shardContextMap.values();
	}

	public ShardContext getShardContext(String shardId) {
		return shardContextMap.get(shardId);
	}
	
	public Map<String, ShardContext> shardContextMap() {
		return shardContextMap;
	}
}
