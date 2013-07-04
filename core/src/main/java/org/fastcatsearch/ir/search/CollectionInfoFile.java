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

package org.fastcatsearch.ir.search;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CollectionInfoFile {
	private static Logger logger = LoggerFactory.getLogger(CollectionInfoFile.class);
	private int segmentSize;
	private SegmentInfo[] segmentInfoList;
	private File f;
	private File collectionDataDir;
	
	public CollectionInfoFile(File collectionDataDir) throws IOException{
		this.collectionDataDir = collectionDataDir;
		f = new File(collectionDataDir, IRFileName.collectionInfoFile);
		if(f.exists()){
			BufferedFileInput in = new BufferedFileInput(f);
			this.segmentSize = in.readInt();
			in.close();
			load();
		}else{
			segmentSize = 0;
			segmentInfoList = new SegmentInfo[8];
			save();
		}
		
	}
	
	public CollectionInfoFile(File collectionDataDir, int segmentSize) throws IOException{
		this.collectionDataDir = collectionDataDir;
		this.segmentSize = segmentSize;
		f = new File(collectionDataDir, IRFileName.collectionInfoFile);
		save();
		
		load();
	}

	private void load() throws IOException{
		logger.info("segmentSize = "+segmentSize);
		int size = 8;
		while(size < segmentSize)
			size += 8;
		segmentInfoList = new SegmentInfo[size];
		for (int segmentNumber = 0; segmentNumber < segmentSize; segmentNumber++) {
			File segmentDir = new File(collectionDataDir, Integer.toString(segmentNumber));
			SegmentInfo si = new SegmentInfo(segmentNumber, segmentDir);
			logger.info(segmentNumber+")"+si);
			segmentInfoList[segmentNumber] = si;
		}
	}
	public SegmentInfo[] getSegmentInfoList(){
		return segmentInfoList;
	}
	
	public int getSegmentSize(){
		return segmentSize;
	}
	
	public boolean addSegment(SegmentInfo segmentInfo){

		if(segmentInfoList.length <= segmentSize){
			SegmentInfo[] newList = new SegmentInfo[segmentInfoList.length * 2];
			System.arraycopy(segmentInfoList, 0, newList, 0, segmentSize);
			segmentInfoList = newList;
		}
		
		segmentInfoList[segmentSize++] = segmentInfo;
		
		return true;
	}
	
	public boolean overrideLastSegment(SegmentInfo segmentInfo){
		segmentInfoList[segmentSize - 1] = segmentInfo;
		return true;
	}
	
	public void save() throws IOException{
		BufferedFileOutput out = new BufferedFileOutput(f);
		out.writeInt(segmentSize);
		logger.debug("CollectionInfoFile saved segmentSize={}", segmentSize);
		out.close();
	}
}
