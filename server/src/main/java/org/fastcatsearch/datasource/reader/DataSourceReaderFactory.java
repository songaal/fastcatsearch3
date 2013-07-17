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

import java.io.File;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DBSourceConfig;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataSourceReaderFactory {
	private static Logger logger = LoggerFactory.getLogger(DataSourceReaderFactory.class);
	
	public static DataSourceReader createSourceReader(File filePath, Schema schema, DataSourceConfig dataSourceConfig, String lastIndexTime, boolean isFullIndexing) throws IRException{
	
		DataSourceReader dataSourceReader = new DataSourceReader(filePath, schema);
		
		//TODO dataSourceReader가 null일수 있다.
		
		for(DBSourceConfig dbSourceConfig : dataSourceConfig.getDBSourceConfigList()){
			String sourceReaderType = dbSourceConfig.getSourceReader();
			String sourceModifierType = dbSourceConfig.getSourceModifier();
			SourceModifier sourceModifier = null;
			if(sourceModifierType != null && sourceModifierType.length() > 0){
				sourceModifier = DynamicClassLoader.loadObject(sourceModifierType, SourceModifier.class);
			}
			
			SingleSourceReader sourceReader = DynamicClassLoader.loadObject(sourceReaderType, SingleSourceReader.class
					, new Class[]{File.class, Schema.class, DBSourceConfig.class, SourceModifier.class, String.class, Boolean.class}
					, new Object[]{filePath, schema, dbSourceConfig, sourceModifier, lastIndexTime, isFullIndexing});
			logger.debug("Loading sourceReader : {} >> {}, modifier:{}", sourceReaderType, sourceReader, sourceModifierType);
			if(sourceReader == null){
				throw new IRException ("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. reader=" + sourceReaderType);
			}else{
				dataSourceReader.addSourceReader(sourceReader);
			}
		}
		return dataSourceReader;
	}
}
