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

package org.fastcatsearch.ir.dic;

import java.io.File;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.dic.HashSetDictionary;
import org.fastcatsearch.ir.io.CharVector;


import junit.framework.TestCase;

public class DicIOTest extends TestCase {

	
	public void testWrite() throws IRException
	{
		int BUCKET_SIZE = 16*1024;
		HashSetDictionary dic = new HashSetDictionary(BUCKET_SIZE);
		dic.put(new CharVector("test"));
		File fp = new File("dic/test.dic");
		dic.save(fp);
		System.out.println("Count : " + dic.count());
	}
	
	
	public void testRead() throws IRException
	{
		try
		{
		int BUCKET_SIZE = 16*1024;
		File fp = new File("dic/nKorean.dic");
		if ( fp.exists() )
		{
		HashSetDictionary dic = new HashSetDictionary(fp);		
		
		if ( dic.contains(new CharVector("test")))
			System.out.println("Read Success");		
		else
			System.out.println("Read Failed");
		}
		else
			System.out.println("File not Exists");
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
	}
}
