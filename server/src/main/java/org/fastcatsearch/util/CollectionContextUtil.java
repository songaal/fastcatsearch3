package org.fastcatsearch.util;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.ClusterConfig;
import org.fastcatsearch.ir.config.ClusterConfig.ShardClusterConfig;
import org.fastcatsearch.ir.config.CollectionConfig;
import org.fastcatsearch.ir.config.CollectionConfig.Shard;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.ShardConfig;
import org.fastcatsearch.ir.config.ShardContext;
import org.fastcatsearch.ir.config.ShardIndexStatus;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.settings.SettingFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionContextUtil {
	private static final Logger logger = LoggerFactory.getLogger(CollectionContextUtil.class);

	public static CollectionContext init(FilePaths indexFilePaths) throws SettingException {
		try {
			Path collectionDir = new Path(indexFilePaths.file());
			SchemaSetting schemaSetting = new SchemaSetting();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.schema), schemaSetting, SchemaSetting.class);
			CollectionConfig collectionConfig = new CollectionConfig();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.collectionConfig), collectionConfig, CollectionConfig.class);
//			ClusterConfig clusterConfig = new ClusterConfig();
//			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.clusterConfig), clusterConfig, ClusterConfig.class);
			DataSourceConfig dataSourceConfig = new DataSourceConfig();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.datasourceConfig), dataSourceConfig, SingleSourceConfig.class);
			ShardIndexStatus collectionStatus = new ShardIndexStatus();
			JAXBConfigs.writeConfig(collectionDir.file(SettingFileNames.indexStatus), collectionStatus, ShardIndexStatus.class);
//			DataInfo dataInfo = new DataInfo();
//			JAXBConfigs.writeConfig(new File(collectionFilePaths.dataFile(0), SettingFileNames.dataInfo), dataInfo, DataInfo.class);
			Schema schema = new Schema(schemaSetting);
			CollectionContext collectionContext = new CollectionContext(indexFilePaths.getId(), indexFilePaths);
			collectionContext.init(schema, null, collectionConfig, dataSourceConfig, collectionStatus);
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

//			logger.debug("dataInfo.getSegmentInfoList() >> {}", dataInfo.getSegmentInfoList().size());
			if (!collectionStatus.isEmpty()) {
				// SegmentInfoList가 없다면 data디렉토리를 지웠거나 색인이 안된상태이므로, 확인차 status초기화해준다.
				collectionStatus.clear();
				JAXBConfigs.writeConfig(indexStatusFile, collectionStatus, ShardIndexStatus.class);
			}

			Schema schema = new Schema(schemaSetting);
			Schema workSchema = null;
			if (workSchemaSetting != null) {
				workSchema = new Schema(workSchemaSetting);
			}
			CollectionContext collectionContext = new CollectionContext(indexFilePaths.getId(), indexFilePaths);
			collectionContext.init(schema, workSchema, collectionConfig, dataSourceConfig, collectionStatus);
			
			
			/*
			 * Load shard context
			 * */
			logger.debug("collectionConfig.getShardConfigList() size = {}", collectionConfig.getShardConfigList().size());
			
			for(Shard shard : collectionConfig.getShardConfigList()){
				String shardId = shard.getId();
				FilePaths shardIndexFilePaths = indexFilePaths.shard(shardId);
//				Path shardDir = new Path(shardIndexFilePaths.file());
				ShardContext shardContext = new ShardContext(collectionId, shardId, shardIndexFilePaths);
				//1. load ShardConfig
				File shardConfigFile = shardIndexFilePaths.file(SettingFileNames.shardConfig);
				ShardConfig shardConfig = JAXBConfigs.readConfig(shardConfigFile, ShardConfig.class);
				
				
				//2. load ShardIndexStatus
				File shardIndexStatusFile = shardIndexFilePaths.file(SettingFileNames.indexStatus);
				ShardIndexStatus shardIndexStatus = JAXBConfigs.readConfig(shardIndexStatusFile, ShardIndexStatus.class);
				
				File indexDir = shardIndexFilePaths.indexDirFile(shardIndexStatus.getSequence());
				if (!indexDir.exists()) {
					indexDir.mkdirs();
				}
				File infoFile = new File(indexDir, SettingFileNames.dataInfo);
				DataInfo dataInfo = null;
				if (infoFile.exists()) {
					dataInfo = JAXBConfigs.readConfig(infoFile, DataInfo.class);
				} else {
					logger.info("File not found : {}", infoFile);
					dataInfo = new DataInfo();
					JAXBConfigs.writeConfig(infoFile, dataInfo, DataInfo.class);
				}
				
				shardContext.init(collectionConfig.getIndexConfig(), collectionConfig.getDataPlanConfig(), shardConfig, shardIndexStatus, dataInfo);
				logger.debug("shard : {} >> {}", shardId, shardContext);
				collectionContext.shardContextMap().put(shardId, shardContext);
			}
			
			
			return collectionContext;
		} catch (Exception e) {
			throw new SettingException("CollectionContext 로드중 에러발생", e);
		}
	}

	public static void write(CollectionContext collectionContext) throws SettingException {
		try {
			FilePaths collectionFilePaths = collectionContext.indexFilePaths();

			Schema schema = collectionContext.schema();
			Schema workSchema = collectionContext.workSchema();
			CollectionConfig collectionConfig = collectionContext.collectionConfig();
			CollectionIndexStatus collectionStatus = collectionContext.indexStatus();
//			DataInfo dataInfo = collectionContext.dataInfo();
			DataSourceConfig dataSourceConfig = collectionContext.dataSourceConfig();

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
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexStatus), collectionStatus, ShardIndexStatus.class);
			}
