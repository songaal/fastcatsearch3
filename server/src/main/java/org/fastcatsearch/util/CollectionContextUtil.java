package org.fastcatsearch.util;

import java.io.File;

import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JAXBConfigs;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.settings.SettingFileNames;

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
		JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionStatus), collectionStatus, CollectionStatus.class);
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
		DataSourceConfig dataSourceConfig = loadDataSourceConfig(collectionDir.file(SettingFileNames.datasource));
		//TODO datasource가 여러개이면 config를 List로 가지고 있게 한다. 
		CollectionStatus collectionStatus = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collectionStatus), CollectionStatus.class);
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
	
	private static DataSourceConfig loadDataSourceConfig(File configFile) {
		DataSourceConfig dataSourceConfig = JAXBConfigs.readConfig(configFile, DataSourceConfig.class);
		
		String configType = dataSourceConfig.getConfigType();
		Class<? extends DataSourceConfig> configClass = (Class<? extends DataSourceConfig>) DynamicClassLoader.loadClass(configType);
		return JAXBConfigs.readConfig(configFile, configClass);
	}

	public static void write(CollectionContext collectionContext) {
		CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		
		Schema schema = collectionContext.schema();
		Schema workSchema = collectionContext.workSchema();
		CollectionConfig collectionConfig = collectionContext.collectionConfig();
		CollectionStatus collectionStatus = collectionContext.collectionStatus();
		DataInfo dataInfo = collectionContext.dataInfo();
		DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
		
		File collectionDir = collectionFilePaths.home().file();
		
		if(schema != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schema, Schema.class);
		}
		if(workSchema != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.workSchema), workSchema, Schema.class);
		}
		if(collectionConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collection), collectionConfig, CollectionConfig.class);
		}
		if(collectionConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionStatus), collectionStatus, CollectionStatus.class);
		}
		if(dataInfo != null){
			File dataDir = collectionFilePaths.dataPath(collectionStatus.getDataStatus().getSequence()).file();
			JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
		}
		if(dataSourceConfig != null){
			JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.datasource), dataSourceConfig, DataSourceConfig.class);
		}
		
	}
}
