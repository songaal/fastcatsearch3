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

package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;

public class DocumentReaderTest extends TestCase{
	public void testOneDoc() throws IOException, SettingException{
		String homePath = "testHome/";
		String collection = "test2"; 
		SchemaSetting schemaSetting = new SchemaSetting();
		File targetDir = new File(homePath+ "collection/" + collection+ "/data");
		DocumentReader reader = new DocumentReader(schemaSetting, targetDir);
		int docNo = 3;
		
		Document document = reader.readDocument(docNo++);
		document = reader.readDocument(docNo++);
		document = reader.readDocument(docNo);
	}
	
	public void testSequence() throws IOException, SettingException{
		String homePath = "testHome/";
		String collection = "test3";
		int dataSequence = 1;
		int segNum = 1;
		SchemaSetting schemaSetting = new SchemaSetting();
		File targetDir = new File(homePath+ "collection/" + collection+ "/data");
		File segmentDir = new File(targetDir, Integer.toString(segNum));
		System.out.println("segmentDir = "+segmentDir.getAbsolutePath());
		DocumentReader reader = new DocumentReader(schemaSetting, segmentDir);
		int count = reader.getDocumentCount();
		System.out.println("## count="+count);
		for(int docNo=0;docNo<count;docNo++){
//			System.out.println("## "+docNo);
			Document document = reader.readDocument(docNo);
//			for(int i=0;i<schema.fieldSettingList.length;i++){
//				System.out.println(document.get(i));
//			}
		}
	}
}
