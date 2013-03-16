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

package org.fastcatsearch.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.ir.source.SourceReader;


public class SingleLineCollectFileParser extends SourceReader{
	
	private DirBufferedReader br;
	private Document document;
	
	private int count; // how many fields are set

	public SingleLineCollectFileParser(Schema schema, DataSourceSetting setting, Boolean isFull) throws IRException {
		super(schema, setting);
		try {
			if(isFull){
				br = new DirBufferedReader(new File(IRSettings.path(setting.fullFilePath)), setting.fileEncoding);
			}else{
				br = new DirBufferedReader(new File(IRSettings.path(setting.incFilePath)), setting.fileEncoding);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
			throw new IRException(e);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(),e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new IRException(e);
		}
		document = null;
		
		deleteIdList = new HashSet<String>();
		
	}
//	int limit = 0;
	public boolean hasNext() throws IRException{
//		if(limit++ >= 1200000)
//			return false;
		
		int size = fieldSettingList.size();
		String key = null;
		String value = null;
		String line = null;
		document = new Document(size);
		count = 0;
		while(count < size) {
			try {
				FieldSetting fs = fieldSettingList.get(count);
				line = br.readLine();
//				logger.debug("line = "+line);
				if(line == null)
					return false;
				//if(line.startsWith(DBReader.BACKUP_HEADER) || line.startsWith("insert_fastcat_id_")){
				if(line.startsWith("insert_fastcat_id_")){
					continue;
				}
				if(line.length() == 0){
					continue;
				}
				int p = line.indexOf(":");
				if(p > 0){
					key = line.substring(0, p);
					value = line.substring(p+1);
				}
				
				if(key.equalsIgnoreCase(fs.name)){
					document.set(count, fs.createField(value));
					count++;
				}else{
					logger.warn("skip! "+key+"("+fs.name+")count="+key.equalsIgnoreCase(fs.name)+", fs.name="+fs.name+", value="+value);
				}
				
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
			
		}

		return true;
	}
	
	
	public Document next() throws IRException{
		if(count != document.size()){
//			throw new IRException("Collect document's field count is diffent from setting's. Check field names. it's case sensitive. collect field size = "+count+", document.size()="+document.size());
			logger.warn("Collect document's field count is diffent from setting's. Check field names. it's case sensitive. collect field size = "+count+", document.size()="+document.size());
			if(hasNext() == false)
				return null;
		}
		count = 0;
		return document;
	}
	
	public void close() throws IRException{
		try {
			br.close();
		} catch (IOException e) {
			throw new IRException(e);
		}
	}
	
}
