///*
// * Copyright 2013 Websquared, Inc.
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *   http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.fastcatsearch.ir.search;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//
//import org.fastcatsearch.ir.common.IRFileName;
//import org.fastcatsearch.ir.io.BufferedFileInput;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
///**
// * @author sangwook.song
// *
// */
//public class SegmentInfo {
//	private static Logger logger = LoggerFactory.getLogger(SegmentInfo.class);
//	
//	private int segmentNumber;
//	private File segmentDir;
//	private int baseDocNo; //base number
//	private int docCount;
//	private int revision;
//	private long time;
//	
//	public long docPositionFilesSize;
//	public long docStoredFileSize;
//	public long groupDataFileSize;
//	public long[] groupKeyFileSize;
//	public long fieldIndexFileSize;
//	public long filterFieldFileSize;
//	
//	public SegmentInfo(int segmentNumber, String segmentDir) throws IOException{
//		this(segmentNumber, new File(segmentDir));
//	}
//	public SegmentInfo(int segmentNumber, File segmentDir) throws IOException{
//		this(segmentNumber, segmentDir, false);
//	}
//	public SegmentInfo(int segmentNumber, File segmentDir, boolean skipVerify) throws IOException{
//		this.segmentNumber = segmentNumber;
//		this.segmentDir = segmentDir;
//		BufferedFileInput in = new BufferedFileInput(segmentDir, IRFileName.segmentInfoFile);
//		this.baseDocNo = in.readInt();
//		this.docCount = in.readInt();
//		this.revision = in.readInt();
//		this.time = in.readLong();
//
//		//filesize verify
//		docPositionFilesSize = in.readLong();
//		File f = new File(segmentDir, IRFileName.docPosition);
//		if(!skipVerify && f.length() != docPositionFilesSize){
//			logger.error(f.getAbsolutePath()+" filesize is different from expected. actual = "+f.length()+", expected = "+docPositionFilesSize);
//		}
//		docStoredFileSize = in.readLong();
//		f = new File(segmentDir, IRFileName.docStored);
//		if(!skipVerify && f.length() != docStoredFileSize){
//			logger.error(f.getAbsolutePath()+" filesize is different from expected. actual = "+f.length()+", expected = "+docStoredFileSize);
//		}
//		
//		//TODO 색인파일이 필드별로 생성되므로 여기에서 verify하기에는 무리가 있다. 일단 스킵.
////		groupDataFileSize = in.readLong();
////		f = new File(segmentDir, IRFileName.groupDataFile);
////		if(!skipVerify && f.length() != groupDataFileSize){
////			logger.error(IRFileName.groupDataFile+f.getName()+" filesize is different from expected. actual = "+f.length()+", expected = "+groupDataFileSize);
////		}
////		int n = in.readInt();
////		groupKeyFileSize = new long[n];
////		for (int i = 0; i < n; i++) {
////			groupKeyFileSize[i] = in.readLong();
////			f = new File(segmentDir, IRFileName.getSuffixFileName(IRFileName.groupKeyFile, Integer.toString(i)));
////			if(!skipVerify && f.length() != groupKeyFileSize[i]){
////				logger.error(f.getName()+" filesize is different from expected. actual = "+f.length()+", expected = "+groupKeyFileSize[i]);
////			}
////		}
////		fieldIndexFileSize = in.readLong();
////		f = new File(segmentDir, IRFileName.fieldIndexFile);
////		if(!skipVerify && f.length() != fieldIndexFileSize){
////			logger.error(f.getName()+" filesize is different from expected. actual = "+f.length()+", expected = "+fieldIndexFileSize);
////		}
//		in.close();
//	}
//	
//	public int getSegmentNumber(){
//		return segmentNumber;
//	}
//	
//	public void setSegmentNumber(int n){
//		segmentNumber = n;
//	}
//	
//	public int getBaseDocNo(){
//		return baseDocNo;
//	}
//	
//	public int getDocCount(){
//		return docCount;
//	}
//	
//	public int getLastRevision(){
//		return revision;
//	}
//	
//	public File getSegmentDir(){
//		return segmentDir;
//	}
//	
//	public long getTime(){
//		return time;
//	}
//	
//	public String toString(){
//		return ("[SegmentInfo]seq="+segmentNumber+", base no = "+baseDocNo+", docCount = "+docCount+", revision = "+revision+", time = "+new Date(time)+ ", path = "+segmentDir.getAbsolutePath());
//	}
//}
