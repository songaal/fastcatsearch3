package org.fastcatsearch.ir.config;

import java.text.SimpleDateFormat;

import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.settings.Schema;

public class CollectionContext {
	private String collectionId;
	private CollectionFilePaths collectionFilePaths;
	private Schema schema;
	private Schema workSchema;
	private CollectionConfig collectionConfig;
	private DataSourceConfig dataSourceSetting;
	private CollectionStatus collectionStatus;
	private DataInfo dataInfo;
	
	public CollectionContext(String collectionId, CollectionFilePaths collectionFilePaths) {
		this.collectionId = collectionId;
		this.collectionFilePaths = collectionFilePaths;
	}

	public void init(Schema schema, Schema workSchema, CollectionConfig collectionConfig, DataSourceConfig dataSourceSetting
			, CollectionStatus collectionStatus, DataInfo dataInfo){
		this.schema = schema;
		this.workSchema = workSchema;
		this.collectionConfig = collectionConfig;
		this.dataSourceSetting = dataSourceSetting;
		this.collectionStatus = collectionStatus;
		this.dataInfo = dataInfo;
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
	
	public CollectionConfig collectionConfig(){
		return collectionConfig;
	}
	
	public DataSourceConfig dataSourceSetting(){
		return dataSourceSetting;
	}
	
	public CollectionStatus collectionStatus(){
		return collectionStatus;
	}
	
	public DataInfo dataInfo(){
		return dataInfo;
	}

	public void applyWorkSchemaFile(String collectionId) {
		// TODO Auto-generated method stub
		
	}
	
	public void updateCollectionStatus(IndexingType indexingType, int dataSequence, int count, long starTime, long endTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		
		//TODO  collectionStatus 를 업데이트...
	}
}
