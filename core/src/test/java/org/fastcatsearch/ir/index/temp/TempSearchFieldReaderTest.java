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

package org.fastcatsearch.ir.index.temp;


import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.index.temp.TempSearchFieldReader;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.FastByteBuffer;

import junit.framework.TestCase;


public class TempSearchFieldReaderTest extends TestCase{
	public void test1() throws IOException{
		File file =new File("testHome/test2/data/temp");
		TempSearchFieldReader reader = new TempSearchFieldReader(0,file, 0);
		while(reader.next()){
			CharVector term = reader.term();
			FastByteBuffer buf = reader.buffer();
			System.out.println("("+(int)term.array[0]+")"+new String(term.array, 1, term.length - 2)+ " = "+term.length);
			
		}
	}

}
