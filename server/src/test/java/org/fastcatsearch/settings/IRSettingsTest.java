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

package org.fastcatsearch.settings;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.GroupSetting;
import org.fastcatsearch.ir.config.IndexSetting;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.config.SortSetting;

public class IRSettingsTest extends TestCase{
	public void setUp(){
		IRSettings.setHome(".");
	}
	
	public void testSchema() throws SettingException{
		Schema schema = IRSettings.getSchema("test1",false);
		
		List<FieldSetting> fieldSettingList = schema.getFieldSettingList();
		List<IndexSetting> indexSettingList = schema.getIndexSettingList();
		List<SortSetting> sortSettingList = schema.getSortSettingList();
		List<GroupSetting> groupSettingList = schema.getGroupSettingList();
		
		
		System.out.println("==== field ====");
		for(int i=0;i<fieldSettingList.size();i++)
			System.out.println(fieldSettingList.get(i));
		
		System.out.println("==== index ====");
		for(int i=0;i<indexSettingList.size();i++)
			System.out.println(indexSettingList.get(i));
		
		System.out.println("==== sort ====");
		for(int i=0;i<sortSettingList.size();i++)
			System.out.println(sortSettingList.get(i));
		
		System.out.println("==== group ====");
		for(int i=0;i<groupSettingList.size();i++)
			System.out.println(groupSettingList.get(i));
		
	}
	
	public void testDatasource(){
		DataSourceSetting datasource = IRSettings.getDatasource("test1",true);
		System.out.println(datasource.driver);
		System.out.println(datasource.url);
		System.out.println(datasource.user);
		System.out.println(datasource.password);
		System.out.println(datasource.incQuery);
		System.out.println(datasource.afterIncQuery);
		System.out.println(datasource.afterFullQuery);
		System.out.println(datasource.bulkSize);
	}
	
	public void testDatatime(){
		Properties indextime = IRSettings.getIndextime("test1",true);
		System.out.println(indextime.get("last_index_time"));
		IRSettings.storeIndextime("test1", "full", "2010-10-10", "2010-10-10", "10 m", 100);
	}
	
//	public void testIRConfig(){
//		IRConfig irConfig = IRSettings.getConfig(true);
//		System.out.println("pk.term.interval = "+irConfig.getInt("pk.term.interval"));
//		System.out.println("pk.bucket.size = "+irConfig.getByteSize("pk.bucket.size"));
//		System.out.println("document.read.buffer.size = "+irConfig.getByteSize("document.read.buffer.size"));
//		System.out.println("document.write.buffer.size = "+irConfig.getByteSize("document.write.buffer.size"));
//		System.out.println("document.parser = "+irConfig.getString("document.parser"));
//	}
	
	public void testPasswd(){
		IRSettings.setHome("c:/fastcat");
		String username = "admin";
		String passwd = "";
//		assertFalse(IRSettings.isCorrectPasswd(username, passwd));
		IRSettings.storePasswd(username, passwd);
		assertTrue(IRSettings.isCorrectPasswd(username, passwd) != null);
		
	}
}
