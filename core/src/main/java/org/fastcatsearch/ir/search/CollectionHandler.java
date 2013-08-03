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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.document.merge.PrimaryKeyIndexMerger;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.io.BytesDataOutput;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.PkRefSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.util.CollectionFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionHandler {

	private static Logger logger = LoggerFactory.getLogger(CollectionHandler.class);
	private List<SegmentReader> segmentReaderList;
	private String collectionId;
	private CollectionContext collectionContext;

	private long startedTime;
	private boolean isLoaded;

	private CollectionSearcher collectionSearcher;
	private CollectionFilePaths collectionFilePaths;

	public CollectionHandler(CollectionContext collectionContext) throws IRException, SettingException {
		this.collectionContext = collectionContext;
		this.collectionId = collectionContext.collectionId();
		this.collectionFilePaths = collectionContext.collectionFilePaths();
		segmentReaderList = new ArrayList<SegmentReader>();
	}

	public CollectionHandler load() throws IRException {
		loadSearcherAndReader();
		this.collectionSearcher = new CollectionSearcher(this);
		startedTime = System.currentTimeMillis();
		isLoaded = true;
		logger.info("Collection[{}] Loaded! {}", collectionId, collectionFilePaths.file().getAbsolutePath());
		return this;
	}

	public long getStartedTime() {
		return startedTime;
	}

	public CollectionFilePaths collectionFilePaths() {
		return collectionFilePaths;
	}

	public CollectionContext collectionContext() {
		return collectionContext;
	}

	public CollectionSearcher searcher() {
		return collectionSearcher;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	private void loadSearcherAndReader() throws IRException {

		int dataSequence = collectionContext.collectionStatus().getSequence();
		collectionContext.collectionFilePaths();

		File dataDir = collectionFilePaths.dataFile(dataSequence);
		if (!dataDir.exists()) {
			logger.info("create collection data directory [{}]", dataDir.getAbsolutePath());
			dataDir.mkdir();
		}

		logger.debug("Load CollectionHandler [{}] data >> {}", collectionId, dataDir.getAbsolutePath());

		// 색인기록이 있다면 세그먼트를 로딩한다.
		if (collectionContext.dataInfo().getSegmentInfoList() != null) {
			try {
				for (SegmentInfo segmentInfo : collectionContext.dataInfo().getSegmentInfoList()) {
					File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentInfo.getId());
					File revisionDir = collectionFilePaths.revisionFile(dataSequence, segmentInfo.getId(), segmentInfo.getRevision());
					BitSet deleteSet = new BitSet(revisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentInfo.getId()));
					segmentReaderList.add(new SegmentReader(segmentInfo, collectionContext.schema(), segmentDir, deleteSet));
				}
			} catch (IOException e) {
				throw new IRException(e);
			}
		}
	}

	public void close() throws IOException {
		for (SegmentReader segmentReader : segmentReaderList) {
			segmentReader.close();
		}
		collectionSearcher = null;
		isLoaded = false;
	}

	public String collectionId() {
		return collectionId;
	}

	public int getDataSequence() {
		return collectionContext.collectionStatus().getSequence();

	}

	public void printSegmentStatus() {
		int i = 0;
		for (SegmentReader segmentReader : segmentReaderList) {
			logger.info("SEG#{} >> {}", i++, segmentReader.segmentInfo());
		}
	}

	public SegmentReader segmentReader(int segmentNumber) {
		if (segmentReaderList.size() == 0) {
			return null;
		}

		return segmentReaderList.get(segmentNumber);

	}

	public SegmentReader getLastSegmentReader() {
		if (segmentReaderList.size() == 0) {
			return null;
		}

		// get Last segment Number
		return segmentReaderList.get(segmentReaderList.size() - 1);
	}

	public SegmentSearcher segmentSearcher(int segmentNumber) {
		if (segmentReaderList.size() == 0) {
			return null;
		}

		return segmentReaderList.get(segmentNumber).segmentSearcher();
	}

	public Schema schema() {
		return collectionContext.schema();
	}

	/**
	 * 색인시 세그먼트를 하나 추가할 경우 호출함. 전체색인시는 세그먼트 0번부터 생성하므로 무조건 이 메서드를 호출하고 증분색인시는 세그먼트내 문서갯수가 많아서 새로운 세그먼트를 만들어야 할 경우 호출된다.
	 * 
	 * full index : segmentNumber = 0 add index : segmentNumber > 0
	 * */
	// 새 세그먼트 추가.
	private void addSegment(SegmentInfo newSegmentInfo, File newSegmentDir, DeleteIdSet deleteSet) throws IOException, IRException {
		logger.debug("Add segment {}", newSegmentInfo);
		BitSet[] deleteSetList = null;
		int segmentSize = segmentReaderList.size();

		if (segmentSize > 0) {
			File targetRevisionDir = new File(newSegmentDir, Integer.toString(newSegmentInfo.getRevision()));
			copyPrevSegmentsDeleteSetToSegment(targetRevisionDir);

			String pkFilename = IndexFileNames.primaryKeyMap;
			deleteSetList = applySegmentDeleteSet(targetRevisionDir, pkFilename, deleteSet, segmentSize);
		}

		/*
		 * apply new delete.set.# to each segment
		 */
		for (int i = 0; i < segmentReaderList.size(); i++) {
			segmentReaderList.get(i).setDeleteSet(deleteSetList[i]);
		}
		// 새로생성된 세그먼트는 로딩하여 리스트에 추가해준다.
		addSegmentReader(new SegmentReader(newSegmentInfo, collectionContext.schema(), newSegmentDir));
	}
	
	// 1. 타 세그먼트에 삭제 문서를 적용한다.
	// 2. 해당 segment reader 재로딩 및 대체하기. 
	public void updateCollection(SegmentInfo segmentInfo, File segmentDir, DeleteIdSet deleteSet) throws IOException, IRException {

		//TODO segmentInfo null일경우, 즉 추가문서없이 deleteSet만 요청된 경우 처리가 필요하다.
		
		/*
		 * 1. 타 세그먼트에 삭제 문서를 적용한다.
		 */
		SegmentReader lastSegmentReader = getSegmentReader(segmentInfo);
		if(lastSegmentReader == null){
			//새로추가된 세그먼트이다.
			addSegment(segmentInfo, segmentDir, deleteSet);
			return;
		}
		
		if(lastSegmentReader.segmentInfo().getIntId() != segmentReaderList.size() - 1){
			//마지막 reader가 아니므로 업데이트불가. revision 생성은 마지막 segment에 수행되므로 segmentid가 다르다면 문제가 발생한것임. 
			throw new IRException("마지막 segment만 update가능합니다.");
		}
		
		logger.debug("Update segment {}", segmentInfo);
		// 마지막 reader로 작업한다.
		int segmentSize = segmentReaderList.size() - 1;
		BitSet[] deleteSetList = null;
		File prevRevisionDir = lastSegmentReader.revisionDir();

		//자신을 제외한 reader가 존재하는지 확인.
		if (segmentSize > 0) {
			File targetRevisionDir = new File(segmentDir, Integer.toString(segmentInfo.getRevision()));
			// 이전 세그먼트의 delete.set을 복사해온다.
			copyPrevSegmentsDeleteSetToRevision(targetRevisionDir);
			// 이전 리비전과의 pk를 머지하고, delete.set을 업데이트한다.
			mergeRevisionPrimaryKeyFilesAndUpdateDeleteSet(prevRevisionDir, targetRevisionDir);

			String pkFilename = IndexFileNames.getTempFileName(IndexFileNames.primaryKeyMap);
			deleteSetList = applySegmentDeleteSet(targetRevisionDir, pkFilename, deleteSet, segmentSize);
			
			for (int i = 0; i < segmentSize; i++) {
				segmentReaderList.get(i).setDeleteSet(deleteSetList[i]);
			}
		}

		/*
		 * 2. 해당 segment reader 재로딩 및 대체하기. 
		 */
		//새 revison을 읽는 segmentReader를 만들어서 기존것과 바꾼다.
		updateSegmentReader(new SegmentReader(segmentInfo, collectionContext.schema(), segmentDir), lastSegmentReader);
		//기존 reader는 닫는다.
		lastSegmentReader.close();
	}
	
	private void addSegmentReader(SegmentReader segmentReader){
		segmentReaderList.add(segmentReader);
		//info.xml 파일업데이트용.
		collectionContext.addSegmentInfo(segmentReader.segmentInfo());
	}
	private void updateSegmentReader(SegmentReader segmentReader, SegmentReader prevSegmentReader){
		segmentReaderList.remove(prevSegmentReader);
		segmentReaderList.add(segmentReader);
		//info.xml 파일업데이트용.
		collectionContext.updateSegmentInfo(segmentReader.segmentInfo());
	}
	private SegmentReader getSegmentReader(SegmentInfo segmentInfo){
		for (SegmentReader segmentReader : segmentReaderList) {
			if(segmentReader.segmentInfo().equals(segmentInfo)){
				return segmentReader;
			}
		}
		return null;
	}

	private BitSet[] applySegmentDeleteSet(File targetRevisionDir, String pkFilename, DeleteIdSet deleteSet, int segmentSize) throws IOException{
		//첨자 i는 세그먼터 id와 일치해야한다.
		BitSet[] deleteSetList = new BitSet[segmentSize];
		PrimaryKeyIndexReader[] pkReaderList = new PrimaryKeyIndexReader[segmentSize];
		for (int i = 0; i < segmentSize; i++) {
			SegmentReader segmentReader = segmentReaderList.get(i);
			String segmentId = segmentReader.segmentInfo().getId();
			deleteSetList[i] = new BitSet(targetRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			pkReaderList[i] = new PrimaryKeyIndexReader(segmentReader.revisionDir(), IndexFileNames.primaryKeyMap);
		}
		
		File pkFile = new File(targetRevisionDir, pkFilename); 

		// 1. applyPrimaryKeyToPrevSegments
		int updateDocumentCount = applyPrimaryKeyToPrevSegments(pkFile, pkReaderList, deleteSetList);
		// /TODO delete tmp pk file
		// 2. applyDeleteIdSetToPrevSegments
		int deleteDocumentCount = applyDeleteIdSetToPrevSegments(targetRevisionDir, deleteSet, pkReaderList, deleteSetList);

		for (int i = 0; i < segmentSize; i++) {
			pkReaderList[i].close();
			deleteSetList[i].save();
			logger.debug("New delete.set saved. set={}", deleteSetList[i]);
		}
		
		return deleteSetList;
	}
	
	private void copyPrevSegmentsDeleteSetToSegment(File targetRevisionDir) throws IOException {
		SegmentReader lastSegmentReader = segmentReaderList.get(segmentReaderList.size() - 1);
		File lastSegmentRevisionDir = lastSegmentReader.revisionDir();// new File(lastSegmentDir,
																		// Integer.toString(lastSegmentRevision));
		int segmentSize = segmentReaderList.size();
		for (int i = 0; i < segmentSize; i++) {
			// File f1 = null;
			String segmentId = segmentReaderList.get(i).segmentInfo().getId();
			// if(i < segmentSize - 1){
			// f1 = new File(lastSegmentRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			// } else {
			// //마지막 세그먼트는 자신의 delete.set에 아이디 suffix를 안붙였으므로, suffix없이 읽어온다.
			// f1 = new File(lastSegmentRevisionDir, IndexFileNames.docDeleteSet);
			// }
			File f1 = new File(lastSegmentRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			File f2 = new File(targetRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			logger.debug("copy delete.set {} => {}", f1.getAbsolutePath(), f2.getAbsolutePath());
			FileUtils.copyFile(f1, f2);
		}
	}

	// 이 메소드가 호출전에는 추가될 리비전이 들어있는 세그먼트reader는 제거된 상태이어야 한다.
	private void copyPrevSegmentsDeleteSetToRevision(File targetRevisionDir) throws IOException {
		SegmentReader lastSegmentReader = segmentReaderList.get(segmentReaderList.size() - 1);
		File lastSegmentRevisionDir = lastSegmentReader.revisionDir();
		int segmentSize = segmentReaderList.size();
		for (int i = 0; i < segmentSize; i++) {
			String segmentId = segmentReaderList.get(i).segmentInfo().getId();
			File f1 = new File(lastSegmentRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
			FileUtils.copyFileToDirectory(f1, targetRevisionDir);
		}

	}

	private void mergeRevisionPrimaryKeyFilesAndUpdateDeleteSet(File prevRevisionDir, File targetRevisionDir) throws IOException {
		//
		// 이전 revision의 pk와 현재 revision의 pk를 머징하고 중복된 문서번호는 deleteSet에 넣는다.
		//
		int indexInterval = collectionContext.collectionConfig().getIndexConfig().getPkTermInterval();
		BitSet revDeleteSet = new BitSet(targetRevisionDir, IndexFileNames.docDeleteSet);
		File prevRevisionPkFile = new File(prevRevisionDir, IndexFileNames.primaryKeyMap);
		File tempPkFile = new File(targetRevisionDir, IndexFileNames.getTempFileName(IndexFileNames.primaryKeyMap));
		File newPkFile = new File(targetRevisionDir, IndexFileNames.primaryKeyMap);
		if (tempPkFile.exists()) {
			logger.debug("MERGE PK {} AND {} => {}", new Object[] { prevRevisionPkFile.getPath(), tempPkFile.getPath(), newPkFile.getPath() });
			// 동일세그먼트내에서 이전 rev와 새 rev사이의 중복문서가 발견횟수를 증가시킨다.
			int updateDocumentCount = new PrimaryKeyIndexMerger().merge(prevRevisionPkFile, tempPkFile, newPkFile, indexInterval, revDeleteSet);
			logger.debug("UPDATE SIZE after merge = {}", updateDocumentCount);
			revDeleteSet.save();
			// temp PK .index파일은 pk파일을 만들때 생성된 것으로, temp PK용도는 bulk용도이므로 .index파일이 필요없다.
			// 그러므로 삭제!!
			File tempPkIndexFile = new File(targetRevisionDir, IndexFileNames.getTempFileName(IndexFileNames.primaryKeyMap) + ".index");
			tempPkIndexFile.delete();
		}
	}

	private int applyPrimaryKeyToPrevSegments(File pkFile, PrimaryKeyIndexReader[] pkReaderList, BitSet[] deleteSetList) throws IOException {

		// 이전 모든 세그먼트를 통틀어 업데이트되고 삭제된 문서수.
		int updateDocumentSize = 0; // 이번 pk와 이전 pk가 동일할 경우
		int segmentSize = segmentReaderList.size();

		// 현 pk를 bulk로 읽어들여 id 중복을 확인한다.
		PrimaryKeyIndexBulkReader pkBulkReader = new PrimaryKeyIndexBulkReader(pkFile);

		BytesBuffer buf = new BytesBuffer(1024);
		// 새로 추가된 pk가 이전 세그먼트에 존재하면 update된 것이다.
		while (pkBulkReader.next(buf) != -1) {
			// backward matching
			for (int i = segmentSize - 1; i >= 0; i--) {
				int localDocNo = pkReaderList[i].get(buf);
				// logger.debug("check "+new String(buf.array, 0, buf.limit));
				if (localDocNo != -1) {
					if (!deleteSetList[i].isSet(localDocNo)) {
						// add delete list
						deleteSetList[i].set(localDocNo);
						updateDocumentSize++;// updateSize 증가
					}
					// if found later pkmap, earlier pkmap already deleted the same pk.
					break;
				}
			}

			buf.clear();
		}

		pkBulkReader.close();

		return updateDocumentSize;
	}

	private int applyDeleteIdSetToPrevSegments(File targetRevisionDir, DeleteIdSet deleteSet, PrimaryKeyIndexReader[] pkReaderList,
			BitSet[] deleteSetList) throws IOException {

		int deleteDocumentSize = 0;

		if (deleteSet == null) {
			return deleteDocumentSize;
		}

		PrimaryKeySetting primaryKeySetting = collectionContext.schema().schemaSetting().getPrimaryKeySetting();
		List<FieldSetting> fieldSettingList = collectionContext.schema().schemaSetting().getFieldSettingList();
		List<PkRefSetting> pkRefSettingList = primaryKeySetting.getFieldList();
		int pkSize = pkRefSettingList.size();
		FieldSetting[] pkFieldSettingList = new FieldSetting[pkSize];
		assert (deleteSet.keySize() == pkSize) : "deleteSet.size()와 pkSize가 일치하지 않음." + deleteSet.size() + ", " + pkSize;

		int pkByteSize = 0;
		for (int i = 0; i < pkSize; i++) {
			String fieldId = pkRefSettingList.get(i).getRef();
			int fieldSequence = collectionContext.schema().getFieldSequence(fieldId);
			pkFieldSettingList[i] = fieldSettingList.get(fieldSequence);
			pkByteSize += pkFieldSettingList[i].getByteSize();
		}
		BytesDataOutput pkOutput = new BytesDataOutput(pkByteSize);

		/*
		 * apply delete set. 이번 색인작업을 통해 삭제가 요청된 문서들을 삭제처리한다.
		 */
		PrimaryKeyIndexReader currentPkReader = new PrimaryKeyIndexReader(targetRevisionDir, IndexFileNames.primaryKeyMap);
		BitSet currentDeleteSet = new BitSet(targetRevisionDir, IndexFileNames.docDeleteSet);
		Iterator<PrimaryKeys> iterator = deleteSet.iterator();
		while (iterator.hasNext()) {

			PrimaryKeys ids = iterator.next();
			logger.debug("--- delete id = {}", ids);
			pkOutput.reset();

			// multivalue는 불가능.
			for (int i = 0; i < pkSize; i++) {
				String idString = ids.getKey(i);

				Field field = null;
				try {
					field = pkFieldSettingList[i].createPrimaryKeyField(idString);
					field.writeFixedDataTo(pkOutput);
				} catch (FieldDataParseException e) {
					// id 값을 필드로 만드는데 실패했다면 건너뛴다.
					logger.error("ID필드를 만들수 없습니다. {}, {}", idString, e);
				}
			}
			BytesRef bytesRef = pkOutput.bytesRef();

			int localDocNo = currentPkReader.get(bytesRef);
			if (localDocNo != -1) {
				// add delete list
				logger.debug("--- delete localDocNo = {} at current segment", localDocNo);
				currentDeleteSet.set(localDocNo);
				deleteDocumentSize++;// deleteSize 증가.
				// 현재 세그먼트에서 삭제된 pk는, 이전 세그먼트에서 찾아볼필요가 없다.
				// 왜냐하면 현재세그먼트와 동일한 pk가 이전 세그먼트에 존재한다면 해당 pk는 이미 삭제리스트에 존재할것이기 때문이다.
				// 기존에는 break로 되어있어서 처음것만 삭제되고, while문을 빠져나가는 버그가 존재했음. 2012-02-10 swsong
				continue;
			}
			// backward matching
			for (int i = segmentReaderList.size() - 1; i >= 0; i--) {
				// CHECK 2011-7-4 song
				localDocNo = pkReaderList[i].get(bytesRef);
				if (localDocNo != -1) {
					if (!deleteSetList[i].isSet(localDocNo)) {
						// add delete list
						logger.debug("--- delete doc[{}] at segment[{}]", localDocNo, i);
						deleteSetList[i].set(localDocNo);
						deleteDocumentSize++;// deleteSize 증가.
					}
					break;
				}
			}

		}

		return deleteDocumentSize;
	}

	public int segmentSize() {
		if (segmentReaderList == null) {
			return 0;
		}

		return segmentReaderList.size();
	}

	// public boolean restore(int segmentNumber, int revisionTo) throws IOException, IRException{
	// if(segmentNumber > segmentSize - 1){
	// logger.error("Requested segment number is greater than last segment. request = "+segmentNumber+", last = "+(segmentSize -
	// 1));
	// return false;
	// }
	// File dataDir = dataSequenceFile.getDataDirFile();
	// File segmentDir = new File(dataDir, Integer.toString(segmentNumber));//new File(IRSettings.getSegmentPath(collectionName,
	// dataSequenceFile.getSequence(), segmentNumber));
	// SegmentInfo segmentInfo = new SegmentInfo(segmentNumber, segmentDir);
	// int lastRevision = segmentInfo.getLastRevision();
	// // logger.debug("^^^ Segment "+segmentNumber+" docCount = "+(segmentInfo.getDocCount()));
	// if(revisionTo > lastRevision){
	// logger.error("Requested revision number is greater than last revision. request = "+revisionTo+", last = "+lastRevision);
	// return false;
	// }
	//
	// if(segmentNumber == segmentSize - 1 && revisionTo == lastRevision){
	// logger.error("The same segment and revision as current's are requested.");
	// //do nothing
	// return false;
	// }
	//
	// //close previous segments
	// for (int i = 0; i < segmentSize; i++) {
	// segmentReaderList[i].close();
	// documentReaderList[i].close();
	// }
	//
	// //copy segment.info from revision dir to current segment dir.
	// File newRevisionDir = IRFileName.getRevisionDir(segmentDir, revisionTo);
	// FileUtils.copyFileToDirectory(new File(newRevisionDir, IRFileName.segmentInfoFile), segmentDir);
	// //reload segment.info file
	// segmentInfo = new SegmentInfo(segmentNumber, segmentDir, true); //skip verify when opening
	// int docCount = segmentInfo.getDocCount();
	//
	// //doc.position
	// IndexOutput fileOutput = new BufferedFileOutput(segmentDir, IRFileName.docPosition, true);
	// logger.debug("restore = "+segmentInfo.docPositionFilesSize+", "+fileOutput);
	// fileOutput.setLength(segmentInfo.docPositionFilesSize);
	// fileOutput.close();
	//
	// DocumentRestorer documentRestorer = new DocumentRestorer(segmentDir);
	// documentRestorer.setSize(docCount);
	//
	// //group.data
	// fileOutput = new BufferedFileOutput(segmentDir, IRFileName.groupDataFile, true);
	// logger.debug("restore = "+segmentInfo.groupDataFileSize+", "+fileOutput);
	// fileOutput.setLength(segmentInfo.groupDataFileSize);
	// fileOutput.close();
	// //group.key.#
	// for (int i = 0; i < segmentInfo.groupKeyFileSize.length; i++) {
	// fileOutput = new BufferedFileOutput(segmentDir, IRFileName.getSuffixFileName(IRFileName.groupKeyFile, Integer.toString(i)),
	// true);
	// logger.debug("restore = "+segmentInfo.groupKeyFileSize[i]+", "+fileOutput);
	// fileOutput.setLength(segmentInfo.groupKeyFileSize[i]);
	// fileOutput.close();
	// }
	// // //sort.field
	// // fileOutput = new BufferedFileOutput(segmentDir, IRFileName.sortFieldFile, true);
	// // logger.debug("restore = "+segmentInfo.fieldIndexFileSize+", "+fileOutput);
	// // fileOutput.setLength(segmentInfo.fieldIndexFileSize);
	// // fileOutput.close();
	// // //filter.field
	// // fileOutput = new BufferedFileOutput(segmentDir, IRFileName.filterFieldFile, true);
	// // logger.debug("restore = "+segmentInfo.filterFieldFileSize+", "+fileOutput);
	// // fileOutput.setLength(segmentInfo.filterFieldFileSize);
	// // fileOutput.close();
	//
	//
	// //reload segments.
	// int oldSegmentSize = segmentSize;
	// segmentSize = segmentNumber + 1;
	//
	// collectionInfoFile = new CollectionInfoFile(dataDir, segmentSize);
	// segmentInfoList = collectionInfoFile.getSegmentInfoList();
	//
	// loadSearcherAndReader();
	//
	// //delete rest segment and revisions
	// for (int i = segmentNumber + 1; i < oldSegmentSize; i++) {
	// File dir = new File(dataDir, Integer.toString(i));
	// FileUtils.deleteDirectory(dir);
	// }
	//
	// for (int i = revisionTo + 1; i <= lastRevision; i++) {
	// File revDir = IRFileName.getRevisionDir(segmentDir, i);
	// FileUtils.deleteDirectory(revDir);
	// }
	//
	// return true;
	// }

}
