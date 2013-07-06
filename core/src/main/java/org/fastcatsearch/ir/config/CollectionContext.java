package org.fastcatsearch.ir.config;

import org.fastcatsearch.ir.settings.Schema;

public class CollectionContext {
	private String collectionId;
	private Schema schema;
	private Schema workSchema;
	private CollectionConfig collectionConfig;
	private CollectionStatus collectionStatus;
	private DataInfo dataInfo;
	
	public CollectionContext(String collectionId) {
		this.collectionId = collectionId;
	}

	public void init(Schema schema, Schema workSchema, CollectionConfig collectionConfig, CollectionStatus collectionStatus, DataInfo dataInfo){
		this.schema = schema;
		this.workSchema = workSchema;
		this.collectionConfig = collectionConfig;
		this.dataInfo = dataInfo;
	}
	
	public String collectionId(){
		return collectionId;
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
	
	public CollectionStatus collectionStatus(){
		return collectionStatus;
	}
	
	public DataInfo dataInfo(){
		return dataInfo;
	}
}
