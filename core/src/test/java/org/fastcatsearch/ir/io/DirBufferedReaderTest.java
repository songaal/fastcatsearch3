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

package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.io.DirBufferedReader;

import junit.framework.TestCase;

	public class DirBufferedReaderTest extends TestCase {
		public void test1() throws IOException{
			File f = new File("/Users/websqrd/Desktop/aa");
			DirBufferedReader r = new DirBufferedReader(f, "utf-8");
			String line = null;
			while((line = r.readLine()) != null){
//				System.out.println(">>dsfsdffffffffff"+line);
			}
			r.close();
	}
}
