/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.datasource.reader;

import java.util.List;
import java.util.Properties;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.CollectFileParser.FileParserConfig;
import org.fastcatsearch.datasource.reader.DBReader.DBReaderConfig;
import org.fastcatsearch.datasource.reader.WebPageSourceReader.WebPageSourceReaderConfig;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.settings.IRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SourceReaderFactory {
	private static Logger logger = LoggerFactory.getLogger(SourceReaderFactory.class);
	
	public static SourceReader createSourceReader(String collection, Schema schema, DataSourceSetting dsSetting, boolean isFull) throws IRException{
	
		SourceModifier sourceModifier = null;
		if(dsSetting.sourceModifier != null && dsSetting.sourceModifier.length() > 0){
			sourceModifier = DynamicClassLoader.loadObject(dsSetting.sourceModifier, SourceModifier.class);
			if(sourceModifier == null){
				throw new IRException ("unable to find source modifier class "+dsSetting.sourceModifier);
			}
		}
		
		if(dsSetting.isMultiSource()){
			List<DataSourceSetting> dsSettingList = IRSettings.getMultiDatasource(collection, true);
			return null;//new MultiSourceReader(schema, dsSettingList, isFull);
		}else{
			if(dsSetting.sourceType.equalsIgnoreCase("FILE")){
				FileParserConfig config = new FileParserConfig();
				config.setFullFilePath(IRSettings.path(dsSetting.fullFilePath));
				config.setIncFilePath(IRSettings.path(dsSetting.incFilePath));
				config.setFileEncoding(dsSetting.fileEncoding);
				config.setFileDocParser(dsSetting.fileDocParser);
				
				SourceReader sourceReader = DynamicClassLoader.loadObject(dsSetting.fileDocParser, SourceReader.class, new Class[]{Schema.class, FileParserConfig.class, SourceModifier.class, Boolean.class}, new Object[]{schema, config, sourceModifier, isFull});
				logger.debug("Loading sourceReader : {}, {}", dsSetting.fileDocParser, sourceReader);
				if(sourceReader == null){
					logger.error("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. sourceType={}", dsSetting.sourceType);
				}else{
					return sourceReader;
				}
			}else if(dsSetting.sourceType.equalsIgnoreCase("DB")){
				DBReaderConfig config = new DBReaderConfig();
				config.setJdbcDriver(dsSetting.driver);
				config.setJdbcUrl(dsSetting.url);
				config.setJdbcUser(dsSetting.user);
				config.setJdbcPassword(dsSetting.password);
				config.setFetchSize(dsSetting.fetchSize);
				config.setBulkSize(dsSetting.bulkSize);
				config.setBeforeIncQuery(dsSetting.beforeIncQuery);
				config.setAfterIncQuery(dsSetting.afterIncQuery);
				config.setBeforeFullQuery(dsSetting.beforeFullQuery);
				config.setAfterFullQuery(dsSetting.afterFullQuery);
				config.setFullQuery(dsSetting.fullQuery);
				config.setIncQuery(dsSetting.incQuery);
				config.setDeleteIdQuery(dsSetting.deleteIdQuery);
				config.setFullBackupPath(IRSettings.path(dsSetting.fullBackupPath));
				config.setIncBackupPath(IRSettings.path(dsSetting.incBackupPath));
				config.setBackupFileEncoding(dsSetting.backupFileEncoding);
				return new DBReader(schema, config, sourceModifier, isFull);
			}else if(dsSetting.sourceType.equalsIgnoreCase("WEB")){
				WebPageSourceReaderConfig config = null;
				return new WebPageSourceReader(schema, config, sourceModifier, isFull);
			}else if(dsSetting.sourceType.equalsIgnoreCase("CUSTOM")){
				SourceReader sourceReader = DynamicClassLoader.loadObject(dsSetting.customReaderClass, SourceReader.class, new Class[]{Schema.class, DataSourceSetting.class, SourceModifier.class, Boolean.class, Properties.class}, new Object[]{schema, dsSetting, sourceModifier, isFull});
				logger.debug("Loading sourceReader : {}, {}", dsSetting.fileDocParser, sourceReader);
				if(sourceReader == null){
					logger.error("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. dsSetting.sourceType={}", dsSetting.sourceType);
				}else{
					return sourceReader;
				}
			}else{
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "수집대상 소스타입을 알수 없습니다.sourceType={}", dsSetting.sourceType);
			}
			return null;
		}
	}
}
