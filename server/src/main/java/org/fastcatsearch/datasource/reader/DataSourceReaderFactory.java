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

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataSourceReaderFactory {
	private static Logger logger = LoggerFactory.getLogger(DataSourceReaderFactory.class);
	
	
	//멀티소스일때 리더를 결합한 compositeSourceReader를 리턴해준다.
	public static DataSourceReader createSourceReader(Path filePath, Schema schema, List<DataSourceConfig> dataSourceConfigList, String lastIndexTime, boolean isFullIndexing) throws IRException{
		
		List<DataSourceReader> readerList = new ArrayList<DataSourceReader>(dataSourceConfigList.size());
		
		for(DataSourceConfig dataSourceConfig : dataSourceConfigList){
			readerList.add(createSourceReader(filePath, schema, dataSourceConfig, lastIndexTime, isFullIndexing));
		}
		return new CompositeDataSourceReader(readerList);
		
	}
	public static DataSourceReader createSourceReader(Path filePath, Schema schema, DataSourceConfig dataSourceConfig, String lastIndexTime, boolean isFullIndexing) throws IRException{
	
		SourceModifier sourceModifier = null;
		if(dataSourceConfig.getSourceModifier() != null && dataSourceConfig.getSourceModifier().length() > 0){
			sourceModifier = DynamicClassLoader.loadObject(dataSourceConfig.getSourceModifier(), SourceModifier.class);
			if(sourceModifier == null){
				throw new IRException ("unable to find source modifier class "+dataSourceConfig.getSourceModifier());
			}
		}
		
		String readerType = dataSourceConfig.getReaderType();
		DataSourceReader sourceReader = DynamicClassLoader.loadObject(readerType, DataSourceReader.class, new Class[]{Path.class, Schema.class, DataSourceConfig.class, SourceModifier.class, String.class, Boolean.class}, new Object[]{filePath, schema, dataSourceConfig, sourceModifier, lastIndexTime, isFullIndexing});
		logger.debug("Loading sourceReader : {} >> {}", readerType, sourceReader);
		if(sourceReader == null){
			throw new IRException ("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. readerType=" + readerType);
		}else{
			return sourceReader;
		}
	}
}
