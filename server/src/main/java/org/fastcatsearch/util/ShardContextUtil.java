//package org.fastcatsearch.util;
//
//import java.io.File;
//
//import org.fastcatsearch.env.Path;
//import org.fastcatsearch.ir.common.SettingException;
//import org.fastcatsearch.ir.config.ClusterConfig;
//import org.fastcatsearch.ir.config.CollectionConfig;
//import org.fastcatsearch.ir.config.CollectionContext;
//import org.fastcatsearch.ir.config.CollectionIndexStatus;
//import org.fastcatsearch.ir.config.DataSourceConfig;
//import org.fastcatsearch.ir.config.JAXBConfigs;
//import org.fastcatsearch.ir.config.ShardContext;
//import org.fastcatsearch.ir.config.ShardIndexStatus;
//import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
//import org.fastcatsearch.ir.settings.Schema;
//import org.fastcatsearch.ir.settings.SchemaSetting;
//import org.fastcatsearch.settings.SettingFileNames;
//
//public class ShardContextUtil {
//
//	public static void saveAfterIndexing(ShardContext shardContext) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	public static ShardContext load(IndexFilePaths indexFilePaths, Integer dataSequence) throws SettingException {
//		try {
//			Path collectionDir = new Path(indexFilePaths.file());
//			File schemaFile = collectionDir.file(SettingFileNames.schema);
//			SchemaSetting schemaSetting = JAXBConfigs.readConfig(schemaFile, SchemaSetting.class);
//			File workSchemaFile = collectionDir.file(SettingFileNames.workSchema);
//			SchemaSetting workSchemaSetting = JAXBConfigs.readConfig(workSchemaFile, SchemaSetting.class);
//			CollectionConfig collectionConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.collectionConfig), CollectionConfig.class);
//			ClusterConfig clusterConfig = JAXBConfigs.readConfig(collectionDir.file(SettingFileNames.clusterConfig), ClusterConfig.class);
//			File dataSourceConfigFile = collectionDir.file(SettingFileNames.datasourceConfig);
//			DataSourceConfig dataSourceConfig = null;
//			if (dataSourceConfigFile.exists()) {
//				dataSourceConfig = JAXBConfigs.readConfig(dataSourceConfigFile, DataSourceConfig.class);
//			} else {
//				dataSourceConfig = new DataSourceConfig();
//			}
//
//			File collectionStatusFile = collectionDir.file(SettingFileNames.indexStatus);
//			CollectionIndexStatus collectionStatus = JAXBConfigs.readConfig(collectionStatusFile, CollectionIndexStatus.class);
//
////			if (dataSequence == null) {
////				// dataSequence가 없으므로 indexedSequence로 선택하여 로딩한다.
////				int indexedSequence = collectionStatus.getSequence();
////				dataSequence = indexedSequence;
////			}
//
//			// dataSequence가 null아 아니면 원하는 sequence의 정보를 읽어온다.
//			File dataDir = indexFilePaths.dataFile();
//			if (!dataDir.exists()) {
//				dataDir.mkdirs();
//			}
////			File infoFile = new File(dataDir, SettingFileNames.dataInfo);
////			DataInfo dataInfo = null;
////			if (infoFile.exists()) {
////				dataInfo = JAXBConfigs.readConfig(infoFile, DataInfo.class);
////			} else {
////				logger.info("File not found : {}", infoFile);
////				dataInfo = new DataInfo();
////				JAXBConfigs.writeConfig(infoFile, dataInfo, DataInfo.class);
////			}
////
////			logger.debug("dataInfo.getSegmentInfoList() >> {}", dataInfo.getSegmentInfoList().size());
//			if (!collectionStatus.isEmpty()) {
//				// SegmentInfoList가 없다면 data디렉토리를 지웠거나 색인이 안된상태이므로, 확인차 status초기화해준다.
//				collectionStatus.clear();
//				JAXBConfigs.writeConfig(collectionStatusFile, collectionStatus, ShardIndexStatus.class);
//			}
//
//			Schema schema = new Schema(schemaSetting);
//			Schema workSchema = null;
//			if (workSchemaSetting != null) {
//				workSchema = new Schema(workSchemaSetting);
//			}
//			ShardContext shardContext = new ShardContext(indexFilePaths.getId(), indexFilePaths);
//			shardContext.init(schema, collectionConfig, clusterConfig, dataSourceConfig, collectionStatus);
//			return collectionContext;
//		} catch (Exception e) {
//			throw new SettingException("CollectionContext 로드중 에러발생", e);
//		}
//	}
//
//}
