/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.datasource.reader;

import java.util.Map;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.util.HTMLTagRemover;

/**
 * 데이터소스 리더.
 * 
 * */
public class DefaultDataSourceReader extends AbstractDataSourceReader<Map<String, Object>> {

	// DataSourceConfig안에는 SingleSourceConfig가 여러개 들어있다.
	public DefaultDataSourceReader(SchemaSetting schemaSetting) throws IRException {
		super(schemaSetting);
	}

	public Document createDocument(Map<String, Object> map) throws IRException {
		try {
//			logger.debug("doc >> {}", map);
			// Schema를 기반으로 Document로 만든다.
			Document document = new Document(fieldSettingList.size());
			for (int i = 0; i < fieldSettingList.size(); i++) {
				FieldSetting fs = fieldSettingList.get(i);
				
				String key = fs.getId();
				String source = fs.getSource();
				if(source != null && source.length() > 0){
					//source가 있다면 source에서 데이터를 가져온다.
					key = source;
				}
				Object data = map.get(key);
//				logger.debug("Get {} : {}", key, data);
				String multiValueDelimiter = fs.getMultiValueDelimiter();
				
				/*
				 * HTML Tag remover
				 */
				boolean isRemoveTag = fs.isRemoveTag();
				if(isRemoveTag && data != null){
					if(!(data instanceof String)){
						data = HTMLTagRemover.clean(data.toString());
					}
				}
				Field f = fs.createIndexableField(data, multiValueDelimiter);
				document.set(i, f);
//				logger.debug("doc [{}]{}:{}", i, fs.getId(), f);
			}
			return document;
		} catch (Throwable e) {
			close();
			throw new IRException(e.getMessage());
		}
	}

}
