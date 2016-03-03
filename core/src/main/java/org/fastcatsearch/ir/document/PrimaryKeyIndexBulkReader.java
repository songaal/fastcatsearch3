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


public class PrimaryKeyIndexBulkReader {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexBulkReader.class);
	
	private BufferedFileInput input;
	private int keyCount;
    private File file;
	
	public PrimaryKeyIndexBulkReader(File file) throws IOException{
        this.file = file;
		input  = new BufferedFileInput(file);
		keyCount = input.readInt();
	}
	
	public int next(BytesBuffer buf) throws IOException{
		if(keyCount <= 0){
			return -1;
		}else
			keyCount--;
			
		int len = input.readVInt();
		buf.limit(len);
		input.readBytes(buf);
		int docNo = input.readInt();
	
		return docNo;
	}

    public File getFile() {
        return file;
    }

    public void close() throws IOException{
		input.close();
	}
}
