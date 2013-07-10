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

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;


public class PrimaryKeyIndexBulkReaderTest extends TestCase{
	
	String homePath = "testHome/";
	String collection = "test3";
	
	
	public void testRead() throws IOException{
		int indexNum = 2;
		int segmentNumber = 1;
		File readDir= null;// new File(IRSettings.getSegmentPath(collection, indexNum, segmentNumber));
		PrimaryKeyIndexBulkReader reader = new PrimaryKeyIndexBulkReader(readDir, IndexFileNames.primaryKeyMap);
		BytesBuffer buf =  new BytesBuffer(1024 * 8);
		int docNo = -1;
		while((docNo = reader.next(buf)) != -1){
			char[] str = IOUtil.readAChars(buf);
			System.out.println(docNo+" = "+new String(str));
			buf.clear();
		}
	}
	
}
