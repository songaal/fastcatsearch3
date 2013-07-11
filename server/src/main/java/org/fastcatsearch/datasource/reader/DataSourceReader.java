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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 데이터소스 리더.
 * 
 * TODO 고려사항 : DataSourceReader를 일반 class로 만들고 internalDataSourceReader를 받아서 처리하게 한다.
 * internalDataSourceReader는 field별 데이터만 셋팅하게 하고 
 * DataSourceReader가 modify수행후 document를 만들어서 최종리턴을 한다.
 * 내부 internalDataSourceReader 가 여러개이면 멀티소스리더같이 동작하게 된다.  
 * */
public abstract class DataSourceReader {
	
	protected static Logger logger = LoggerFactory.getLogger(DataSourceReader.class);
	
	protected Path filePath;
	protected List<FieldSetting> fieldSettingList;
	protected int idFieldIndex;
	protected DeleteIdSet deleteIdList;
	protected SourceModifier sourceModifier;
	protected Schema schema;
	protected int primaryKeySize;
	protected DataSourceConfig dataSourceConfig;
	
	public abstract boolean hasNext() throws IRException;
	public abstract Document next() throws IRException;
	public abstract void close() throws IRException;
	
	public DataSourceReader(){}
	
	public DataSourceReader(Path filePath, Schema schema, DataSourceConfig dataSourceConfig, SourceModifier sourceModifier) throws IRException{
		this.filePath = filePath;
		this.schema = schema;
		this.dataSourceConfig = dataSourceConfig;
		fieldSettingList = schema.schemaSetting().getFieldSettingList();
//		idFieldIndex = schemaSetting.getIndexID();
//		if(idFieldIndex == -1){
//			throw new IRException("Schema has no primary key!");
//		}
		this.sourceModifier = sourceModifier;
		
		PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		if(primaryKeySetting != null && primaryKeySetting.getFieldList().size() > 0){
			int pkFieldSize = primaryKeySetting.getFieldList().size();
			deleteIdList = new DeleteIdSet(pkFieldSize);
		}
		
		primaryKeySize = schema.schemaSetting().getPrimaryKeySetting().getFieldList().size();
	}
	
	public final DeleteIdSet getDeleteList(){
		return deleteIdList;
	}
	
	
}
