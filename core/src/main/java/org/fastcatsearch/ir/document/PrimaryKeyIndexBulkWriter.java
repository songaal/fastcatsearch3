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

import java.io.IOException;

import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * write sorted keys to make a pk map.
 * @author sangwook.song
 *
 */
public class PrimaryKeyIndexBulkWriter {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexBulkWriter.class);
	
	private IndexOutput output;
	private IndexOutput indexOutput;
	private int indexInterval;
	private int keyCount;
	private int keyIndexCount;

	public PrimaryKeyIndexBulkWriter(IndexOutput output, IndexOutput indexOutput, int indexInterval) throws IOException{
		this.output = output;
		this.indexOutput = indexOutput;
		this.indexInterval = indexInterval;
		output.writeInt(0);
		indexOutput.writeInt(0);
	}
	
	
	public void done() throws IOException{
		long t = output.position();
		output.seek(0);
		output.writeInt(keyCount);
//		output.close();
		
		t = indexOutput.position();
		indexOutput.seek(0);
		indexOutput.writeInt(keyIndexCount);
//		indexOutput.close();
		
		
	}
	public void write(BytesBuffer buf, int value) throws IOException{
		
		//write pkmap index
		if(keyCount % indexInterval == 0){
			indexOutput.writeVInt(buf.remaining());
			indexOutput.writeBytes(buf);
			indexOutput.writeLong(output.position());
			keyIndexCount++;
		}
		
		output.writeVInt(buf.remaining());
		output.writeBytes(buf);
		output.writeInt(value);
		keyCount++;
	}
	
	public int getKeyCount(){
		return keyCount;
	}
	
	public int getKeyIndexCount(){
		return keyIndexCount;
	}
}
