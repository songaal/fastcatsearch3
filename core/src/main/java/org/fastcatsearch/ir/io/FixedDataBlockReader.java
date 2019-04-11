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
import org.fastcatsearch.ir.query.RankInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FixedDataBlockReader {
	private static Logger logger = LoggerFactory.getLogger(FixedDataBlockReader.class);
	
	private LRUBlockCache cache;
	private IndexInput input;
	private int dataSize;
	private int dataPerBlock;
	private int blockSize;
	
	public FixedDataBlockReader(File dir, String filename, int dataSize, int blockSize) throws IOException {
		input = new BufferedFileInput(dir, filename);
		this.dataSize = dataSize;
		this.dataPerBlock = blockSize / dataSize;
		int newBlockSize = dataPerBlock * dataSize;
		this.blockSize = newBlockSize;
		cache = new LRUBlockCache();
		logger.debug("FixedDataBlockReader={}, blockSize={}", filename, newBlockSize);
	}

	public void get(int docNo, byte[] data, int offset) throws IOException {
		int block = docNo / dataPerBlock;
		int pos = (docNo % dataPerBlock) * dataSize;
		CachedBlock blockObj = cache.getBlock(block);
		if(blockObj == null){
			//Read from file
			blockObj = new CachedBlock(block, new byte[blockSize]);

			synchronized(input){
				input.seek(block * blockSize);
				input.readBytes(blockObj.buf, 0, blockSize);
			}
			
			cache.putBlock(blockObj);
			
			for (int i = 0; i < dataSize; i++) {
				data[offset + i] = blockObj.buf[pos + i];
			}
		}else{
			for (int i = 0; i < dataSize; i++) {
				data[offset + i] = blockObj.buf[pos + i];
			}
		}
		cache.check();
	}
	
	public void getBulk(RankInfo[] rankInfoList, int n, byte[] buf) throws IOException {
		int offset = 0;
		int t1 = 0, t2 = 0;
		for (int k = 0; k < n; k++) {
			int docNo = rankInfoList[k].docNo();
			int block = docNo / dataPerBlock;
			int pos = (docNo % dataPerBlock) * dataSize;
//			logger.info("docNo= "+docNo+", dataPerBlock="+dataPerBlock+", block="+block+", pos="+pos);
			CachedBlock blockObj = cache.getBlock(block);
//			logger.info("data = "+data);
			if(blockObj == null){
				//Read from file
				blockObj = new CachedBlock(block, new byte[blockSize]);
				long st = System.currentTimeMillis();
				synchronized(input){
//					t1 += (System.currentTimeMillis() -st);
					input.seek(block * blockSize);
					input.readBytes(blockObj.buf, 0, blockSize);
//					t2 += (System.currentTimeMillis() -st);
				}
				
				cache.putBlock(blockObj);
//				logger.info("input= "+input.size()+", position="+block * blockSize);
				
				for (int i = 0; i < dataSize; i++) {
					buf[offset++] = blockObj.buf[pos + i];
				}
			}else{
				for (int i = 0; i < dataSize; i++) {
					buf[offset++] = blockObj.buf[pos + i];
				}
			}
		}
		
		cache.check();
//		logger.info("t1 = "+t1+", t2="+t2);
		
	}
	public void close() throws IOException{
		input.close();
	}
	
}
