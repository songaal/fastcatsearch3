package org.fastcatsearch.ir.config;

import java.util.Date;
import java.util.List;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.ClusterConfig.ShardClusterConfig;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.util.IndexFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContext {
	protected static Logger logger = LoggerFactory.getLogger(CollectionContext.class);
	
	private String id;
	private IndexFilePaths indexFilePaths;
	private Schema schema;
	private Schema workSchema;
	private CollectionConfig collectionConfig;
	private ClusterConfig clusterConfig;
	private DataSourceConfig dataSourceConfig;
	private CollectionIndexStatus collectionStatus;
	//TODO MAP???
	private List<ShardContext> shardContextList;
	
	public CollectionContext(String collectionId, IndexFilePaths indexFilePaths) {
		this.id = collectionId;
		this.indexFilePaths = indexFilePaths;
	}

	public void init(Schema schema, Schema workSchema, CollectionConfig collectionConfig, ClusterConfig clusterConfig, DataSourceConfig dataSourceConfig
			, CollectionIndexStatus collectionStatus){
		this.schema = schema;
		this.workSchema = workSchema;
		this.collectionConfig = collectionConfig;
		this.clusterConfig = clusterConfig;
		this.dataSourceConfig = dataSourceConfig;
		this.collectionStatus = collectionStatus;
	}
	
	
	public CollectionContext copy(){
		CollectionContext collectionContext = new CollectionContext(id, indexFilePaths);
		collectionContext.init(schema, workSchema, collectionConfig, clusterConfig, dataSourceConfig, collectionStatus.copy());
		return collectionContext;
	}
	
	public String collectionId(){
		return id;
	}
	
	public IndexFilePaths indexFilePaths(){
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
	
	public ClusterConfig clusterConfig(){
		return clusterConfig;
	}
	
	public DataSourceConfig dataSourceConfig(){
		return dataSourceConfig;
	}
	
	public CollectionIndexStatus collectionStatus(){
		return collectionStatus;
	}

	public void updateCollectionStatus(IndexingType indexingType, RevisionInfo revisionInfo, long startTime, long endTime){
		IndexStatus indexStatus = null;
		if(indexingType == IndexingType.FULL){
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

	

	public ShardContext getShardContext(String shardId) {
		// TODO Auto-generated method stub
		return null;
	}
}
