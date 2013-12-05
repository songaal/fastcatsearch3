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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 데이터소스 리더.
 * 
 * TODO 고려사항 : DataSourceReader를 일반 class로 만들고 internalDataSourceReader를 받아서 처리하게 한다. internalDataSourceReader는 field별 데이터만 셋팅하게
 * 하고 DataSourceReader가 modify수행후 document를 만들어서 최종리턴을 한다. 내부 internalDataSourceReader 가 여러개이면 멀티소스리더같이 동작하게 된다.
 * */
public class DataSourceReader {

	protected static Logger logger = LoggerFactory.getLogger(DataSourceReader.class);

	protected List<FieldSetting> fieldSettingList;

	protected SourceModifier sourceModifier;

	private DeleteIdSet deleteIdList;

	private List<SingleSourceReader> singleSourceReaderList;
	private int readerPos;
	private SingleSourceReader currentReader;

	// DataSourceConfig안에는 SingleSourceConfig가 여러개 들어있다.
	public DataSourceReader(SchemaSetting schemaSetting) throws IRException {
		fieldSettingList = schemaSetting.getFieldSettingList();

		PrimaryKeySetting primaryKeySetting = schemaSetting.getPrimaryKeySetting();
		if (primaryKeySetting != null && primaryKeySetting.getFieldList().size() > 0) {
			int pkFieldSize = primaryKeySetting.getFieldList().size();
			deleteIdList = new DeleteIdSet(pkFieldSize);
		}

		singleSourceReaderList = new ArrayList<SingleSourceReader>();
	}

	private void nextReader() {
		if (readerPos < singleSourceReaderList.size()) {
			currentReader = singleSourceReaderList.get(readerPos++);
		} else {
			currentReader = null;
		}
	}

	public final DeleteIdSet getDeleteList() {
		return deleteIdList;
	}

	public void addSourceReader(SingleSourceReader sourceReader) {
		sourceReader.setDeleteIdList(deleteIdList);
		singleSourceReaderList.add(sourceReader);
	}

	public void init(){
		nextReader();
	}
	public boolean hasNext() throws IRException {
		try {
			while (true) {
				if (currentReader == null) {
					// 다음번 reader가 null이면 모두 읽은 것임.
					return false;
				}

				if (currentReader.hasNext()) {
					return true;
				} else {
					currentReader.close();
					// 이번리더를 다 읽었으면 다음 리더로 이동.
					nextReader();
				}
			}
		} catch (Throwable e) {
			close();
			throw new IRException(e);
		}
	}

	public Document nextDocument() throws IRException {
		try {
			Map<String, Object> map = currentReader.nextElement();
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

	public void close() {
		for (SingleSourceReader reader : singleSourceReaderList) {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IRException e) {
			}
		}
	}

}
