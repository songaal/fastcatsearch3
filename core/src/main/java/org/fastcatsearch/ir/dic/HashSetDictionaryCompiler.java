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

public class HashSetDictionaryCompiler {
	private static Logger logger = LoggerFactory.getLogger(HashSetDictionaryCompiler.class);
	// private static int BUCKET_SIZE = 16 * 1024;
	boolean splitByWhitespace; // 공백기준으로 단어들을 분리할지 여부.

	public static void main(String[] args) throws IRException {
		HashSetDictionaryCompiler c = new HashSetDictionaryCompiler();

		String splitByWhitespaceValue = System.getProperty("splitByWhitespace");
		if (splitByWhitespaceValue != null && splitByWhitespaceValue.equalsIgnoreCase("true")) {
			c.splitByWhitespace = true;
			logger.debug("Use splitByWhitespace");
		}
		
		if (args.length == 3) {
			c.compile(new File(args[0]), args[1], new File(args[2]));
		} else if (args.length == 4) {
			c.compile(new File[] { new File(args[0]), new File(args[1]) }, args[2], new File(args[3]));
		}
	}

	public void compile(File[] inputList, String charset, File output) throws IRException {
		try {
			logger.debug("Dictionary compile2 start! {}", charset);
			DirBufferedReader br = new DirBufferedReader(inputList, charset);
			int bucketSize = getEstimatedBucketSize(inputList, charset);
			compile0(br, output, bucketSize);
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error("IOException", e);
			throw new IRException(e);
		}
	}

	public void compile(File input, String charset, File output) throws IRException {
		try {
			logger.debug("Dictionary compile start! {}", charset);
			int bucketSize = getEstimatedBucketSize(input, charset);
			DirBufferedReader br = new DirBufferedReader(input, charset);
			compile0(br, output, bucketSize);
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException", e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error("IOException", e);
			throw new IRException(e);
		}
	}

	private int getEstimatedBucketSize(File[] inputList, String charset) throws IOException {
		int bucketSize = 0;
		for (int i = 0; i < inputList.length; i++) {
			bucketSize += getEstimatedBucketSize(inputList[i], charset);
		}
		return bucketSize;
	}

	private int getEstimatedBucketSize(File input, String charset) throws IOException {
		int multipleNumber = 256;
		int count = (int) Math.ceil(getLineCount(input, charset) * 1.5);// 1.5배의 여유공간.
		return (count + multipleNumber - 1) / multipleNumber * multipleNumber;
	}

	private int getLineCount(File input, String charset) throws IOException {
		DirBufferedReader br = new DirBufferedReader(input, charset);
		try {
			int count = 0;
			while (br.readLine() != null) {
				count++;
			}
			return count;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	private void compile0(DirBufferedReader br, File output, int bucketSize) throws IOException, IRException {
		HashSetDictionary dic = new HashSetDictionary(bucketSize);
		String line = null;
		long st = System.currentTimeMillis();
		int cnt = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("//"))
				continue;
			line = line.trim();
			if (line.length() > 0) {
				
				// logger.debug("--"+line);
				int p = line.indexOf('/');
				String a = null;
				if (p > 0) {
					a = line.substring(0, p);
				} else {
					a = line;
				}

				if (splitByWhitespace && a.contains(" ")) {
					// 공백이 있다면 떼어내어 여러 키워드를 입력한다.
					String[] keywordList = a.split(" ");
					for (int i = 0; i < keywordList.length; i++) {
						if(keywordList[i].length() <= 1){
							continue;
						}else if(keywordList[i].length() == 2){
							//영문 또는 숫자이면 추가하지 않는다.
							
							if(Character.isDigit(keywordList[i].charAt(0)) && Character.isDigit(keywordList[i].charAt(1))){
								continue;
							}
							if(isAlphabetic(keywordList[i].charAt(0)) && isAlphabetic(keywordList[i].charAt(1))){
								continue;
							}
							
						}
						CharVector term = new CharVector(keywordList[i]);
						dic.put(term);
						cnt++;
					}
				} else {
					// logger.debug("* " + a);
					if(a.length() <= 1){
						continue;
					}
					CharVector term = new CharVector(a);
					dic.put(term);
					cnt++;
				}
			}
		}
		dic.save(output);
		logger.debug("Dictionary compile done.. total {} words. putTerm={} {}ms", new Object[]{dic.count(), cnt, System.currentTimeMillis() - st});
	}
	
	//
	// java 1.7 has method 'isAlphabetic' but 1.6 doesn't
	// so we add it for compiler compatibility
	//
	public boolean isAlphabetic(char c) {
		if((c>='A' && c<='Z') || (c>='a' && c<='z')) {
			return true;
		}
		return false;
	}
}
