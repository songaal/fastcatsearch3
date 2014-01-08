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

package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 임시로 flush하여 저장한 pk를 읽어들인다.
 * @see LargePrimaryKeyIndexWriter 
 * */
public class TempPrimaryKeyIndexReader {
	private static Logger logger = LoggerFactory.getLogger(TempPrimaryKeyIndexReader.class);
	
	private BufferedFileInput input;
	private int keyCount;
	
	private BytesBuffer key;
	private int docNo;
	
	public TempPrimaryKeyIndexReader(File file) throws IOException{
		input  = new BufferedFileInput(file);
		keyCount = input.readInt();
		
//		key = new BytesBuffer(1024); //max 1k
	}
	public BytesBuffer key(){
		return key;
	}
	public int docNo(){
		return docNo;
	}
	
	public boolean next() throws IOException {
		if(keyCount > 0){
			
			int len = input.readVInt();
			key = new BytesBuffer(len);
//			key.limit(len);
			input.readBytes(key);
			docNo = input.readInt();
			
			keyCount--;
			return true;
		}else{
			key = null;
			docNo = -1;
			return false;
		}
	}
	
	public void close() throws IOException{
		input.close();
	}
}
