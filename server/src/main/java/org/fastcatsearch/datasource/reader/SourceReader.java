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
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SourceReader {
	
	protected static Logger logger = LoggerFactory.getLogger(SourceReader.class);
	
	protected Path filePath;
	protected List<FieldSetting> fieldSettingList;
	protected int idFieldIndex;
	protected DeleteIdSet deleteIdList;
	protected SourceModifier sourceModifier;
	protected Schema schema;
	protected int primaryKeySize;
	
	public abstract boolean hasNext() throws IRException;
	public abstract Document next() throws IRException;
	public abstract void close() throws IRException;
	
	public SourceReader(){}
	
	public SourceReader(Path filePath, Schema schema, SourceModifier sourceModifier) throws IRException{
		this.filePath = filePath;
		this.schema = schema;
		fieldSettingList = schema.schemaSetting().getFieldSettingList();
		
//		idFieldIndex = schemaSetting.getIndexID();
//		if(idFieldIndex == -1){
//			throw new IRException("Schema has no primary key!");
//		}
		PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		this.sourceModifier = sourceModifier;
		
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
