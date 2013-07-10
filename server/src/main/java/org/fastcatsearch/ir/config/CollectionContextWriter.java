package org.fastcatsearch.ir.config;

import java.io.File;

import org.fastcatsearch.env.CollectionFilePaths;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.settings.SettingFileNames;

public class CollectionContextWriter {

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