//			if (dataInfo != null) {
//				File dataDir = collectionFilePaths.dataFile(collectionStatus.getSequence());
//				dataDir.mkdirs();
//				JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
//			}

			if (dataSourceConfig != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.datasourceConfig), dataSourceConfig, DataSourceConfig.class);
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
		FilePaths collectionFilePaths = collectionContext.indexFilePaths();

		CollectionIndexStatus collectionStatus = collectionContext.indexStatus();
//		DataInfo dataInfo = collectionContext.dataInfo();

		File collectionDir = collectionFilePaths.file();

		try {
			if (collectionStatus != null) {
				JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.indexStatus), collectionStatus, ShardIndexStatus.class);
			}
//			if (dataInfo != null) {
//				File dataDir = collectionFilePaths.dataFile(collectionStatus.getSequence());
//				dataDir.mkdirs();
//				logger.debug("Save DataInfo >> {}", dataInfo);
//				JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
//
//				SegmentInfo lastSegmentInfo = dataInfo.getLastSegmentInfo();
//				File revisionDir = collectionFilePaths.revisionFile(collectionStatus.getSequence(), lastSegmentInfo.getId(),
//						lastSegmentInfo.getRevision());
//				RevisionInfo revisionInfo = lastSegmentInfo.getRevisionInfo();
//				if (revisionInfo != null) {
//					logger.debug("Save RevisionInfo >> {}, {}", revisionDir.getAbsolutePath(), revisionInfo);
//					JAXBConfigs.writeConfig(new File(revisionDir, SettingFileNames.revisionInfo), revisionInfo, RevisionInfo.class);
//				}
//			}
			
			for(ShardContext shardContext : collectionContext.getShardContextList()){
				saveShardAfterIndexing(shardContext);
			}
		} catch (JAXBException e) {
			throw new SettingException("색인후 CollectionContext 저장중 에러발생", e);
		}
	}
	
	public static void saveShardAfterIndexing(ShardContext shardContext) throws SettingException {
		FilePaths indexFilePaths = shardContext.filePaths();

		ShardIndexStatus shardIndexStatus = shardContext.indexStatus();
		DataInfo dataInfo = shardContext.dataInfo();

		try {
			if (shardIndexStatus != null) {
				JAXBConfigs.writeConfig(indexFilePaths.file(SettingFileNames.indexStatus), shardIndexStatus, ShardIndexStatus.class);
			}
			if (dataInfo != null) {
				File indexDir = indexFilePaths.indexDirFile(shardContext.getIndexSequence());
				indexDir.mkdirs();
				logger.debug("Save DataInfo >> {}", dataInfo);
				JAXBConfigs.writeConfig(new File(indexDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);

				SegmentInfo lastSegmentInfo = dataInfo.getLastSegmentInfo();
				File revisionDir = indexFilePaths.revisionFile(shardContext.getIndexSequence(), lastSegmentInfo.getId(),
						lastSegmentInfo.getRevision());
				RevisionInfo revisionInfo = lastSegmentInfo.getRevisionInfo();
				if (revisionInfo != null) {
					logger.debug("Save RevisionInfo >> {}, {}", revisionDir.getAbsolutePath(), revisionInfo);
					JAXBConfigs.writeConfig(new File(revisionDir, SettingFileNames.revisionInfo), revisionInfo, RevisionInfo.class);
				}
			}
		} catch (JAXBException e) {
			throw new SettingException("색인후 CollectionContext 저장중 에러발생", e);
		}
	}

	// 색인이 끝나고 dataInfo 저장.
	// public static void saveDataInfo(CollectionContext collectionContext) throws SettingException {
	// CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
	// DataInfo dataInfo = collectionContext.dataInfo();
	// CollectionStatus collectionStatus = collectionContext.collectionStatus();
	//
	// try {
	// if (dataInfo != null) {
	// logger.debug("Save DataInfo >> {}", dataInfo);
	// File dataDir = collectionFilePaths.dataFile(collectionStatus.getSequence());
	// JAXBConfigs.writeConfig(new File(dataDir, SettingFileNames.dataInfo), dataInfo, DataInfo.class);
	//
	// SegmentInfo lastSegmentInfo = dataInfo.getLastSegmentInfo();
	// File revisionDir = collectionFilePaths.revisionFile(collectionStatus.getSequence(), lastSegmentInfo.getId(),
	// lastSegmentInfo.getRevision());
	// RevisionInfo revisionInfo = lastSegmentInfo.getRevisionInfo();
	// if (revisionInfo != null) {
	// logger.debug("Save RevisionInfo >> {}", revisionInfo);
	// JAXBConfigs.writeConfig(new File(revisionDir, SettingFileNames.revisionInfo), revisionInfo, RevisionInfo.class);
	// }
	// }
	//
	// } catch (JAXBException e) {
	// throw new SettingException("CollectionContext 저장중 에러발생", e);
	// }
	// }

	// 색인끝나고 sequence 및 last 색인건수 저장.
	// public static void saveCollectionStatus(CollectionContext collectionContext) {
	// CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
	// CollectionStatus collectionStatus = collectionContext.collectionStatus();
	// File collectionDir = collectionFilePaths.file();
	//
	// if (collectionStatus != null) {
	// logger.debug("Save CollectionStatus >> {}", collectionStatus);
	// JAXBConfigs.writeConfig(new File(collectionDir, SettingFileNames.collectionStatus), collectionStatus,
	// CollectionStatus.class);
	// }
	// }

	// workschema파일이 존재한다면 workschema를 schema로 대치하고
	// schema파일을 저장하고, workschema파일을 지운다.
	public static void applyWorkSchema(CollectionContext collectionContext) throws SettingException {
		FilePaths collectionFilePaths = collectionContext.indexFilePaths();
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
