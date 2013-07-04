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

package org.fastcatsearch.ir.document.merge;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fastcatsearch.ir.common.SettingException;


public class PrimaryKeyIndexMergerTest extends TestCase{
	
	String homePath = "testHome/";
	String collection ="test3";
	
	public void test1() throws SettingException, IOException{
		
		PrimaryKeyIndexMerger m = new PrimaryKeyIndexMerger();
		
		int indexInterval = 256;
		int indexNumber = 0;
		
		File seg1Dir = new File("");//IRSettings.getSegmentPath(collection, indexNumber, 0));
		File seg2Dir = new File("");//IRSettings.getSegmentPath(collection, indexNumber, 1));
		File targetDir = new File("");//IRSettings.getSegmentPath(collection, indexNumber, 5));
		m.merge(seg1Dir, seg2Dir, targetDir, indexInterval, null);
	}
}
