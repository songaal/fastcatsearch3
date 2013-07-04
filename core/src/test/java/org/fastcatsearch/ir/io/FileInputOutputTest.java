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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

/**
 * @author sangwook
 *
 */
public class FileInputOutputTest extends TestCase{
	/**
	 * FileOutputStream의 FileChannel은 position변경을 할수 있는가? 
	 * 
	 * @throws IOException
	 */
	public void test1() throws IOException{
		ByteBuffer buf = ByteBuffer.allocate(16);
		buf.putLong(2345);
		buf.putLong(101);
		buf.flip();
		FileOutputStream fos = new FileOutputStream("test");
		FileChannel fc = fos.getChannel();
		
		fc.position(1000);
		fc.write(buf);
		fos.close();
		fc.close();
		buf.clear();
		
		
		FileInputStream fis = new FileInputStream("test");
		fc = fis.getChannel();
		fc.read(buf, 1000);
		buf.flip();
		System.out.println(">>"+buf.getLong()+","+buf.getLong());
		fis.close();
		fc.close();
		
		
		
		
		
	}
}
