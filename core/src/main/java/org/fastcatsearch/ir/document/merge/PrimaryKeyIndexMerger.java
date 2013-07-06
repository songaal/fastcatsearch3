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

package org.fastcatsearch.ir.document.merge;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkWriter;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PrimaryKeyIndexMerger {
	private static Logger logger = LoggerFactory.getLogger(PrimaryKeyIndexMerger.class);
	private static Logger debugLogger = LoggerFactory.getLogger("DEBUG_LOG");
	private PrimaryKeyIndexBulkWriter w;
	
	public PrimaryKeyIndexMerger(){ 
		debugLogger.debug("--------------------------------------------");
	}
	
	/*
	 * 증분색인후 이전 revision의 pk와 새 revision의 pk를 머징하면서  동일한 pk가 발견되면 이전 revision의 문서번호를 deleteSet에 넣어준다.
	 * 이때문에, 동일한 문서를 증분색인하더라도 중복으로 검색되지 않는것이다. 
	 * For Document primary key map
	 * file2's primary key is appended at file1's ends
	 * */
	public int merge(File prevRevisionDir, File tempPkFile, File newPkFile, int indexInterval, BitSet deleteSet) throws IOException {
		int inSegmentDocUpdateCount = 0; //동일세그먼트내에서 이전 rev와 새 rev사이의 중복문서가 발견될 경우 update사이즈를 증가시킨다.
		
		PrimaryKeyIndexBulkReader r1 = null;
		PrimaryKeyIndexBulkReader r2 = null;
		
		if(prevRevisionDir.isDirectory())
			r1 = new PrimaryKeyIndexBulkReader(prevRevisionDir, IRFileName.primaryKeyMap);
		else
			r1 = new PrimaryKeyIndexBulkReader(prevRevisionDir);
		
		if(tempPkFile.isDirectory())
			r2 = new PrimaryKeyIndexBulkReader(tempPkFile, IRFileName.primaryKeyMap);
		else
			r2 = new PrimaryKeyIndexBulkReader(tempPkFile);
		
		w = new PrimaryKeyIndexBulkWriter(newPkFile, indexInterval);
		
		BytesBuffer buf1 = new BytesBuffer(1024);
		BytesBuffer buf2 = new BytesBuffer(1024);
		
		int docNo1 = r1.next(buf1);
		int docNo2 = r2.next(buf2);
		
		//merge in ascending order 
		while(docNo1 >= 0 && docNo2 >= 0){
			
			int ret = BytesBuffer.compareBuffer(buf1, buf2);
			
			if(ret == 0){
				//must write doc2 number because doc1 was replaced with doc2.
				//prev doc no put to deleteSet
				
				//TODO 이 코드는 디버그용임..
				if(debugLogger.isDebugEnabled()){
					int id = IOUtil.readInt(buf1.bytes, 0);
//					debugLogger.debug("{} / {} -- delete", docNo1, id);
//					debugLogger.debug("{} / {} -- PK0", docNo2, id);
				}
				//////
				if(deleteSet != null){
					deleteSet.set(docNo1);
					inSegmentDocUpdateCount++;
//					logger.debug("$$ delete docid= {} replace ==> {}", docNo1, docNo2);
				}
				
				w.write(buf1, docNo2);
				buf1.clear();
				docNo1 = r1.next(buf1);
				buf2.clear();
				docNo2 = r2.next(buf2);
			}else if(ret < 0){
				if(debugLogger.isDebugEnabled()){
					int id = IOUtil.readInt(buf1.bytes, 0);
					debugLogger.debug("{} / {} -- PK1", docNo1, id);
				}
				w.write(buf1, docNo1);
				buf1.clear();
				docNo1 = r1.next(buf1);
			}else{
				if(debugLogger.isDebugEnabled()){
					int id = IOUtil.readInt(buf2.bytes, 0);
					debugLogger.debug("{} / {} -- PK2", docNo2, id);
				}
				w.write(buf2, docNo2);
				buf2.clear();
				docNo2 = r2.next(buf2);
			}
		}
		
		while(docNo1 >= 0){
			if(debugLogger.isDebugEnabled()){
				int id = IOUtil.readInt(buf1.bytes, 0);
				debugLogger.debug("{} / {} -- PK1", docNo1, id);
			}
			w.write(buf1, docNo1);
			buf1.clear();
			docNo1 = r1.next(buf1);
		}
		
		while(docNo2 >= 0){
			if(debugLogger.isDebugEnabled()){
				int id = IOUtil.readInt(buf2.bytes, 0);
				debugLogger.debug("{} / {} -- PK2", docNo2, id);
			}
			w.write(buf2, docNo2);
			buf2.clear();
			docNo2 = r2.next(buf2);
		}
		
		r1.close();
		r2.close();
		w.close();
		debugLogger.debug("--------------------------------------------");
		return inSegmentDocUpdateCount;
	}

	/*
	 * For Group key map
	 * */
	public boolean merge(File file1, long base1, File file2, long base2, IndexOutput pkmapOutput, IndexOutput pkmapIndexOutput, int indexInterval) throws IOException {
		PrimaryKeyIndexBulkReader r1 = null;
		PrimaryKeyIndexBulkReader r2 = null;
		
		if(file1.isDirectory())
			r1 = new PrimaryKeyIndexBulkReader(file1, IRFileName.primaryKeyMap, base1);
		else
			r1 = new PrimaryKeyIndexBulkReader(file1, base1);
		
		if(file2.isDirectory())
			r2 = new PrimaryKeyIndexBulkReader(file2, IRFileName.primaryKeyMap, base2);
		else
			r2 = new PrimaryKeyIndexBulkReader(file2, base2);
		
		w = new PrimaryKeyIndexBulkWriter(pkmapOutput, pkmapIndexOutput, indexInterval, true);
		
		BytesBuffer buf1 = new BytesBuffer(1024);
		BytesBuffer buf2 = new BytesBuffer(1024);
		
		int docNo1 = r1.next(buf1);
		int docNo2 = r2.next(buf2);
		
		//merge in ascending order 
		while(docNo1 >= 0 && docNo2 >= 0){
			
			int ret = BytesBuffer.compareBuffer(buf1, buf2);
			
			if(ret == 0){
				//must write doc2 number because doc1 was replaced with doc2.
				
				w.write(buf1, docNo2);
				buf1.clear();
				docNo1 = r1.next(buf1);
				buf2.clear();
				docNo2 = r2.next(buf2);
			}else if(ret < 0){
				w.write(buf1, docNo1);
				buf1.clear();
				docNo1 = r1.next(buf1);
			}else{
				w.write(buf2, docNo2);
				buf2.clear();
				docNo2 = r2.next(buf2);
			}
		}
		
		while(docNo1 >= 0){
			w.write(buf1, docNo1);
			buf1.clear();
			docNo1 = r1.next(buf1);
		}
		
		while(docNo2 >= 0){
			w.write(buf2, docNo2);
			buf2.clear();
			docNo2 = r2.next(buf2);
		}
		
		r1.close();
		r2.close();
		w.close();
		
		return true;
	}
	
	public int getKeyCount(){
		return w.getKeyCount();
	}
	
	public int getKeyIndexCount(){
		return w.getKeyIndexCount();
	}
	
}
