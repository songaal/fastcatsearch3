///*
// * Copyright 2013 Websquared, Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.config;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.Properties;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DataSourceSetting {
//	
//	private static Logger logger = LoggerFactory.getLogger(DataSourceSetting.class);
//			
//	private Properties props;
//	private Properties customProps;
//	
//	public String sourceType;
//	public String sourceFrom;//MULTI경우 여러 데이터 소스를 사용한다. datasource.conf.# 과 같은 이름의 파일을 순차적으로 읽어온다. [MULTI | SINGLE] 
//	public String sourceModifier;
//	public int testSize;
//	
//	//file 
//	public String fullFilePath;
//	public String incFilePath;
//	public String fileEncoding;
//	public String fileDocParser;
//	
//	//db
//	public String driver;
//	public String url;
//	public String user;
//	public String password;
//	public int fetchSize;
//	public int bulkSize;
//	public String beforeIncQuery;
//	public String afterIncQuery;
//	public String beforeFullQuery;
//	public String afterFullQuery;
//	public String fullQuery;
//	public String incQuery;
//	public String deleteIdQuery;
//	public String fullBackupPath;
//	public String incBackupPath;
//	public String backupFileEncoding;
//	
//	//custom
//	public String customReaderClass;
//	public String customConfigFile;
//
//	public DataSourceSetting(){ }
//	
//	public DataSourceSetting(Properties props){
//		this.props = props;
//		sourceType = props.getProperty("source.type");
//		sourceFrom = props.getProperty("source.from");
//		sourceModifier = props.getProperty("source.modifier");
//		String testSizeStr = props.getProperty("source.testSize");
//		if(testSizeStr != null && !testSizeStr.equals("")){
//			testSize = Integer.parseInt(testSizeStr);
//		}
//		
//		fullFilePath = props.getProperty("full.source.path");
//		incFilePath = props.getProperty("inc.source.path");
//		fileEncoding = props.getProperty("file.encoding");
//		fileDocParser = props.getProperty("file.document.parser");
//		
//		if(sourceType != null && sourceType.equals("DB")){
//			driver = props.getProperty("driver");
//			url = props.getProperty("url");
//			user = props.getProperty("user");
//			password = props.getProperty("password");
//			fetchSize = Integer.parseInt(props.getProperty("fetchsize"));
//			bulkSize = Integer.parseInt(props.getProperty("bulksize"));
//			beforeIncQuery = props.getProperty("before.inc.query");
//			afterIncQuery = props.getProperty("after.inc.query");
//			beforeFullQuery = props.getProperty("before.full.query");
//			afterFullQuery = props.getProperty("after.full.query");
//			fullQuery = props.getProperty("full.query");
//			incQuery = props.getProperty("inc.query");
//			deleteIdQuery = props.getProperty("update.id.query");
//			fullBackupPath = props.getProperty("full.backup.path");
//			incBackupPath = props.getProperty("inc.backup.path");
//			backupFileEncoding = props.getProperty("backup.file.encoding");
//		}
//		if(sourceType != null && sourceType.equals("CUSTOM")){
//			customReaderClass = props.getProperty("custom.reader.class");
//			customConfigFile = props.getProperty("custom.conf.file");
//		}
//		if(customConfigFile != null){
//			File path = new File(IRSettings.path(customConfigFile));
//			if(path.isFile()){
//				try {
//					customProps = new Properties();
//					customProps.loadFromXML(new FileInputStream(path));
//				} catch (FileNotFoundException e) {
//					logger.error(e.getMessage(),e);
//				} catch (IOException e) {
//					logger.error(e.getMessage(),e);
//				}
//			}
//		}
//		
//	}
//	
//	public Properties getProperties(){
//		return props;
//	}
//	public Properties getCustomProperties(){
//		return customProps;
//	}
//	public static void init(Properties props){
//		props.setProperty("source.type", "DB");
//		props.setProperty("source.from", "SINGLE");
//		props.setProperty("source.modifier", "");
//		props.setProperty("source.testSize", "");
//		
//		props.setProperty("full.source.path", "");
//		props.setProperty("inc.source.path", "");
//		props.setProperty("file.encoding", "");
//		props.setProperty("file.document.parser", "");
//		
//		props.setProperty("driver", "");
//		props.setProperty("url", "");
//		props.setProperty("user", "");
//		props.setProperty("password", "");
//		props.setProperty("fetchsize", "1000");
//		props.setProperty("bulksize", "100");
//		
//		props.setProperty("before.full.query", "");
//		props.setProperty("after.full.query", "");
//		props.setProperty("before.inc.query", "");
//		props.setProperty("after.inc.query", "");
//		props.setProperty("full.query", "");
//		props.setProperty("inc.query", "");
//		props.setProperty("update.id.query", "");
//		props.setProperty("full.backup.path", "");
//		props.setProperty("inc.backup.path", "");
//		props.setProperty("backup.file.encoding", "");
//		
//		props.setProperty("custom.reader.class", "");
//		props.setProperty("custom.conf.file", "");
//	}
//	
//	public boolean isMultiSource(){
//		return "multi".equalsIgnoreCase(sourceFrom);
//	}
//}
