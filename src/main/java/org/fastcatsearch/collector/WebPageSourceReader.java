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

package org.fastcatsearch.collector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.ir.source.SourceReader;
import org.fastcatsearch.util.ReadabilityExtractor;
import org.fastcatsearch.util.WebPageGather;

public class WebPageSourceReader extends SourceReader{
	
	private DirBufferedReader br;
	private Document document;
	private Pattern p;
	private int count; // how many fields are set
	private int lineNum;
	private int id;
	private WebPageGather webPageGather;
	private Statement stmt;
	private ResultSet rs;
	private static ReadabilityExtractor extractor = new ReadabilityExtractor();

	public WebPageSourceReader(Schema schema, DataSourceSetting setting, Boolean isFull) throws IRException {
		super(schema, setting);
		try {
			if(isFull){
				//br = new DirBufferedReader(new File(IRSettings.path(setting.fullFilePath)), setting.fileEncoding);
				br = new DirBufferedReader(new File(IRSettings.getCollectionHome(schema.collection) + "url.ful"), setting.fileEncoding);
			}else{
				//br = new DirBufferedReader(new File(IRSettings.path(setting.incFilePath)), setting.fileEncoding);
				br = new DirBufferedReader(new File(IRSettings.getCollectionHome(schema.collection) + "url.inc"), setting.fileEncoding);
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
		
		p = Pattern.compile("<title>(.*)</title>",Pattern.CASE_INSENSITIVE);
		
		deleteIdList = new HashSet<String>();
		id = 0;
		webPageGather = new WebPageGather();
		
		DBService dbHandler = DBService.getInstance();
		Connection conn = dbHandler.getConn();
		String countSQL = "SELECT * FROM "+schema.collection+"WebPageSource";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(countSQL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNext() throws IRException{
		int size = fieldSettingList.size();
		document = new Document(size);
		
		String oneDoc = readOneDoc();
		if(oneDoc == null)
			return false;
		
		String[] tmps = oneDoc.split("\\t");
		count = 0;
		if (tmps.length >= 1) {
			String source = webPageGather.getLinkPageContent(tmps[0], tmps.length>2?tmps[2]:"utf-8", "get");
			//id
			document.set(count, fieldSettingList.get(count).createField(""+(id++)));
			count++;
			//title
			if (tmps.length == 1) {
				Matcher m = p.matcher(source);
				String title = "";
				if (m.find()) {
					title = m.group(1);
				}else{
					if (source.length() > 10) {
						title = source.substring(0,10);
					}else{
						title = source;
					}
				}
				document.set(count, fieldSettingList.get(count).createField(title));
				count++;
			} else {
				if (tmps[1] == null) {
					Matcher m = p.matcher(source);
					String title = "";
					if (m.find()) {
						title = m.group(1);
					}else{
						if (source.length() > 10) {
							title = source.substring(0,10);
						}else{
							title = source;
						}
					}
					document.set(count, fieldSettingList.get(count).createField(title));
					count++;
				} else {
					document.set(count, fieldSettingList.get(count).createField(tmps[1]));
					count++;
				}
			}
			
			//content
			document.set(count, fieldSettingList.get(count).createField(extractor.extract(source)));
			count++;
			//url
			document.set(count, fieldSettingList.get(count).createField(tmps[0]));
			count++;
		} else {
			logger.error("There is error in url list file at line "+lineNum);
			return false;
		}
		
//		logger.debug("doc = "+document);
		if(document == null)
			return false;
		
		return true;
		
	}
	
	
	private String readOneDoc() throws IRException {
		try{
			//String line = br.readLine();
			String line = null;
			if (rs.next()) {
				line= rs.getString("link") + "	" + rs.getString("title")+ "	" + rs.getString("encoding");
			}
			lineNum++;
			
			if(line == null)
				return null;
			
			while ("".equals(line.trim()) || line.contains("\\[") || !line.contains("http")) {
				//line = br.readLine();
				if (rs.next()) {
					line= rs.getString("link") + "	" + rs.getString("title")+ "	" + rs.getString("encoding");
				}
				lineNum++;
				if(line == null)
					return null;
			}
			
			return line;
			
		}catch (SQLException e) {
			throw new IRException(e);
		}
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
			rs.close();
			stmt.close();
		} catch (IOException e) {
			throw new IRException(e);
		} catch (SQLException e) {
			throw new IRException(e);
		}
	}
	
}
