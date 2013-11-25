package org.fastcatsearch.util;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.config.IndexingScheduleConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.settings.SettingFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContextUtil {
	private static final Logger logger = LoggerFactory.getLogger(CollectionContextUtil.class);

	public static CollectionContext init(CollectionConfig collectionConfig, FilePaths indexFilePaths) throws SettingException {
		try {
			Path collectionDir = new Path(indexFilePaths.file());
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
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasourceConfig), dataSourceConfig, SingleSourceConfig.class);
			//status.xml
			CollectionIndexStatus collectionStatus = new CollectionIndexStatus();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.indexStatus), collectionStatus, CollectionIndexStatus.class);
			//info.xml
			DataInfo dataInfo = new DataInfo();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.dataInfo), dataInfo, DataInfo.class);
			//schedule.xml
			IndexingScheduleConfig indexingScheduleConfig = new IndexingScheduleConfig();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.scheduleConfig), indexingScheduleConfig, IndexingScheduleConfig.class);
			
			Schema schema = new Schema(schemaSetting);
			CollectionContext collectionContext = new CollectionContext(indexFilePaths.getId(), indexFilePaths);
			collectionContext.init(schema, null, collectionConfig, indexConfig, dataSourceConfig, collectionStatus, dataInfo, indexingScheduleConfig);
			return collectionContext;
		} catch (Exception e) {
			throw new SettingException("CollectionContext 로드중 에러발생", e);
		}
	}

	public static CollectionContext load(Collection collection, FilePaths indexFilePaths) throws SettingException {
		try {
			String collectionId = collection.getId();
			Path collectionDir = new Path(indexFilePaths.file());
			File schemaFile = collectionDir.file(SettingFileNames.schema);
			SchemaSetting schemaSetting = JAXBConfigs.readConfig(schemaFile, SchemaSetting.class);
			File workSchemaFile = collectionDir.file(SettingFileNames.workSchema);
			SchemaSetting workSchemaSetting = JAXBConfigs.readConfig(workSchemaFile, SchemaSetting.class);
			
			CollectionConfig collectionConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collectionConfig), CollectionConfig.class);
			IndexConfig indexConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.indexConfig), IndexConfig.class);
			
			File dataSourceConfigFile = collectionDir.file(SettingFileNames.datasourceConfig);
			DataSourceConfig dataSourceConfig = null;
			if (dataSourceConfigFile.exists()) {
				dataSourceConfig = JAXBConfigs.readConfig(dataSourceConfigFile, DataSourceConfig.class);
			} else {
				dataSourceConfig = new DataSourceConfig();
			}

			File indexStatusFile = collectionDir.file(SettingFileNames.indexStatus);
			
			CollectionIndexStatus collectionStatus = JAXBConfigs.readConfig(indexStatusFile, CollectionIndexStatus.class);
			
			// dataSequence가 null아 아니면 원하는 sequence의 정보를 읽어온다.
			File dataDir = indexFilePaths.dataFile();
			if (!dataDir.exists()) {
				dataDir.mkdirs();
			}
			
			File dataInfoFile = collectionDir.file(SettingFileNames.dataInfo);
			DataInfo dataInfo = null;
			if(dataInfoFile.exists()){
				dataInfo = JAXBConfigs.readConfig(dataInfoFile, DataInfo.class);
			}else{
				dataInfo = new DataInfo();
				JAXBConfigs.writeConfig(dataInfoFile, dataInfo, DataInfo.class);
			}
			
			File scheduleConfigFile = collectionDir.file(SettingFileNames.scheduleConfig);
			IndexingScheduleConfig indexingScheduleConfig = null;
			if (scheduleConfigFile.exists()) {
				indexingScheduleConfig = JAXBConfigs.readConfig(scheduleConfigFile, IndexingScheduleConfig.class);
			} else {
				indexingScheduleConfig = new IndexingScheduleConfig();
			}
			
			Schema schema = new Schema(schemaSetting);
			Schema workSchema = null;
			if (workSchemaSetting != null) {
				workSchema = new Schema(workSchemaSetting);
			}
			CollectionContext collectionContext = new CollectionContext(indexFilePaths.getId(), indexFilePaths);
			collectionContext.init(schema, workSchema, collectionConfig, indexConfig, dataSourceConfig, collectionStatus, dataInfo, indexingScheduleConfig);
			
			return collectionContext;
		} catch (Exception e) {
			throw new SettingException("CollectionContext 로드중 에러발생", e);
		}
	}

	public static void write(CollectionContext collectionContext) throws SettingException {
		try {
			FilePaths collectionFilePaths = collectionContext.collectionFilePaths();

			Schema schema = collectionContext.schema();
			Schema workSchema = collectionContext.workSchema();
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
			if (workSchema != null && workSchema.schemaSetting() != null) {
				SchemaSetting schemaSetting = schema.schemaSetting();
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.workSchema), schemaSetting, SchemaSetting.class);
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
			if (dataInfo != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
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
	 * data#/{revision}/revision.xml 저장.
	 * 
	 * */
	public static void saveCollectionAfterIndexing(CollectionContext collectionContext) throws SettingException {
		FilePaths collectionFilePaths = collectionContext.collectionFilePaths();

		CollectionIndexStatus collectionStatus = collectionContext.indexStatus();
		DataInfo dataInfo = collectionContext.dataInfo();
		
		FilePaths dataFilePaths = collectionFilePaths.dataPath();

		try {
			if (collectionStatus != null) {
				JAXBConfigs.writeConfig(collectionFilePaths.file(SettingFileNames.indexStatus), collectionStatus, CollectionIndexStatus.class);
			}
			
			if (dataInfo != null) {
				File indexDir = dataFilePaths.indexDirFile(collectionContext.getIndexSequence());
				indexDir.mkdirs();
				logger.debug("Save DataInfo >> {}", dataInfo);
				
				JAXBConfigs.writeConfig(new File(indexDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);

				SegmentInfo lastSegmentInfo = dataInfo.getLastSegmentInfo();
				if(lastSegmentInfo != null) {
					File revisionDir = dataFilePaths.revisionFile(collectionContext.getIndexSequence(), lastSegmentInfo.getId(),
							lastSegmentInfo.getRevision());
					RevisionInfo revisionInfo = lastSegmentInfo.getRevisionInfo();
					if (revisionInfo != null) {
						logger.debug("Save RevisionInfo >> {}, {}", revisionDir.getAbsolutePath(), revisionInfo);
						JAXBConfigs.writeConfig(new File(revisionDir, SettingFileNames.revisionInfo), revisionInfo, RevisionInfo.class);
					}
				}
			}
		} catch (JAXBException e) {
			throw new SettingException("색인후 CollectionContext 저장중 에러발생", e);
		}
	}
	
	// workschema파일이 존재한다면 workschema를 schema로 대치하고
	// schema파일을 저장하고, workschema파일을 지운다.
	public static void applyWorkSchema(CollectionContext collectionContext) throws SettingException {
		FilePaths collectionFilePaths = collectionContext.collectionFilePaths();
		Schema schema = collectionContext.schema();
		Schema workSchema = collectionContext.workSchema();
		File collectionDir = collectionFilePaths.file();

		try {
			logger.debug("applyWorkSchema schema={}", schema);
			logger.debug("applyWorkSchema workSchema={}", workSchema);
			if (workSchema != null && !workSchema.isEmpty()) {
				schema.update(workSchema);
				collectionContext.setWorkSchema(null);
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.schema), schema, Schema.class);
				File workSchemaFile = new File(collectionDir, SettingFileNames.workSchema);
				if (workSchemaFile.exists()) {
					workSchemaFile.delete();
				}
			}
		} catch (JAXBException e) {
			throw new SettingException("WorkSchema 적용중 에러발생", e);
		}

	}
}
