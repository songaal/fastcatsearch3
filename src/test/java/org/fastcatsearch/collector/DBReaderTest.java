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

import junit.framework.TestCase;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.ir.document.Document;


public class DBReaderTest extends TestCase{

	public void testFullIndexing() throws SettingException, IRException{
		IRSettings.setHome("testHome");
		String collection = "blog_db";
		Schema schema = IRSettings.getSchema(collection, true);
		DataSourceSetting dsSetting = IRSettings.getDatasource(collection, true);
		DBReader dbReader = new DBReader(schema, dsSetting, true);
		
		while(dbReader.hasNext()){
			Document document = dbReader.next();
			System.out.println(document);
		}
	}
	
	public void testAddIndexing() throws SettingException, IRException{
		IRSettings.setHome("testHome");
		String collection = "blog_db";
		Schema schema = IRSettings.getSchema(collection, true);
		DataSourceSetting dsSetting = IRSettings.getDatasource(collection, true);
		DBReader dbReader = new DBReader(schema, dsSetting, false);
		
		while(dbReader.hasNext()){
			Document document = dbReader.next();
			System.out.println(document);
		}
	}
}
