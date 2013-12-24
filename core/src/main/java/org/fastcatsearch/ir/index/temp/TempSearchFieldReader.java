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

package org.fastcatsearch.ir.index.temp;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class TempSearchFieldReader {
	private static Logger logger = LoggerFactory.getLogger(TempSearchFieldReader.class);
	
	private int sequence;
	private String indexId;
	private CharVector term;
	private BytesRef buffer;
	private IndexInput tempInput;
	private int left;
	
	public TempSearchFieldReader(int sequence, String indexId, File tempFile, long startPos) throws IOException{
		this.sequence = sequence;
		this.indexId = indexId;
		this.tempInput = new BufferedFileInput(tempFile);
		tempInput.seek(startPos);
		logger.debug("{} - reader input position = {}", indexId, startPos);
		logger.debug("{} - filesize = {}", indexId, tempFile.length());
		left = tempInput.readInt();
		logger.debug("{} - count = {}", indexId, left);
	}
	
	//read next field terms
//	public boolean resume(){
//		try {
////			logger.debug(id+" - reader resume position = "+tempInput.position());
//			left = tempInput.readInt();
//		} catch (IOException e) {
//			return false;
//		}
//		logger.debug("{} - count = {}", indexId, left);
//		return true;
//	}
	public void close() throws IOException{
		tempInput.close();
	}
	public String indexId(){
		return indexId;
	}
	
	public int sequence(){
		return sequence;
	}
	
	public boolean next() throws IOException{
		if(left == 0){
			term = null;
			buffer = null;
			return false;
		}
		
		char[] array = null;
		try{
			array = tempInput.readUString();
		}catch(EOFException e){
			logger.error(e.getMessage(),e);
			logger.debug("{} - count = {}", indexId, left);
			throw e;
		}
		int len = array.length;
		term = new CharVector(array, 0, len);
		
		int bufLength = tempInput.readVInt();
		buffer = new BytesRef(bufLength);
		tempInput.readBytes(buffer);
		left--;
		
		return true;
	}
	
	public int left(){
		return left;
	}
	public CharVector term(){
		return term;
	}
	public BytesRef buffer(){
		return buffer;
	}
}
