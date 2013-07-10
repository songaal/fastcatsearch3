package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.env.SettingFileNames;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JAXBConfigs;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;

public class CollectionContextUtil {
	
	public static CollectionContext init(CollectionFilePaths collectionFilePaths) {
		Path collectionDir = collectionFilePaths.home();
		SchemaSetting schemaSetting = new SchemaSetting();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.schema), schemaSetting, SchemaSetting.class);
		CollectionConfig collectionConfig = new CollectionConfig();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collection), collectionConfig, CollectionConfig.class);
		DataSourceConfig dataSourceConfig = new DataSourceConfig();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasource), dataSourceConfig, DataSourceConfig.class);
		CollectionStatus collectionStatus = new CollectionStatus();
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.dataStatus), collectionStatus, CollectionStatus.class);
		DataInfo dataInfo = new DataInfo();
		JAXBConfigs.writeConfig(collectionFilePaths.dataPath(0).file(SettingFileNames.dataInfo), dataInfo, DataInfo.class);
		Schema schema = new Schema(schemaSetting);
		CollectionContext collectionContext = new CollectionContext(collectionFilePaths.collectionId(), collectionFilePaths);
		collectionContext.init(schema, null, collectionConfig, dataSourceConfig, collectionStatus, dataInfo);
		return collectionContext;
	}
	
	public static CollectionContext load(CollectionFilePaths collectionFilePaths, int dataSequence){
		Path collectionDir = collectionFilePaths.home();
		SchemaSetting schemaSetting = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.schema), SchemaSetting.class);
		SchemaSetting workSchemaSetting = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.workSchema), SchemaSetting.class);
		CollectionConfig collectionConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collection), CollectionConfig.class);
		DataSourceConfig dataSourceConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.datasource), DataSourceConfig.class);
		CollectionStatus collectionStatus = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.dataStatus), CollectionStatus.class);
		//dataSequence가 -1아 아니면 원하는 sequence의 정보를 읽어온다.
		File infoFile = collectionFilePaths.dataPath(dataSequence).file(SettingFileNames.dataInfo);
		DataInfo dataInfo = null;
		if(infoFile.exists()){
			dataInfo = JAXBConfigs.readConfig(infoFile, DataInfo.class);
		}
		Schema schema = new Schema(schemaSetting);
		Schema workSchema = new Schema(workSchemaSetting);
		CollectionContext collectionContext = new CollectionContext(collectionFilePaths.collectionId(), collectionFilePaths);
		collectionContext.init(schema, workSchema, collectionConfig, dataSourceConfig, collectionStatus, dataInfo);
		return collectionContext;
	}
	
	public static void save(CollectionContext collectionContext){
		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		
		//TODO 해당파일위치에 저장한다.
		
		
		
	}
}
