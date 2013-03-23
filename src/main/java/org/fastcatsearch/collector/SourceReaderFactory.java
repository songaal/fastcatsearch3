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

package org.fastcatsearch.collector;

import java.util.List;
import java.util.Properties;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.source.SourceReader;
import org.fastcatsearch.log.EventDBLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SourceReaderFactory {
	private static Logger logger = LoggerFactory.getLogger(SourceReaderFactory.class);
	
	public static SourceReader createSourceReader(String collection, Schema schema, DataSourceSetting dsSetting, boolean isFull) throws IRException{
		if(dsSetting.isMultiSource()){
			List<DataSourceSetting> dsSettingList = IRSettings.getMultiDatasource(collection, true);
			return new MultiSourceReader(schema, dsSettingList, isFull);
		}else{
			if(dsSetting.sourceType.equalsIgnoreCase("FILE")){
				SourceReader sourceReader = (SourceReader) DynamicClassLoader.getInstance().loadObject(dsSetting.fileDocParser, new Class[]{Schema.class, DataSourceSetting.class, Boolean.class}, new Object[]{schema, dsSetting, isFull});
				logger.debug("Loading sourceReader : {}, {}", dsSetting.fileDocParser, sourceReader);
				if(sourceReader == null){
					logger.error("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. sourceType={}", dsSetting.sourceType);
				}else{
					return sourceReader;
				}
			}else if(dsSetting.sourceType.equalsIgnoreCase("DB")){
				return new DBReader(schema, dsSetting, isFull);
			}else if(dsSetting.sourceType.equalsIgnoreCase("WEB")){
				return new WebPageSourceReader(schema, dsSetting, isFull);
			}else if(dsSetting.sourceType.equalsIgnoreCase("CUSTOM")){
				SourceReader sourceReader = (SourceReader) DynamicClassLoader.getInstance().loadObject(dsSetting.customReaderClass, new Class[]{Schema.class, DataSourceSetting.class, Boolean.class, Properties.class}, new Object[]{schema, dsSetting, isFull});
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
