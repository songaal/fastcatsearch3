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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SourceReader {
	
	protected static Logger logger = LoggerFactory.getLogger(SourceReader.class);
	
	protected List<FieldSetting> fieldSettingList;
	protected AsciiCharTrie fieldIndex;
	protected int idFieldIndex;
	protected Set<String> deleteIdList = new HashSet<String>();
	protected SourceModifier sourceModifier;
	
	public abstract boolean hasNext() throws IRException;
	public abstract Document next() throws IRException;
	public abstract void close() throws IRException;
	
	public SourceReader(){}
	
	public SourceReader(Schema schema, SourceModifier sourceModifier) throws IRException{
		fieldSettingList = schema.getFieldSettingList();
		fieldIndex = schema.fieldnames;
		idFieldIndex = schema.getIndexID();
		if(idFieldIndex == -1){
			throw new IRException("Schema has no primary key!");
		}
		
		this.sourceModifier = sourceModifier;
	}
	
	public final Set<String> getDeleteList(){
		return deleteIdList;
	}
	
	@XmlRootElement(name = "source")
	public static class SourceConfig {
		private String readerType;
		private String configType;
		private String sourceModifier;
		
		@XmlAttribute(required=true)
		public String getReaderType(){
			return readerType;
		}
		
		@XmlAttribute(required=true)
		public String getConfigType(){
			return configType;
		}
		
		@XmlElement
		public String getSourceModifier(){
			return sourceModifier;
		}
		
		public void setReaderType(String readerType){
			this.readerType = readerType;
		}
		
		public void setConfigType(String configType){
			this.configType = configType;
		}
		
		public void setSourceModifier(String sourceModifier){
			this.sourceModifier = sourceModifier;
		}
	}
	
}
