package org.fastcatsearch.util;

import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.*;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.IndexingScheduleConfig.IndexingSchedule;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.settings.SettingFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;

public class CollectionContextUtil {
	private static final Logger logger = LoggerFactory.getLogger(CollectionContextUtil.class);

	public synchronized static CollectionContext create(CollectionConfig collectionConfig, FilePaths collectionFilePaths) throws SettingException {
		try {
			Path collectionDir = new Path(collectionFilePaths.file());
			//collection config.xml
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
			//schema.xml
			SchemaSetting schemaSetting = new SchemaSetting();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.schema), schemaSetting, SchemaSetting.class);
			//default index-config.xml
			IndexConfig indexConfig = IndexConfig.defaultConfig;
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.indexConfig), IndexConfig.defaultConfig, IndexConfig.class);
			//datasource.xml
			DataSourceConfig dataSourceConfig = new DataSourceConfig();
			dataSourceConfig.setFullIndexingSourceConfig(new ArrayList<SingleSourceConfig>());
			dataSourceConfig.setAddIndexingSourceConfig(new ArrayList<SingleSourceConfig>());
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
			//status.xml
			CollectionIndexStatus collectionStatus = new CollectionIndexStatus();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.indexStatus), collectionStatus, CollectionIndexStatus.class);
			//info.xml
			int dataSequence = collectionStatus.getSequence();
			FilePaths indexFilePaths = collectionFilePaths.dataPaths().indexFilePaths(dataSequence);
			if (!indexFilePaths.file().exists()) {
				indexFilePaths.file().mkdirs();
			}
			DataInfo dataInfo = new DataInfo();
			JAXBConfigs.writeConfig(indexFilePaths.file(SettingFileNames.dataInfo), dataInfo, DataInfo.class);
			//schedule.xml
			IndexingScheduleConfig indexingScheduleConfig = new IndexingScheduleConfig();
			indexingScheduleConfig.setFullIndexingSchedule(IndexingSchedule.DefaultIndexingSchedule);
			indexingScheduleConfig.setAddIndexingSchedule(IndexingSchedule.DefaultIndexingSchedule);
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.scheduleConfig), indexingScheduleConfig, IndexingScheduleConfig.class);
			
			Schema schema = new Schema(schemaSetting);
			CollectionContext collectionContext = new CollectionContext(collectionFilePaths.getId(), collectionFilePaths);
			collectionContext.init(schema, null, collectionConfig, indexConfig, dataSourceConfig, collectionStatus, dataInfo, indexingScheduleConfig);
			return collectionContext;
		} catch (Exception e) {
			throw new SettingException("CollectionContext 로드중 에러발생", e);
		}
	}
	

	public synchronized static boolean writeConfigFile(Object configObject, FilePaths collectionFilePaths) {
		if(!collectionFilePaths.file().exists()) {
			collectionFilePaths.file().mkdirs();
		}
		
		Path collectionDir = new Path(collectionFilePaths.file());
		try {
			if (configObject instanceof CollectionConfig) {
				CollectionConfig collectionConfig = (CollectionConfig) configObject;
				JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
			}else if (configObject instanceof DataSourceConfig) {
				DataSourceConfig dataSourceConfig = (DataSourceConfig) configObject;
				JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
			}
		} catch (JAXBException e) {
			logger.error("", e);
			return false;
		}
		return true;
	}
	
	
	public synchronized static CollectionContext load(Collection collection, FilePaths collectionFilePaths) throws SettingException {
		try {
			String collectionId = collection.getId();
			Path collectionPath = new Path(collectionFilePaths.file());
			File schemaFile = collectionPath.file(SettingFileNames.schema);
			SchemaSetting schemaSetting = JAXBConfigs.readConfig(schemaFile, SchemaSetting.class);
			File workSchemaFile = collectionPath.file(SettingFileNames.workSchema);
			SchemaSetting workSchemaSetting = JAXBConfigs.readConfig(workSchemaFile, SchemaSetting.class);
			
			CollectionConfig collectionConfig = JAXBConfigs.readConfig(collectionPath.file(SettingFileNames.collectionConfig), CollectionConfig.class);
			IndexConfig indexConfig = JAXBConfigs.readConfig(collectionPath.file(SettingFileNames.indexConfig), IndexConfig.class);
			
			File dataSourceConfigFile = collectionPath.file(SettingFileNames.datasourceConfig);
			DataSourceConfig dataSourceConfig = null;
			if (dataSourceConfigFile.exists() && dataSourceConfigFile.length() > 0) {
				dataSourceConfig = JAXBConfigs.readConfig(dataSourceConfigFile, DataSourceConfig.class);
			} else {
				dataSourceConfig = new DataSourceConfig();
			}

			File indexStatusFile = collectionPath.file(SettingFileNames.indexStatus);
			
			CollectionIndexStatus collectionStatus = JAXBConfigs.readConfig(indexStatusFile, CollectionIndexStatus.class);
			
			int dataSequence = collectionStatus.getSequence();
			FilePaths indexFilePaths = collectionFilePaths.dataPaths().indexFilePaths(dataSequence);
			if (!indexFilePaths.file().exists()) {
				indexFilePaths.file().mkdirs();
			}
			File dataInfoFile = indexFilePaths.file(SettingFileNames.dataInfo);
			logger.debug("load dataInfoFile > {}", dataInfoFile.getAbsolutePath());
			DataInfo dataInfo = null;
			if(dataInfoFile.exists() && dataInfoFile.length() > 0){
				dataInfo = JAXBConfigs.readConfig(dataInfoFile, DataInfo.class);
			}else{
				dataInfo = new DataInfo();
				JAXBConfigs.writeConfig(dataInfoFile, dataInfo, DataInfo.class);
			}
			
			File scheduleConfigFile = collectionPath.file(SettingFileNames.scheduleConfig);
			IndexingScheduleConfig indexingScheduleConfig = null;
			if (scheduleConfigFile.exists() && scheduleConfigFile.length() > 0) {
				indexingScheduleConfig = JAXBConfigs.readConfig(scheduleConfigFile, IndexingScheduleConfig.class);
			} else {
				indexingScheduleConfig = new IndexingScheduleConfig();
			}
			
			Schema schema = new Schema(schemaSetting);
			CollectionContext collectionContext = new CollectionContext(collection.getId(), collectionFilePaths);
			collectionContext.init(schema, workSchemaSetting, collectionConfig, indexConfig, dataSourceConfig, collectionStatus, dataInfo, indexingScheduleConfig);
			
			return collectionContext;
		} catch (Exception e) {
			throw new SettingException("CollectionContext 로드중 에러발생", e);
		}
	}

	public synchronized static void write(CollectionContext collectionContext) throws SettingException {
		try {
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();

			Schema schema = collectionContext.schema();
			SchemaSetting workSchemaSetting = collectionContext.workSchemaSetting();
			IndexConfig indexConfig = collectionContext.indexConfig();
			CollectionConfig collectionConfig = collectionContext.collectionConfig();
			CollectionIndexStatus collectionStatus = collectionContext.indexStatus();
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			DataInfo dataInfo = collectionContext.dataInfo();
			IndexingScheduleConfig indexingScheduleConfig = collectionContext.indexingScheduleConfig();
			
			File collectionDir = collectionFilePaths.file();

			if (schema != null && schema.schemaSetting() != null) {
				SchemaSetting schemaSetting = schema.schemaSetting();
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schemaSetting, SchemaSetting.class);
			}
			if (workSchemaSetting != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.workSchema), workSchemaSetting, SchemaSetting.class);
			}
			if(indexConfig != null){
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexConfig), indexConfig, IndexConfig.class);
			}
			if (collectionConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
			}
			if (collectionStatus != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexStatus), collectionStatus, CollectionIndexStatus.class);
			}
			if (dataSourceConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
			}
			int dataSequence = collectionStatus.getSequence();
			FilePaths indexFilePaths = collectionFilePaths.dataPaths().indexFilePaths(dataSequence);
			if (!indexFilePaths.file().exists()) {
				indexFilePaths.file().mkdirs();
			}
			if (dataInfo != null) {
				JAXBConfigs.writeConfig(indexFilePaths.file(SettingFileNames.dataInfo), dataInfo, DataInfo.class);
			}
			if (indexingScheduleConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.scheduleConfig), indexingScheduleConfig, IndexingScheduleConfig.class);
			}
		} catch (Exception e) {
			throw new SettingException("CollectionContext 저장중 에러발생", e);
		}

	}

	/**
	 * status.xml 저장.
	 * data#//info.xml 저장.
	 *
	 * */
	public synchronized static void saveCollectionAfterIndexing(CollectionContext collectionContext) throws SettingException {
		FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		
		FilePaths dataFilePaths = collectionFilePaths.dataPaths();
		File collectionDir = collectionFilePaths.file();
		
		try {
			Schema schema = collectionContext.schema();
			if (schema != null && schema.schemaSetting() != null) {
				SchemaSetting schemaSetting = schema.schemaSetting();
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schemaSetting, SchemaSetting.class);
			}

			IndexConfig indexConfig = collectionContext.indexConfig();
			if(indexConfig != null){
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexConfig), indexConfig, IndexConfig.class);
			}
			
			CollectionConfig collectionConfig = collectionContext.collectionConfig();
			if (collectionConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
			}
			
			CollectionIndexStatus collectionStatus = collectionContext.indexStatus();
			if (collectionStatus != null) {
				JAXBConfigs.writeConfig(collectionFilePaths.file(SettingFileNames.indexStatus), collectionStatus, CollectionIndexStatus.class);
			}

			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();
			if (dataSourceConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
			}
			
			DataInfo dataInfo = collectionContext.dataInfo();
			if (dataInfo != null) {
				File indexDir = dataFilePaths.indexDirFile(collectionContext.getIndexSequence());
                if(!indexDir.exists()) {
                    indexDir.mkdirs();
                }
				logger.debug("Save DataInfo >> {}", dataInfo);
				JAXBConfigs.writeConfig(new File(indexDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
			}
			
			File workSchemaFile = collectionFilePaths.file(SettingFileNames.workSchema);
			if (workSchemaFile.exists()) {
				workSchemaFile.delete();
			}
		} catch (JAXBException e) {
			throw new SettingException("색인후 CollectionContext 저장중 에러발생", e);
		}
	}

    public synchronized static void saveCollectionAfterDynamicIndexing(CollectionContext collectionContext) throws SettingException {
        FilePaths collectionFilePaths = collectionContext.collectionFilePaths();

        FilePaths dataFilePaths = collectionFilePaths.dataPaths();
        try {
            DataInfo dataInfo = collectionContext.dataInfo();
            if (dataInfo != null) {
                File indexDir = dataFilePaths.indexDirFile(collectionContext.getIndexSequence());
                if(!indexDir.exists()) {
                    indexDir.mkdirs();
                }
                /* 2019.6.27 swsong: 설정을 저장하는도중에 세그먼트 정보가 수시로 변경될수 있으므로, 복사본을 만들어서 저장한다.*/
				dataInfo = dataInfo.copy();
                logger.debug("Save DataInfo >> {}", dataInfo);
                JAXBConfigs.writeConfig(new File(indexDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
            }
        } catch (JAXBException e) {
            throw new SettingException("동적색인후 CollectionContext 저장중 에러발생", e);
        }
    }
	
}
