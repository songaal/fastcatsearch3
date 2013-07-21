package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.config.JAXBConfigs;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.settings.SettingFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContextUtil {
	private static final Logger logger = LoggerFactory.getLogger(CollectionContextUtil.class);
	
	public static CollectionContext init(CollectionFilePaths collectionFilePaths) {
		Path collectionDir = new Path(collectionFilePaths.file());
		SchemaSetting schemaSetting = new SchemaSetting();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.schema), schemaSetting, SchemaSetting.class);
		CollectionConfig collectionConfig = new CollectionConfig();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasourceConfig), dataSourceConfig, SingleSourceConfig.class);
		CollectionStatus collectionStatus = new CollectionStatus();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionStatus), collectionStatus, CollectionStatus.class);
		DataInfo dataInfo = new DataInfo();
		JAXBConfigs.writeConfig(new File(collectionFilePaths.dataFile(0), SettingFileNames.dataInfo), dataInfo, DataInfo.class);
		Schema schema = new Schema(schemaSetting);
		CollectionContext collectionContext = new CollectionContext(collectionFilePaths.collectionId(), collectionFilePaths);
		collectionContext.init(schema, null, collectionConfig, dataSourceConfig, collectionStatus, dataInfo);
		return collectionContext;
	}
	
	public static CollectionContext load(CollectionFilePaths collectionFilePaths, Integer dataSequence){
		Path collectionDir = new Path(collectionFilePaths.file());
		File schemaFile = collectionDir.file(SettingFileNames.schema);
		logger.debug("schemaFile >> {}", schemaFile.getAbsolutePath());
		SchemaSetting schemaSetting = JAXBConfigs.readConfig(schemaFile, SchemaSetting.class);
		File workSchemaFile = collectionDir.file(SettingFileNames.workSchema);
		SchemaSetting workSchemaSetting = JAXBConfigs.readConfig(workSchemaFile, SchemaSetting.class);
		CollectionConfig collectionConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collectionConfig), CollectionConfig.class);
		
		File dataSourceConfigFile = collectionDir.file(SettingFileNames.datasourceConfig);
		DataSourceConfig dataSourceConfig = null;
		if(dataSourceConfigFile.exists()){
			dataSourceConfig = JAXBConfigs.readConfig(dataSourceConfigFile, DataSourceConfig.class);
		}else{
			dataSourceConfig = new DataSourceConfig();
		}
		
		CollectionStatus collectionStatus = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collectionStatus), CollectionStatus.class);
		
		if(dataSequence == null){
			//dataSequence가 없으므로 indexedSequence로 선택하여 로딩한다.
			int indexedSequence = collectionStatus.getSequence();
			dataSequence = indexedSequence;
			
		}
		//dataSequence가 null아 아니면 원하는 sequence의 정보를 읽어온다.
		File infoFile = new File(collectionFilePaths.dataFile(dataSequence), SettingFileNames.dataInfo);
		DataInfo dataInfo = null;
		if(infoFile.exists()){
			dataInfo = JAXBConfigs.readConfig(infoFile, DataInfo.class);
		}else{
			logger.info("File not found : {}", infoFile);
			dataInfo = new DataInfo();
		}
		Schema schema = new Schema(schemaSetting);
		Schema workSchema = null;
		if(workSchemaSetting != null){
			workSchema = new Schema(workSchemaSetting);
		}
		CollectionContext collectionContext = new CollectionContext(collectionFilePaths.collectionId(), collectionFilePaths);
		collectionContext.init(schema, workSchema, collectionConfig, dataSourceConfig, collectionStatus, dataInfo);
		return collectionContext;
	}
	
	public static void write(CollectionContext collectionContext) {
		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		
		Schema schema = collectionContext.schema();
		Schema workSchema = collectionContext.workSchema();
		CollectionConfig collectionConfig = collectionContext.collectionConfig();
		CollectionStatus collectionStatus = collectionContext.collectionStatus();
		DataInfo dataInfo = collectionContext.dataInfo();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		
		File collectionDir = collectionFilePaths.file();
		
		if(schema != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schema, Schema.class);
		}
		if(workSchema != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.workSchema), workSchema, Schema.class);
		}
		if(collectionConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
		}
		if(collectionConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionStatus), collectionStatus, CollectionStatus.class);
		}
		if(dataInfo != null){
			File dataDir = collectionFilePaths.dataFile(collectionStatus.getSequence());
			JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
		}
		
		if(dataSourceConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
		}
		
	}
	
	//TODO 색인이 끝나고 상태 저장.
	public static void writeStatus(CollectionContext collectionContext) {
		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		
		Schema schema = collectionContext.schema();
		CollectionStatus collectionStatus = collectionContext.collectionStatus();
		DataInfo dataInfo = collectionContext.dataInfo();
		
		File collectionDir = collectionFilePaths.file();
		
		//TODO schema가 업데이트 되었으면 저장.
//		if(schema != null){
//			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schema, Schema.class);
//		}
//		if(workSchema != null){
			//삭제.
//		}
		if(dataInfo != null){
			File dataDir = collectionFilePaths.dataFile(collectionStatus.getSequence());
			JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
		}
		
	}
}
