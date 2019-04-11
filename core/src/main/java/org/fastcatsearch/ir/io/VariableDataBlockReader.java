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

import org.fastcatsearch.ir.io.cache.CachedBlock;
import org.fastcatsearch.ir.io.cache.LRUBlockCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VariableDataBlockReader {
	private static Logger logger = LoggerFactory.getLogger(VariableDataBlockReader.class);
	
	private LRUBlockCache cache;
	private IndexInput input;
	private int blockSize;
	
	public VariableDataBlockReader(File dir, String filename, int blockSize) throws IOException {
		input = new BufferedFileInput(dir, filename);
		this.blockSize = blockSize;
		cache = new LRUBlockCache();
		logger.debug("VariableDataBlockReader "+filename+", blockSize="+blockSize);
	}

	public void get(long position, byte[] data, int offset, int length) throws IOException {
		int block = (int) (position / blockSize);
		int pos = (int) (position % blockSize);
		
		int nread = 0;
		while(nread < length){
			//블럭을 연이어 계속읽을수 있다.
			int left = blockSize - pos;
			if(left == 0){
				block++;
				pos = 0;
				left = blockSize - pos;
			}
			int toRead = left < length ? left : length;
			
			CachedBlock blockObj = cache.getBlock(block);
			if(blockObj == null){
				//Read from file
				blockObj = new CachedBlock(block, new byte[blockSize]);
	
				synchronized(input){
					input.seek(block * blockSize);
					input.readBytes(blockObj.buf, 0, blockSize);
				}
				
				cache.putBlock(blockObj);
				
				for (int i = 0; i < toRead; i++) {
					data[offset + i] = blockObj.buf[pos + i];
				}
			}else{
				for (int i = 0; i < toRead; i++) {
					data[offset + i] = blockObj.buf[pos + i];
				}
			}
			
			pos += toRead;
			nread += toRead;
		}
		cache.check();
	}
	
	public void close() throws IOException{
		input.close();
	}
	
}
