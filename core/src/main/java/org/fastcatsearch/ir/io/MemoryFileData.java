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
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sangwook
 *
 */
public class MemoryFileData{
	private static Logger logger = LoggerFactory.getLogger(MemoryFileData.class);

	public File f;
	public byte[] data;
	protected int size;
	
	public MemoryFileData(File dir, String filename) throws IOException{
		this(new File(dir, filename));
	}
	public MemoryFileData(File file) throws IOException{
		this.f = file;
		long length = f.length();
		FileInputStream fis = new FileInputStream(f);
		
		if(length > Integer.MAX_VALUE){
			throw new IOException("File size is too long. length = "+length+", Integer.MAX_VALUE="+Integer.MAX_VALUE);
		}
		size = (int) length;
		data = new byte[size];
		logger.info("data size = "+size);
		int n = 0;
		while(n < length){
			n += fis.read(data);
		}
		
		fis.close();
		
	}
	public MemoryFileData(String filename) throws IOException{
		this(null, filename);
	}
	
	public int size() throws IOException{
		return size;
	}
	
	public String toString(){
		return "BufferedFileInput path = "+f.getAbsolutePath();
	}
	public void close() throws IOException {
		
	}

}
