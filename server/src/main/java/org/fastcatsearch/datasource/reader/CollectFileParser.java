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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.FileSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;


public class CollectFileParser extends SourceReader{
	
	private DirBufferedReader br;
	private Document document;
	
	private static String END ="<<EOD>>";
	private static String DELETE ="﻿<<DELETE>>";
	private int count; // how many fields are set

	public CollectFileParser(Path filePath, Schema schema, FileSourceConfig config, SourceModifier sourceModifier, Boolean isFull) throws IRException {
		super(filePath, schema, sourceModifier);
		try {
			if(isFull){
				br = new DirBufferedReader(new File(config.getFullFilePath()), config.getFileEncoding());
			}else{
				br = new DirBufferedReader(new File(config.getIncFilePath()), config.getFileEncoding());
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
		
		deleteIdList = new DeleteIdSet(primaryKeySize);
		
	}
	
	public boolean hasNext() throws IRException{
		int size = fieldSettingList.size();
		//no schema setting 
		if(size == 0)
			return false;
		
		String key = null;
		String value = null;
		String line = null;
		document = new Document(size);
		count = 0;
		while(count < size) {
			try {
				FieldSetting fs = fieldSettingList.get(count);
				line = br.readLine();
				if(line == null)
					return false;
				if(line.indexOf("<<DELETE>>") == 0){
					String id = line.substring(10);
					deleteIdList.add(id);
					continue;
				}
				int p = line.indexOf(":");
				if(p > 0){
					key = line.substring(0, p);
					value = line.substring(p+1);
				}
				if(key.equalsIgnoreCase(fs.getId())){
					if(key.equals("body")){
						StringBuffer sb = new StringBuffer();
						sb.append(value);
						while(!END.equals(line = br.readLine())){
							sb.append('\n');
							sb.append(line);
						}
						document.set(count, fs.createField(sb.toString()));
					}else{
						document.set(count, fs.createField(value));
					}
					count++;
				}else{
					logger.warn("skip! :"+key+":, value="+value+", fs.id=:"+fs.getId()+":");
//					throw new IRException("collect file format error! field = "+key+", expected = "+fs.name+", value = "+value);
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
