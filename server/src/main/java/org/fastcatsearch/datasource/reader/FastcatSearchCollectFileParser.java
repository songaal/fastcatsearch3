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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.env.Path;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.config.FileSourceConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.HTMLTagRemover;


public class FastcatSearchCollectFileParser extends SingleSourceReader {
	
	private DirBufferedReader br;
	private Map<String, Object> dataMap;
	
	
	private static String DOC_START ="<doc>";
	private static String DOC_END ="</doc>";
	
	
	private static String OPEN_PATTERN = "^<([\\w]+[^>]*)>$";
	private static String CLOSE_PATTERN = "^<\\/([\\w]+[^>]*)>$";
	private Pattern OPAT;
	private Pattern CPAT;

	public FastcatSearchCollectFileParser(File filePath, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime, boolean isFull) throws IRException {
		super(filePath, singleSourceConfig, sourceModifier, lastIndexTime, isFull);
	}
	
	@Override
	public void init() throws IRException {
		FileSourceConfig config = (FileSourceConfig) singleSourceConfig;
		String fileEncoding = config.getFileEncoding();
		if(fileEncoding == null){
			fileEncoding = Charset.defaultCharset().toString();
		}
		try {
			File file = null;
			if(isFull){
				file = new Path(filePath).makePath(config.getFullFilePath()).file();
				br = new DirBufferedReader(file, fileEncoding);
			}else{
				file = new Path(filePath).makePath(config.getIncFilePath()).file();
				br = new DirBufferedReader(file, fileEncoding);
			}
			logger.info("Collect file = {}, {}", file.getAbsolutePath(), fileEncoding);
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
		dataMap = null;
		
		OPAT = Pattern.compile(OPEN_PATTERN);
		CPAT = Pattern.compile(CLOSE_PATTERN);
	}
	
	@Override
	public boolean hasNext() throws IRException{
		String line = null;
		dataMap = new HashMap<String, Object>();
		
		String oneDoc = readOneDoc();
		if(oneDoc == null){
			return false;
		}
		
		logger.debug("ONE DOC = {}", oneDoc);
		
		BufferedReader reader = new BufferedReader(new StringReader(oneDoc));
		
		StringBuffer sb = new StringBuffer();
		
		String openTag = "";
		boolean isOpened = false;
		
		while(true){
			try {
				line = reader.readLine();
				
				if(line == null){
					
					break;
				}
				
				if(line.length() == 0){
					continue;
				}
				
				line = line.trim();
				
				
				if(line.length() > 1 && line.charAt(0) == '<' && line.charAt(1) != '/'){
					Matcher m = OPAT.matcher(line);
					if(m.matches()){
						String tag = m.group(1);
						openTag = tag;
						isOpened = true;
						if(logger.isTraceEnabled()){
							logger.trace("OpenTag [{}]", tag);
						}
						continue;
					}
				}
				
				if(isOpened && line.startsWith("</")){
					Matcher m = CPAT.matcher(line);
					if(m.matches()){
						String closeTag = m.group(1);
						if(openTag.equals(closeTag)){
							isOpened = false;
							String targetStr = sb.toString();
							if(logger.isTraceEnabled()){
								logger.trace("CloseTag [{}]", closeTag);
								logger.trace("Data [{}]", targetStr);
							}
							dataMap.put(openTag, targetStr);
							sb = new StringBuffer();
							continue;
						}
					}
				}
				
				
				if(sb.length() > 0){
					sb.append(Environment.LINE_SEPARATOR);
				}
				
				sb.append(line);
			
//				logger.debug(sb.toString());
			
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				throw new IRException(e);
			}
		}
//		logger.debug("doc = "+document);
		if(dataMap == null)
			return false;
		
		return true;
		
	}
	
	
	private String readOneDoc() throws IRException {
		try{
			StringBuffer sb = new StringBuffer();
			
			String line = br.readLine();
			
			if(line == null)
				return null;
			
			int lineNumber = 0;
			
			while(!line.equals(DOC_START)){
				//doc opened
				line = br.readLine();
				if(line == null)
					return null;
			}
			
			line = br.readLine();
			
			//doc started
			while(!line.equals(DOC_END)){
				//doc ended
				if(lineNumber >= 1){
					sb.append(Environment.LINE_SEPARATOR);
				}
				sb.append(line);
				line = br.readLine();
				lineNumber++;
			}
			return sb.toString();
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	@Override
	public Map<String, Object> next() throws IRException{
		return dataMap;
	}
	
	@Override
	public void close() throws IRException{
		try {
			if(br != null){
				br.close();
			}
		} catch (IOException e) {
			throw new IRException(e);
		}
	}

	
	
}
