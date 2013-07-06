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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IRFileName;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.document.DocumentRestorer;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.document.merge.PrimaryKeyIndexMerger;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.MultiKeyEntry;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.IndexOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PkRefSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CollectionHandler {
	
	private static Logger logger = LoggerFactory.getLogger(CollectionHandler.class);
	private SegmentSearcher[] segmentSearcherList;
	private DocumentReader[] documentReaderList;
	private SegmentInfo[] segmentInfoList;
	private int segmentSize;
//	private Schema schema;
	private Schema schema;
	private String collectionId;
	private File collectionDir;
	private CollectionInfoFile collectionInfoFile;
	private DataSequenceFile dataSequenceFile;
	private CollectionContext collectionContext;
	
	private long startedTime;
	private IndexConfig indexConfig;
	
	private CollectionSearcher collectionSearcher;
	
	public CollectionHandler(CollectionContext collectionContext) throws IRException, SettingException{
		//-1 means reading dataSequence from file.
//		this(collection, collectionDir, schema, indexConfig, -1);
		this.collectionContext = collectionContext;
	}
	
	public CollectionHandler(String collection, File collectionDir, Schema schema, IndexConfig indexConfig, int dataSequence) throws IRException, SettingException{
		this.indexConfig = indexConfig;
		this.collectionId = collection;
		this.collectionDir = collectionDir;
		this.schema = schema;
//		String collectionHomeDir = IRSettings.getCollectionHome(collection);
//		schema = IRSettings.getSchema(collection, true);
//		IRConfig irConfig = IRSettings.getConfig(true);
		dataSequenceFile = new DataSequenceFile(collectionDir, dataSequence);
		int seq = dataSequenceFile.getSequence();
		File dataDir = dataSequenceFile.getDataDirFile();
		//		String dirName = (seq == 0 ? "data" : "data"+seq);
//		dataDir = new File(collectionDir, dirName);
//		dataDir = IRSettings.getCollectionDataPath(collection, seq);
		logger.info("["+collection+"] Open data"+seq+", "+dataDir);
//		new File(dataDir).mkdirs();
		dataDir.mkdir();
		
		try {
			collectionInfoFile = new CollectionInfoFile(dataDir);
		} catch (IOException e) {
			throw new IRException(e);
		}
		segmentSize = collectionInfoFile.getSegmentSize();
		segmentInfoList = collectionInfoFile.getSegmentInfoList();
		
		loadSearcherAndReader();
		
//		String HASClassName = irConfig.getString("search.highlightAndSummary");
//		if(HASClassName.length() > 0)
//			has = (HighlightAndSummary)IRSettings.classLoader.loadObject(HASClassName);
		
		
		this.collectionSearcher = new CollectionSearcher(this);
		
		startedTime = System.currentTimeMillis();
	}
	
	public CollectionContext collectionContext(){
		return collectionContext;
	}
	public CollectionSearcher searcher(){
		return collectionSearcher;
	}
	
	private void loadSearcherAndReader() throws IRException{
		int DEFAULT_SIZE = 8;
		while(segmentSize > DEFAULT_SIZE){
			DEFAULT_SIZE += 8;
		}
		segmentSearcherList = new SegmentSearcher[DEFAULT_SIZE];
		documentReaderList = new DocumentReader[DEFAULT_SIZE];
		
		if(segmentSize > 0){
			//last segment.
			SegmentInfo segmentInfo = segmentInfoList[segmentSize - 1];
			File lastSegmentDir = segmentInfo.getSegmentDir();
			int lastRevision = segmentInfo.getLastRevision();
			try {
				for (int i = 0; i < segmentSize; i++) {
					SegmentInfo si = segmentInfoList[i];
					File segmentDir = si.getSegmentDir();
					int baseDocNo = segmentInfoList[i].getBaseDocNo();
					int docCount = segmentInfoList[i].getDocCount();
					int revision = segmentInfoList[i].getLastRevision();
					
					BitSet deleteSet = null;
					if(i < segmentSize - 1){
						deleteSet = new BitSet(IRFileName.getRevisionDir(lastSegmentDir, lastRevision), IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
					}else{
						deleteSet = new BitSet(IRFileName.getRevisionDir(segmentDir, revision), IRFileName.docDeleteSet);
					}
					
					segmentSearcherList[i] = new SegmentSearcher(schema, segmentDir, baseDocNo, docCount, deleteSet, revision);
					documentReaderList[i] = new DocumentReader(schema, segmentDir, baseDocNo);
				}
			} catch (IOException e) {
				throw new IRException(e);
			}
			
		}
	}
	public void close() throws IOException{
		for (int i = 0; i < segmentSize; i++) {
			documentReaderList[i].close();
			segmentSearcherList[i].close();
		}
		
	}
	
	public String collectionId(){
		return collectionId;
	}
	
	public int getDataSequence(){
		return dataSequenceFile.getSequence();
		
	}
	
	public void printSegmentStatus(){
		for (int i = 0; i < segmentSize; i++) {
			SegmentInfo info = segmentInfoList[i];
			logger.info("{}", info);
		}
	}
	
	public int[] getAllSegmentNumberList(){
		int[] list = new int[segmentSize];
		for (int i = 0; i < segmentSize; i++) {
			SegmentInfo segmentInfo = segmentInfoList[i];
			list[i] = segmentInfo.getSegmentNumber();
		}
		return list;
	}
	
	public SegmentInfo getLastSegmentInfo(){
		if(segmentSize == 0)
			return null;
		
		//get Last segment Number
		return segmentInfoList[segmentSize - 1];
	}
	
	public SegmentInfo getSegmentInfo(int segmentNumber){
		if(segmentSize == 0)
			return null;
		
		//get Last segment Number
		return segmentInfoList[segmentNumber];
	}
	
	public SegmentSearcher getSegmentSearcher(int segmentNumber) {
		if(segmentSize == 0)
			return null;
		
		return segmentSearcherList[segmentNumber];
	}
	
	public DocumentReader getDocumentReader(int m) {
		if(segmentSize == 0)
			return null;
		
		return documentReaderList[m];
	}
	
	
	public int getLastSegmentNumber(){
		if(segmentSize == 0)
			return -1;
		
		//get Last segment Number
		SegmentInfo segmentInfo = segmentInfoList[segmentSize - 1];
		return segmentInfo.getSegmentNumber();
	}
	
	public int getNextSegmentNumber(){
		if(segmentSize == 0)
			return 0;
			
		//get Last segment Number
		SegmentInfo segmentInfo = segmentInfoList[segmentSize - 1];
		return segmentInfo.getSegmentNumber() + 1;
	}

	public Schema schema(){
		return schema;
	}
	
	/**
	 * 색인시 세그먼트를 하나 추가할 경우 호출함.
	 * 전체색인시는 세그먼트 0번부터 생성하므로 무조건 이 메서드를 호출하고
	 * 증분색인시는 세그먼트내 문서갯수가 많아서 새로운 세그먼트를 만들어야 할 경우 호출된다. 
	 * 
	 * full index : segmentNumber = 0
	 * add index : segmentNumber > 0
	 * */
	public int[] addSegment(int segmentNumber, File newSegmentDir, DeleteIdSet deleteSet) throws IOException, IRException{
//		File newSegmentDir = new File(IRSettings.getSegmentPath(collectionName, dataSequenceFile.getSequence(), segmentNumber));
		SegmentInfo newSegmentInfo = new SegmentInfo(segmentNumber, newSegmentDir);
		int baseDocNo = newSegmentInfo.getBaseDocNo();
		int docCount = newSegmentInfo.getDocCount();
		logger.debug("^^^ Segment "+segmentNumber+", base = "+baseDocNo+", docCount = "+newSegmentInfo.getDocCount()+", segmentSize = "+segmentSize);
		
		int revision = newSegmentInfo.getLastRevision();
		
//		SegmentSearcher newSegmentSearcher = 
//		DocumentReader newDocumentReader = new DocumentReader(schema, newSegmentDir, baseDocNo);
		int[] updateAndDeleteSize = {0, 0};
		BitSet[] deleteSetList = null;
		if(segmentSize > 0)
			deleteSetList = addEachSegmentDeleteSet(newSegmentDir, revision, deleteSet, updateAndDeleteSize);
		
		/*
		 * add segment 
		 * */
		//add new segment
		collectionInfoFile.addSegment(newSegmentInfo);
		//update segmentInfoList
		segmentInfoList = collectionInfoFile.getSegmentInfoList();
		int tempSegmentSize = collectionInfoFile.getSegmentSize();
		
		if(tempSegmentSize > segmentSearcherList.length){
			SegmentSearcher[] newSegmentSearcherList = new SegmentSearcher[tempSegmentSize * 2];
			DocumentReader[] newDocumentReaderList = new DocumentReader[tempSegmentSize * 2];
			System.arraycopy(segmentSearcherList, 0, newSegmentSearcherList, 0, segmentSearcherList.length);
			System.arraycopy(documentReaderList, 0, newDocumentReaderList, 0, segmentSearcherList.length);
			
			segmentSearcherList = newSegmentSearcherList;
			documentReaderList = newDocumentReaderList;
		}
		segmentSearcherList[tempSegmentSize - 1] = new SegmentSearcher(schema, newSegmentDir, baseDocNo, docCount, revision);
		documentReaderList[tempSegmentSize - 1] = new DocumentReader(schema, newSegmentDir, baseDocNo);
		
		
		/*
		 * apply new delete.set.# to each segment
		 * */
		for (int i = 0; i < segmentSize; i++) {
			segmentSearcherList[i].setDeleteSet(deleteSetList[i]);
//			logger.debug("## setDeleteSet searcher-"+i);
//			logger.debug(deleteSetList[i]);
//			logger.debug("segmentSearcherList["+i+"] "+segmentSearcherList[i].hashCode()+" has del.set = "+deleteSetList[i].hashCode());
		}
		
		//notify new segment
		segmentSize = tempSegmentSize;

		collectionInfoFile.save();
		
		return updateAndDeleteSize;
		
	}

	
	private BitSet[] addEachSegmentDeleteSet(File newSegmentDir, int revision, DeleteIdSet deleteSet, int[] updateAndDeleteSize) throws IOException{
		File prevSegmentDir = segmentInfoList[segmentSize - 1].getSegmentDir();
		int prevRevision = segmentInfoList[segmentSize - 1].getLastRevision();
		
		/*
		 * copy delete.set.# files to current segment directory.
		 */
		for (int i = 0; i < segmentSize; i++) {
			File f1 = null;
			if(i < segmentSize - 1)
				f1 = new File(IRFileName.getRevisionDir(prevSegmentDir, prevRevision), IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
			else
				f1 = new File(IRFileName.getRevisionDir(prevSegmentDir, prevRevision), IRFileName.docDeleteSet);
			
			File f2 = new File(IRFileName.getRevisionDir(newSegmentDir, revision), IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
			logger.debug(f1.getAbsoluteFile().toString());
			logger.debug(f2.getAbsoluteFile().toString());
			FileUtils.copyFile(f1, f2);
		}
		
		BitSet[] deleteSetList = new BitSet[segmentSize];
		for (int i = 0; i < segmentSize; i++) {
			deleteSetList[i] = new BitSet(IRFileName.getRevisionDir(newSegmentDir, revision), IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
		}
		
		PrimaryKeyIndexReader[] pkReaderList = new PrimaryKeyIndexReader[segmentSize];
		
		for (int i = 0; i < segmentSize; i++) {
			SegmentInfo segmentInfo = segmentInfoList[i];
			File segmentDir = segmentInfo.getSegmentDir();
			int revision0 = segmentInfo.getLastRevision();
			pkReaderList[i] = new PrimaryKeyIndexReader(IRFileName.getRevisionDir(segmentDir,revision0), IRFileName.primaryKeyMap);
		}
		
		PrimaryKeyIndexBulkReader pkBulkReader = null;
		
		pkBulkReader = new PrimaryKeyIndexBulkReader(IRFileName.getRevisionDir(newSegmentDir,revision), IRFileName.primaryKeyMap);
		
		BytesBuffer buf = new BytesBuffer(1024);
		//새로 추가된 pk가 이전 세그먼트에 존재하면 update된 것이다.
		while(pkBulkReader.next(buf) != -1){
			//backward matching
			for (int i = segmentSize - 1; i >= 0; i--) {
				int localDocNo = pkReaderList[i].get(buf.bytes, buf.offset, buf.length);
//				logger.debug("check "+new String(buf.array, 0, buf.limit));
				if(localDocNo != -1){
					//add delete list
					deleteSetList[i].set(localDocNo);
					updateAndDeleteSize[0]++;//updateSize 증가
					//if found later pkmap, earlier pkmap already deleted the same pk.
					break;
				}
			}
			
			buf.clear();
		}
		
		pkBulkReader.close();
		
		
		PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		List<PkRefSetting> pkRefSettingList = primaryKeySetting.getFieldList();
		int pkSize = pkRefSettingList.size();
		FieldSetting[] pkFieldSettingList = new FieldSetting[pkSize];

		int pkByteSize = 0;
		for (int i = 0; i < pkSize; i++) {
			String fieldId = pkRefSettingList.get(i).getRef();
			pkFieldSettingList[i] = schema.getFieldSetting(fieldId);
			pkByteSize += pkFieldSettingList[i].getByteSize();
		}
		BytesDataOutput pkOutput = new BytesDataOutput(pkByteSize);
		
		
		//apply delete set.
//		FieldSetting primaryFieldSetting = schema.getFieldSettingList().get(schema.getIndexID());
//		FastByteBuffer idBuf = new FastByteBuffer(primaryFieldSetting.getByteSize());
		if(deleteSet != null){
			Iterator<MultiKeyEntry> iterator = deleteSet.iterator();
			while(iterator.hasNext()){
				
				MultiKeyEntry ids = iterator.next();
				logger.debug("--- delete id = {}", ids);
				pkOutput.reset();
				
				assert(deleteSet.size() == pkSize) : "deleteSet.size()와 pkSize가 일치하지 않음."+deleteSet.size()+", "+pkSize;
				//multivalue는 불가능.
				for (int i = 0; i < pkSize; i++) {
					String idString = ids.getKey(i);
					Field field = pkFieldSettingList[i].createPrimaryKeyField(idString);
					field.writeFixedDataTo(pkOutput);
				}
				BytesRef bytesRef = pkOutput.bytesRef();
//				Field field = primaryFieldSetting.createField(id);
//				idBuf.clear();
//				field.getRealBytes(idBuf);
//				idBuf.flip();
				
				//backward matching
				for (int i = segmentSize - 1; i >= 0; i--) {
					//CHECK 2011-7-4 song
//					int localDocNo = pkReaderList[i].get(idBuf.array, idBuf.pos(), idBuf.limit());
					int localDocNo = pkReaderList[i].get(bytesRef.bytes, bytesRef.offset, bytesRef.length);
					if(localDocNo != -1){
						//add delete list
						logger.debug("--- delete = "+localDocNo+" at " + i);
						deleteSetList[i].set(localDocNo);
						updateAndDeleteSize[1]++;//deleteSize 증가.
						break;
					}
				}
				
			}
		}
		
		
		for (int i = 0; i < segmentSize; i++) {
			pkReaderList[i].close();
			deleteSetList[i].save();
			logger.debug("New delete.set saved. "+deleteSetList[i]);
		}
		return deleteSetList;
	}
	
	/**
	 * 증분색인용 세그먼트 추가메서드. 기존 세그먼트에 추가문서를 append한다.
	 * 증분색인시 세그먼트를 새로 분리해야하는경우는 이 메서드가 아닌 addSegment가 사용된다.
	 * 
	 * # 증분색인시 appendSegment에는 두가지 경우가 있다.
	 * 1. 실제 문서가 추가되어  새로운 리비전 디렉토리가 append 되는경우. isCreated = true
	 * => 이전 리비전에서 삭제문서를 가져와 처리필요.
	 *
	 * 2. 추가문서없고, 삭제문서만 존재시 isCreated = false
	 * => 기존의 리비전디렉토리를 그대로 사용한다. 이전 리비전에서 삭제파일 가져오는 것은 중복task. 현 삭제리스트적용만 필요하다.
	 * 
	 * @param isCreated 새로만들어진 세그먼트인지 여부.
	 * */
	public int[] appendSegment(int segmentNumber, File segmentDir, DeleteIdSet deleteSet, boolean isCreated) throws IOException, IRException {
		logger.debug("appendSegment segment={}, isCreated={}",segmentNumber, isCreated);
//		File segmentDir = new File(IRSettings.getSegmentPath(collectionName, dataSequenceFile.getSequence(), segmentNumber));
		SegmentInfo modifiedSegmentInfo = new SegmentInfo(segmentNumber, segmentDir);
		int baseDocNo = modifiedSegmentInfo.getBaseDocNo();
		int docCount = modifiedSegmentInfo.getDocCount();
		int revision = modifiedSegmentInfo.getLastRevision();
		logger.debug("^^^ Append Segment ={}, segBaseDocNo={}, segDocCount={}, rev={}",new Object[]{segmentNumber,baseDocNo, docCount,revision});
		
//		SegmentSearcher appendedSegmentSearcher = new SegmentSearcher(schema, segmentDir, baseDocNo, revision);
//		DocumentReader appendedDocumentReader = new DocumentReader(schema, segmentDir, baseDocNo);
		
		BitSet[] deleteSetList = null;
		int[] updateAndDeleteSize = {0, 0};
		if(segmentSize > 0)
			deleteSetList = appendEachSegmentDeleteSet(segmentDir, revision ,deleteSet, updateAndDeleteSize, isCreated);
		
		//update segment info
		collectionInfoFile.overrideLastSegment(modifiedSegmentInfo);
		
		//update segmentInfoList
		segmentInfoList = collectionInfoFile.getSegmentInfoList();
		//close previous searcher
		segmentSearcherList[segmentSize - 1].close();
		documentReaderList[segmentSize - 1].close();
		//override new searcher
		segmentSearcherList[segmentSize - 1] = new SegmentSearcher(schema, segmentDir, baseDocNo, docCount, revision);
		documentReaderList[segmentSize - 1] = new DocumentReader(schema, segmentDir, baseDocNo);
		
		/*
		 * apply new delete.set.# to each segment
		 * */
		for (int i = 0; i < segmentSize - 1; i++) {
			segmentSearcherList[i].setDeleteSet(deleteSetList[i]);
		}
		
		collectionInfoFile.save();
		
//		int size = IRSettings.getConfig().getInt("segment.revision.backup.size");
		int size = 3; //리비전은 디폴드 3개까지만 남겨둔다.
		//delete old revisions exceed backup count
		if(size != -1 && revision > size){
			int delRev = revision - size - 1;
			File newRevisionDir = IRFileName.getRevisionDir(segmentDir, delRev);
			FileUtils.deleteDirectory(newRevisionDir);
		}
		
		return updateAndDeleteSize;
	}
	
	
	private BitSet[] appendEachSegmentDeleteSet(File appendSegmentDir, int revision, DeleteIdSet deleteSet, int[] updateAndDeleteSize, boolean isCreated) throws IOException{
		logger.debug("appendEachSegmentDeleteSet >> rev={}, deleteset={}, iscreate={}", new Object[]{revision, deleteSet.size(), isCreated});
		File revisionDir = IRFileName.getRevisionDir(appendSegmentDir, revision);
		//
		//새로추가된 세그먼트내 리비전일 경우에는 이전 세그먼트의 삭제리스트가 없으므로, 복사해와야한다.
		//
		if(isCreated){
			//
			// 이전 리비전이 존재할 경우에만 수행한다.
			//
			if(revision > 0){
				File prevRevisionDir = IRFileName.getRevisionDir(appendSegmentDir, revision - 1);
			
				/*
				 * 이전 리비전 디렉토리에서 세그먼트별 삭제리스트를 복사해온다. delete.# 파일임.
				 * copy delete.set.# files to current revision directory.
				 */
				for (int i = 0; i < segmentSize - 1; i++) {
					File f1 = new File(prevRevisionDir, IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
					FileUtils.copyFileToDirectory(f1, revisionDir);
					logger.debug("COPY {} TO {}", f1.getPath(), revisionDir.getPath());
				}
			
				//
				//이전 revision의 pk와 현재 revision의 pk를 머징하고 중복된 문서번호는 deleteSet에 넣는다.
				//
				int indexInterval = indexConfig.getPkTermInterval();//IRSettings.getConfig().getInt("pk.term.interval");
				BitSet revDeleteSet = new BitSet(revisionDir, IRFileName.docDeleteSet);
				File tempPkFile = new File(revisionDir, IRFileName.getTempFileName(IRFileName.primaryKeyMap));
				File newPkFile = new File(revisionDir, IRFileName.primaryKeyMap);
				if(tempPkFile.exists()){
					logger.debug("MERGE PK {} AND {} => {}", new Object[]{prevRevisionDir.getPath(), tempPkFile.getPath(), newPkFile.getPath()});
					//동일세그먼트내에서 이전 rev와 새 rev사이의 중복문서가 발견횟수를 증가시킨다.
					updateAndDeleteSize[0] += new PrimaryKeyIndexMerger().merge(prevRevisionDir, tempPkFile, newPkFile, indexInterval, revDeleteSet); 
					logger.debug("UPDATE SIZE after merge ="+updateAndDeleteSize[0] );
					revDeleteSet.save();
					//temp PK .index파일은 pk파일을 만들때 생성된 것으로, temp PK용도는 bulk용도이므로 .index파일이 필요없다.
					//그러므로 삭제!!
					File tempPkIndexFile = new File(revisionDir, IRFileName.getTempFileName(IRFileName.primaryKeyMap)+".index");
					tempPkIndexFile.delete();
				}
			}
		}
		
		
		//현재 segment의 이전 rev와 새 rev의 중복문서에 대한 delete처리는 위에서 완료된 상태이므로,
		//이제는 세그먼트간 delete처리만 수행한다.
		BitSet[] deleteSetList = new BitSet[segmentSize - 1];
		for (int i = 0; i < segmentSize - 1; i++) {
			deleteSetList[i] = new BitSet(revisionDir, IRFileName.getSuffixFileName(IRFileName.docDeleteSet, Integer.toString(i)));
		}
		
		PrimaryKeyIndexReader[] pkList = new PrimaryKeyIndexReader[segmentSize - 1];
		
		for (int i = 0; i < segmentSize - 1; i++) {
			SegmentInfo segmentInfo = segmentInfoList[i];
			File segmentDir = segmentInfo.getSegmentDir();
			int revision0 = segmentInfo.getLastRevision();
			pkList[i] = new PrimaryKeyIndexReader(IRFileName.getRevisionDir(segmentDir,revision0), IRFileName.primaryKeyMap);
		}
		
		//only use added primary key. prev revision's pk is already applied.
		//read pkmap file from temp pk file (pk.map.temp)
		File tempPkFile = new File(revisionDir, IRFileName.getTempFileName(IRFileName.primaryKeyMap));
		if(tempPkFile.exists()){
			PrimaryKeyIndexBulkReader pkBulkReader = new PrimaryKeyIndexBulkReader(tempPkFile);
			//현재 추가할 세그먼트의  pk들이 이전 세그먼트에 존재하면 delete처리를 한다.
			//i번째 segment의 삭제리스트는 i번째 deleteSetList에 추가하도록 한다.
			BytesBuffer buf = new BytesBuffer(1024);
			while(pkBulkReader.next(buf) != -1){
				//backward matching
				for (int i = segmentSize - 2; i >= 0; i--) {
					int localDocNo = pkList[i].get(buf.bytes, buf.offset, buf.length);
					if(localDocNo != -1){
						//add delete doc no
						if(!deleteSetList[i].isSet(localDocNo)){
							deleteSetList[i].set(localDocNo);
							updateAndDeleteSize[0]++;
						}
						//발견되었으면 그 이전 세그먼트는 확인해볼 필요가없다.
						//이미 이전에 append될때 deleteSet에 추가되었을 것이기 때문이다.
						break;
					}
				}
				
				buf.clear();
			}
			pkBulkReader.close();
			
			//delete pk.map.temp
			tempPkFile.delete();
		}
		
		//삭제요청된 문서를 지워준다.
		//현재 세그먼트에서 pk가 발견되지 않으면 이전 세그먼트를 순차적으로 뒤져서 삭제표시해준다.
//		FieldSetting primaryFieldSetting = schema.getFieldSettingList().get(schema.getIndexID());
//		FastByteBuffer idBuf = new FastByteBuffer(primaryFieldSetting.getByteSize());
		
		
		PrimaryKeySetting primaryKeySetting = schema.schemaSetting().getPrimaryKeySetting();
		List<PkRefSetting> pkRefSettingList = primaryKeySetting.getFieldList();
		int pkSize = pkRefSettingList.size();
		FieldSetting[] pkFieldSettingList = new FieldSetting[pkSize];

		int pkByteSize = 0;
		for (int i = 0; i < pkSize; i++) {
			String fieldId = pkRefSettingList.get(i).getRef();
			pkFieldSettingList[i] = schema.getFieldSetting(fieldId);
			pkByteSize += pkFieldSettingList[i].getByteSize();
		}
		BytesDataOutput pkOutput = new BytesDataOutput(pkByteSize);
		
		
		if(deleteSet != null){
			//현재 pkmap부터 과거 pkmap 순으로 읽어가면서 삭제문서를 체크한다.
			PrimaryKeyIndexReader currentPkReader = new PrimaryKeyIndexReader(revisionDir, IRFileName.primaryKeyMap);
			BitSet currentDeleteSet = new BitSet(revisionDir, IRFileName.docDeleteSet);
			Iterator<MultiKeyEntry> iterator = deleteSet.iterator();
			while(iterator.hasNext()){
				MultiKeyEntry ids = iterator.next();
				logger.debug("--- delete id = {}", ids);
				pkOutput.reset();
				
				assert(deleteSet.size() == pkSize) : "deleteSet.size()와 pkSize가 일치하지 않음."+deleteSet.size()+", "+pkSize;
				//multivalue는 불가능.
				for (int i = 0; i < pkSize; i++) {
					String idString = ids.getKey(i);
					Field field = pkFieldSettingList[i].createPrimaryKeyField(idString);
					field.writeFixedDataTo(pkOutput);
				}
				
//				Field field = primaryFieldSetting.createField(id);
//				idBuf.clear();
//				field.getRealBytes(idBuf);
//				idBuf.flip();
				//2012-4-30 swsong
				//자신의 revision에서 삭제문서찾는 로직을 다시 활성화함. 현 리비전에는 현재세그먼트의 삭제리스트가 들어있음. 
				//증분문서와 삭제문서에 동일한 문서가 들어올 경우 수집기에서 이미 삭제리스트에서 제외되므로 삭제되지 않고 추가됨.
				//증분문서가 0건이고 삭제문서만 존재할 경우, revision은 증가하지 않으므로, 현재 revision디렉토리에서 삭제문서처리하는 로직이 꼭 필요함.
				BytesRef bytesRef = pkOutput.bytesRef();
//				int localDocNo = currentPkReader.get(idBuf.array, idBuf.pos(), idBuf.limit());
				int localDocNo = currentPkReader.get(bytesRef.bytes, bytesRef.offset, bytesRef.length);
				if(localDocNo != -1){
					//add delete list
					logger.debug("--- delete localDocNo = {} at current segment", localDocNo);
					currentDeleteSet.set(localDocNo);
					updateAndDeleteSize[1]++;//deleteSize 증가.
					//현재 세그먼트에서 삭제된 pk는, 이전 세그먼트에서 찾아볼필요가 없다.
					//왜냐하면 현재세그먼트와 동일한 pk가 이전 세그먼트에 존재한다면 해당 pk는 이미 삭제리스트에 존재할것이기 때문이다.
					//기존에는 break로 되어있어서 처음것만 삭제되고, while문을 빠져나가는 버그가 존재했음. 2012-02-10 swsong
					continue;
				}
				//backward matching
				for (int i = segmentSize - 2; i >= 0; i--) {
//					localDocNo = pkList[i].get(idBuf.array, idBuf.pos(), idBuf.limit());
					localDocNo = pkList[i].get(bytesRef.bytes, bytesRef.offset, bytesRef.length);
					if(localDocNo != -1){
						//add delete list
						logger.debug("--- delete localDocNo = {} at segment {}", localDocNo, i);
						deleteSetList[i].set(localDocNo);
						updateAndDeleteSize[1]++;//deleteSize 증가.
						//if found later pkmap, earlier pkmap already deleted the same pk.
						break;
					}
				}
				
			}//while
			currentPkReader.close();
			currentDeleteSet.save();
		}
		
		
		
		for (int i = 0; i < segmentSize - 1; i++) {
			pkList[i].close();
			deleteSetList[i].save();
		}
		
		return deleteSetList;
	}

	public boolean restore(int segmentNumber, int revisionTo) throws IOException, IRException{
		if(segmentNumber > segmentSize - 1){
			logger.error("Requested segment number is greater than last segment. request = "+segmentNumber+", last = "+(segmentSize - 1));
			return false;
		}
		File dataDir = dataSequenceFile.getDataDirFile();
		File segmentDir = new File(dataDir, Integer.toString(segmentNumber));//new File(IRSettings.getSegmentPath(collectionName, dataSequenceFile.getSequence(), segmentNumber));
		SegmentInfo segmentInfo = new SegmentInfo(segmentNumber, segmentDir);
		int lastRevision = segmentInfo.getLastRevision();
//		logger.debug("^^^ Segment "+segmentNumber+" docCount = "+(segmentInfo.getDocCount()));
		if(revisionTo > lastRevision){
			logger.error("Requested revision number is greater than last revision. request = "+revisionTo+", last = "+lastRevision);
			return false;
		}
		
		if(segmentNumber == segmentSize - 1 && revisionTo == lastRevision){
			logger.error("The same segment and revision as current's are requested.");
			//do nothing
			return false;
		}
		
		//close previous segments
		for (int i = 0; i < segmentSize; i++) {
			segmentSearcherList[i].close();
			documentReaderList[i].close();
		}
		
		//copy segment.info from revision dir to current segment dir.
		File newRevisionDir = IRFileName.getRevisionDir(segmentDir, revisionTo);
		FileUtils.copyFileToDirectory(new File(newRevisionDir, IRFileName.segmentInfoFile), segmentDir);
		//reload segment.info file
		segmentInfo = new SegmentInfo(segmentNumber, segmentDir, true); //skip verify when opening
		int docCount = segmentInfo.getDocCount();
		
		//doc.position
		IndexOutput fileOutput = new BufferedFileOutput(segmentDir, IRFileName.docPosition, true);
		logger.debug("restore = "+segmentInfo.docPositionFilesSize+", "+fileOutput);
		fileOutput.setLength(segmentInfo.docPositionFilesSize);
		fileOutput.close();
		
		DocumentRestorer documentRestorer = new DocumentRestorer(segmentDir);
		documentRestorer.setSize(docCount);
		
		//group.data
		fileOutput = new BufferedFileOutput(segmentDir, IRFileName.groupDataFile, true);
		logger.debug("restore = "+segmentInfo.groupDataFileSize+", "+fileOutput);
		fileOutput.setLength(segmentInfo.groupDataFileSize);
		fileOutput.close();
		//group.key.#
		for (int i = 0; i < segmentInfo.groupKeyFileSize.length; i++) {
			fileOutput = new BufferedFileOutput(segmentDir, IRFileName.getSuffixFileName(IRFileName.groupKeyFile, Integer.toString(i)), true);
			logger.debug("restore = "+segmentInfo.groupKeyFileSize[i]+", "+fileOutput);
			fileOutput.setLength(segmentInfo.groupKeyFileSize[i]);
			fileOutput.close();
		}
//		//sort.field
//		fileOutput = new BufferedFileOutput(segmentDir, IRFileName.sortFieldFile, true);
//		logger.debug("restore = "+segmentInfo.fieldIndexFileSize+", "+fileOutput);
//		fileOutput.setLength(segmentInfo.fieldIndexFileSize);
//		fileOutput.close();
//		//filter.field
//		fileOutput = new BufferedFileOutput(segmentDir, IRFileName.filterFieldFile, true);
//		logger.debug("restore = "+segmentInfo.filterFieldFileSize+", "+fileOutput);
//		fileOutput.setLength(segmentInfo.filterFieldFileSize);
//		fileOutput.close();
		
		
		//reload segments.
		int oldSegmentSize = segmentSize;
		segmentSize = segmentNumber + 1;
		
		collectionInfoFile = new CollectionInfoFile(dataDir, segmentSize);
		segmentInfoList = collectionInfoFile.getSegmentInfoList();
		
		loadSearcherAndReader();
		
		//delete rest segment and revisions
		for (int i = segmentNumber + 1; i < oldSegmentSize; i++) {
			File dir = new File(dataDir, Integer.toString(i));
			FileUtils.deleteDirectory(dir);
		}
		
		for (int i = revisionTo + 1; i <= lastRevision; i++) {
			File revDir = IRFileName.getRevisionDir(segmentDir, i);
			FileUtils.deleteDirectory(revDir);
		}
		
		return true;
	}
	
	
	public int segmentSize(){
		return segmentSize;
	}
	
	
	public void saveDataSequenceFile() throws IRException {
		dataSequenceFile.save();		
	}

	public long getStartedTime(){
		return startedTime;
	}

}
