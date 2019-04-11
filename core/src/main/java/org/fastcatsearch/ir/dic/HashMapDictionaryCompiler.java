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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DirBufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @deprecated
 * */
public class HashMapDictionaryCompiler {
	private static Logger logger = LoggerFactory.getLogger(HashMapDictionaryCompiler.class);
	private static int BUCKET_SIZE = 16 * 1024;
	
	public static void main(String[] args) throws IRException {
		HashMapDictionaryCompiler c = new HashMapDictionaryCompiler();
		c.compile(new File(args[0]), args[1], new File(args[2]));
	}
	
	public void compile(File input, String charset, File output) throws IRException{
		try {
			logger.debug("Dictionary compile start!");
			HashMapDictionary dic = new HashMapDictionary(BUCKET_SIZE);
			DirBufferedReader br = new DirBufferedReader(input, charset);
			String line = null;
			long st = System.currentTimeMillis();
			int cnt = 0;
			int[] startPos = new int[128];
			
			while((line = br.readLine())!= null){
				
				if(line.length() > 0){
					
//					logger.debug("--"+line);
					String key = null;
					CharVector[] termList = null;
					
					int p = line.indexOf('\t');
					if(p > 0){
						key = line.substring(0, p);
//						logger.debug("key = "+key);
						String value = line.substring(p + 1);
						
						String[] tmp = value.split(",");
						if(tmp.length == 1 && tmp[0].length() == 0){
							termList = new CharVector[]{new CharVector(key)};
//							logger.debug("val = "+termList[0]);
						}else{
							termList = new CharVector[tmp.length];
							for (int i = 0; i < tmp.length; i++) {
								termList[i] = new CharVector(tmp[i]);
							}
						}
					}else{
						logger.warn("Cannot find '\\\\t' character. ignore read = "+ line);
						continue;
					}

					CharVector term = new CharVector(key);
//					logger.debug("put "+term+" : "+termList);
					dic.put(term, termList);
					cnt++;
				}
			}
			dic.save(output);
			logger.debug("Dictionary compile done.. total {} word. {}ms", cnt, System.currentTimeMillis() - st);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException",e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error("IOException",e);
			throw new IRException(e);
		}
	}
	
}
