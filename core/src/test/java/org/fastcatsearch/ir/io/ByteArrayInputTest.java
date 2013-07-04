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

import java.io.IOException;

import org.fastcatsearch.ir.io.ByteArrayInput;



import junit.framework.TestCase;

public class ByteArrayInputTest extends TestCase{
	public void testReadComplementByte() throws IOException{
		byte[] data = new byte[]{3,6,9,12,111,122};
		byte[] readData = new byte[data.length];
		ByteArrayInput in = new ByteArrayInput(data,0, data.length);
		in.readComplementBytes(readData, 0, readData.length);
		
		for(int i=0;i<data.length;i++){
			int c = 0xFF - data[i];
			System.out.println(c+":"+(readData[i] & 0xFF));
			assertEquals(c, readData[i] & 0xFF);
		}
	}

}
