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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpecialCharacterMapCompiler {
	private static Logger logger = LoggerFactory.getLogger(SpecialCharacterMapCompiler.class);
	
	public static void main(String[] args) throws IRException {
		SpecialCharacterMapCompiler c = new SpecialCharacterMapCompiler();
		c.compile(new File(args[0]), args[1], new File(args[2]));
	}
	
	public void compile(File input, String charset, File output) throws IRException{
		long st = System.currentTimeMillis();
		try {
			logger.debug("SpecialCharacterMap compile start!");
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input),charset));
			IndexOutput out = new BufferedFileOutput(output);
			String line = null;
			int cnt = 0;
			while((line = br.readLine()) != null){
				String[] str = line.split(",");
				for (int i = 0; i < str.length; i++) {
					int ch = Integer.parseInt(str[i], 16);
					
//					if(cnt % 16 == 0)
//						System.out.println();
//					System.out.println(cnt+" = "+(char)ch+"/ "+(char)cnt);
//					
//					if(cnt > 300)
//						break;
					
					out.writeUChar(ch);
					cnt++;
				}
				
//				if(cnt > 300)
//					break;
			}
			out.flush();
			out.close();
			br.close();
			logger.debug("SpecialCharacterMap compile done.. total {} word. {}", cnt, Formatter.getFormatTime(System.currentTimeMillis() - st));
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException",e);
			throw new IRException(e);
		} catch (IOException e) {
			logger.error("IOException",e);
			throw new IRException(e);
		}
	}
	
}
