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
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceReaderFactory {
	private static Logger logger = LoggerFactory.getLogger(DataSourceReaderFactory.class);

	public static DataSourceReader createSourceReader(File filePath, Schema schema, DataSourceConfig dataSourceConfig, String lastIndexTime,
			boolean isFullIndexing) throws IRException {

		DataSourceReader dataSourceReader = new DataSourceReader(filePath, schema);
		logger.debug("dataSourceConfig > {}", dataSourceConfig);
		if (dataSourceConfig != null) {
			for (SingleSourceConfig singleSourceConfig : dataSourceConfig.getSourceConfigList()) {
				if(!singleSourceConfig.isActive()){
					continue;
				}
				SingleSourceReader sourceReader = createSingleSourceReader(filePath, singleSourceConfig, lastIndexTime, isFullIndexing);
				sourceReader.init();
				dataSourceReader.addSourceReader(sourceReader);
			}
		} else {
			logger.error("설정된 datasource가 없습니다.");
		}
		dataSourceReader.init();
		return dataSourceReader;
	}

	private static SingleSourceReader createSingleSourceReader(File filePath, SingleSourceConfig singleSourceConfig, String lastIndexTime,
			boolean isFullIndexing) throws IRException {
		String sourceReaderType = singleSourceConfig.getSourceReader();
		String sourceModifierType = singleSourceConfig.getSourceModifier();
		SourceModifier sourceModifier = null;
		if (sourceModifierType != null && sourceModifierType.length() > 0) {
			sourceModifier = DynamicClassLoader.loadObject(sourceModifierType, SourceModifier.class);
		}

		SingleSourceReader sourceReader = DynamicClassLoader.loadObject(sourceReaderType, SingleSourceReader.class, new Class[] { File.class,
				SingleSourceConfig.class, SourceModifier.class, String.class, boolean.class }, new Object[] { filePath,
				singleSourceConfig, sourceModifier, lastIndexTime, isFullIndexing });
		logger.debug("Loading sourceReader : {} >> {}, modifier:{}", sourceReaderType, sourceReader, sourceModifier);
		// dataSourceReader가 null일 수 있다.
		if (sourceReader == null) {
			throw new IRException("소스리더를 로드하지 못했습니다. 해당 클래스가 클래스패스에 없거나 생성자 시그너처가 일치하는지 확인이 필요합니다. reader=" + sourceReaderType);
		} else {
			return sourceReader;
		}

	}
}
