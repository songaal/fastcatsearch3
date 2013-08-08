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
		List<SegmentInfo> segmentInfoList = collectionContext.dataInfo().getSegmentInfoList();
		int segmentSize = segmentInfoList.size();
		if (segmentSize > 0) {
			//FIXME 반드시 0,1,2...차례대로 list에 존재해야한다. deleteset을 적용해야하기때문에..
			SegmentInfo lastSegmentInfo = segmentInfoList.get(segmentSize - 1);
			File lastRevisionDir = collectionFilePaths.revisionFile(dataSequence, lastSegmentInfo.getId(), lastSegmentInfo.getRevision());
			try {
				for (SegmentInfo segmentInfo : collectionContext.dataInfo().getSegmentInfoList()) {
					File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentInfo.getId());
					//삭제문서는 마지막 세그먼트의 마지막 리비전에 최신 업데이트 파일이 있으므로, 그것을 로딩한다.
					BitSet deleteSet = new BitSet(lastRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentInfo.getId()));
					segmentReaderList.add(new SegmentReader(segmentInfo, collectionContext.schema(), segmentDir, deleteSet));
					logger.debug("{}", segmentInfo);
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

	//segment reader 추가. collectionContext에도 자동으로 segmentInfo추가됨.
	private void addSegmentReader(SegmentReader segmentReader) {
		segmentReaderList.add(segmentReader);
		// info.xml 파일업데이트용.
		collectionContext.addSegmentInfo(segmentReader.segmentInfo());
	}

	//segment reader 교체.
	private void updateSegmentReader(SegmentReader segmentReader, SegmentReader prevSegmentReader) {
		segmentReaderList.remove(prevSegmentReader);
		segmentReaderList.add(segmentReader);
		// info.xml 파일업데이트용.
		collectionContext.updateSegmentInfo(segmentReader.segmentInfo());
	}

	//SegmentReader 찾기.
	private SegmentReader getSegmentReader(String segmentId) {
		for (SegmentReader segmentReader : segmentReaderList) {
			if (segmentReader.segmentInfo().getId().equals(segmentId)) {
				return segmentReader;
			}
		}
		return null;
	}
	
	/**
	 * 증분색인후 호출하는 메소드이다. 
	 * 1. 타 세그먼트에 삭제 문서를 적용한다.
	 * 2. 해당 segment reader 재로딩 및 대체하기.
	 * */
	public void updateCollection(CollectionContext collectionContext, SegmentInfo segmentInfo, File segmentDir, DeleteIdSet deleteSet) throws IOException, IRException {

		this.collectionContext = collectionContext;
		
		// TODO segmentInfo null일경우, 즉 추가문서없이 deleteSet만 요청된 경우 처리가 필요하다.
		String segmentId = segmentInfo.getId();
		SegmentReader oldSegmentReader = getSegmentReader(segmentId);
		//동일한 id의 세그먼트가 없을경우는 증분색인시 segment가 추가된경우이다.
		if (oldSegmentReader == null) {
			/*
			 * 증분색인시 세그먼트 하나가 추가된 경우. 
			 */
			// 전체색인시는 세그먼트 0번부터 생성. 증분색인시는 세그먼트내 문서갯수가 많아서 새로운 세그먼트를 만들어야 할 경우.
			logger.debug("Add segment {}", segmentInfo);
			applyDeleteSet(segmentInfo, segmentDir, segmentReaderList, IndexFileNames.primaryKeyMap, deleteSet);
			// 새로생성된 세그먼트는 로딩하여 리스트에 추가해준다.
			addSegmentReader(new SegmentReader(segmentInfo, collectionContext.schema(), segmentDir));
		} else {
			/*
			 * 리비전이 증가한경우.
			 */
			if (oldSegmentReader.segmentInfo().getIntId() != segmentReaderList.size() - 1) {
				// 마지막 reader가 아니므로 업데이트불가. revision 생성은 마지막 segment에 수행되므로 segmentid가 다르다면 문제가 발생한것임.
				throw new IRException("마지막 segment만 update가능합니다.");
			}

			logger.debug("Update segment {}", segmentInfo);
			String pkFilename = IndexFileNames.getTempFileName(IndexFileNames.primaryKeyMap);
			//마지막 reader를 제외한 리스트를 생성하여 delete.set을 update한다.
			List<SegmentReader> prevSegmentReaderList = segmentReaderList.subList(0, segmentReaderList.size() - 1);

			applyDeleteSet(segmentInfo, segmentDir, prevSegmentReaderList, pkFilename, deleteSet);
			
			File prevRevisionDir = new File(segmentDir, Integer.toString(oldSegmentReader.segmentInfo().getRevision()));
			File targetRevisionDir = new File(segmentDir, Integer.toString(segmentInfo.getRevision()));

			mergeRevisionPrimaryKeyFilesAndUpdateDeleteSet(segmentId, prevRevisionDir, targetRevisionDir);

			//해당 segment reader 재로딩 및 대체하기.
			// 새 revison을 읽는 segmentReader를 만들어서 기존것과 바꾼다.
			updateSegmentReader(new SegmentReader(segmentInfo, collectionContext.schema(), segmentDir), oldSegmentReader);
			// 기존 reader는 닫는다.
			oldSegmentReader.close();
		}
	}

	// 이전 세그먼트가 존재하면 delete.set을 업데이트하여 segment reader 에 적용시켜준다.
	// 최근 색인작업으로 추가된 newSegmentInfo 세그먼트의 문서는 최신이므로 delete.set을 따로 적용할 필요가없다.
	private BitSet[] applyDeleteSet(SegmentInfo segmentInfo, File segmentDir, List<SegmentReader> prevSegmentReaderList, String pkFilename, DeleteIdSet deleteSet)
			throws IOException {
		String segmentId = segmentInfo.getId();
		int prevSegmentSize = prevSegmentReaderList.size();
		BitSet[] deleteSetList = null;

		if (prevSegmentSize > 0) {
			File targetRevisionDir = new File(segmentDir, Integer.toString(segmentInfo.getRevision()));

			SegmentReader lastSegmentReader = segmentReaderList.get(prevSegmentSize - 1);
			File lastSegmentRevisionDir = lastSegmentReader.revisionDir();

			// 이전 revision에서 delete.set.#들을 복사해온다.
			copyDeleteSet(prevSegmentReaderList, lastSegmentRevisionDir, targetRevisionDir);
			
			// 복사해온 delete.set.#들을 기존 pk들을 확인하면서 update해준다. 파일에 write까지 수행됨.
			//예를들어 현재 segment가 5이면 수정파일은 delete.set.0,1,2,3,4 이다.  
			deleteSetList = updateDeleteSetWithPrevSegments(segmentId, targetRevisionDir, pkFilename, deleteSet, prevSegmentReaderList);
			/*
			 * apply new delete.set.# to each segment
			 */
			for (int i = 0; i < prevSegmentSize; i++) {
				segmentReaderList.get(i).setDeleteSet(deleteSetList[i]);
			}
		}
		return deleteSetList;
	}

	/*
	 * Indexing작업으로 생성된 pk와 delete list 를 이전 segment들과 비교해보면서 세그먼트별 delete.set.#들을 업데이트한다. delete.set.# 파일들은 최종 revision디렉토리안에
	 * 존재한다.
	 */
	private BitSet[] updateDeleteSetWithPrevSegments(String segmentId, File targetRevisionDir, String pkFilename, DeleteIdSet deleteIdSet,
			List<SegmentReader> prevSegmentReaderList) throws IOException {
		// 첨자 i는 세그먼터 id와 일치해야한다.
		int prevSegmentSize = prevSegmentReaderList.size();
		BitSet[] prevDeleteSetList = new BitSet[prevSegmentSize];
		PrimaryKeyIndexReader[] prevPkReaderList = new PrimaryKeyIndexReader[prevSegmentSize];
		for (int i = 0; i < prevSegmentSize; i++) {
			SegmentReader prevSegmentReader = prevSegmentReaderList.get(i);
			String id = prevSegmentReader.segmentInfo().getId();
			prevDeleteSetList[i] = new BitSet(targetRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, id));
			prevPkReaderList[i] = new PrimaryKeyIndexReader(prevSegmentReader.revisionDir(), IndexFileNames.primaryKeyMap);
		}

		File pkFile = new File(targetRevisionDir, pkFilename);

		// 1. applyPrimaryKeyToPrevSegments
		// pk끼리 비교하면서 중복된 것은 deleteSet에 넣어준다.
		int updateDocumentCount = applyPrimaryKeyToPrevSegments(pkFile, prevPkReaderList, prevDeleteSetList);

		// 2. applyDeleteIdSetToPrevSegments
		// 업데이트된 deleteSet와 색인시 수집된 deleteIdSet을 
		int deleteDocumentCount = applyDeleteIdSetToPrevSegments(segmentId, targetRevisionDir, deleteIdSet, prevPkReaderList, prevDeleteSetList);

		for (int i = 0; i < prevSegmentSize; i++) {
			prevPkReaderList[i].close();
			prevDeleteSetList[i].save();
			logger.debug("New delete.set saved. set={}", prevDeleteSetList[i]);
		}

		return prevDeleteSetList;
	}

	private void copyDeleteSet(List<SegmentReader> prevSegmentReaderList, File sourceDir, File targetDir) throws IOException {
		for (SegmentReader prevSegmentReader : prevSegmentReaderList) {
			String segmentId = prevSegmentReader.segmentInfo().getId();
			String filename = IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId);
			File f1 = new File(sourceDir, filename);
			File f2 = new File(targetDir, filename);
			logger.debug("Copy file {}=>{}", f1.getAbsolutePath(), f2.getAbsolutePath());
			FileUtils.copyFile(f1, f2);
		}

	}

	private void mergeRevisionPrimaryKeyFilesAndUpdateDeleteSet(String segmentId, File prevRevisionDir, File targetRevisionDir) throws IOException {
		//
		// 이전 revision의 pk와 현재 revision의 pk를 머징하고 중복된 문서번호는 deleteSet에 넣는다.
		//
		int indexInterval = collectionContext.collectionConfig().getIndexConfig().getPkTermInterval();
		BitSet revDeleteSet = new BitSet(targetRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
		File prevRevisionPkFile = new File(prevRevisionDir, IndexFileNames.primaryKeyMap);
		
		String tempPkFilename = IndexFileNames.getTempFileName(IndexFileNames.primaryKeyMap);
		File tempPkFile = new File(targetRevisionDir, tempPkFilename);
		File newPkFile = new File(targetRevisionDir, IndexFileNames.primaryKeyMap);
		if (tempPkFile.exists()) {
			logger.debug("MERGE PK {} AND {} => {}", new Object[] { prevRevisionPkFile.getPath(), tempPkFile.getPath(), newPkFile.getPath() });
			// 동일세그먼트내에서 이전 rev와 새 rev사이의 중복문서가 발견횟수를 증가시킨다.
			int updateDocumentCount = new PrimaryKeyIndexMerger().merge(prevRevisionPkFile, tempPkFile, newPkFile, indexInterval, revDeleteSet);
			logger.debug("UPDATE SIZE after merge = {}", updateDocumentCount);
			revDeleteSet.save();
			// temp PK .index파일은 pk파일을 만들때 생성된 것으로, temp PK용도는 bulk용도이므로 .index파일이 필요없다.
			// 그러므로 삭제!!
			
			String tempPkIndexFilename = IndexFileNames.getIndexFileName(tempPkFilename);
			new File(targetRevisionDir, tempPkFilename).delete();
			new File(targetRevisionDir, tempPkIndexFilename).delete();
		}
	}

	/*
	 * 이번에 색인된 pkFile을 이전 pkFile들과 비교하면서 중복된 pk는 deleteSetList에 추가해준다. 
	 * deleteSet에 추가된 갯수는 업데이트문서 갯수를 의미하며, 해당갯수는 결과로 리턴한다. 
	 */
	private int applyPrimaryKeyToPrevSegments(File pkFile, PrimaryKeyIndexReader[] prevPkReaderList, BitSet[] prevDeleteSetList) throws IOException {

		// 이전 모든 세그먼트를 통틀어 업데이트되고 삭제된 문서수.
		int updateDocumentSize = 0; // 이번 pk와 이전 pk가 동일할 경우

		// 현 pk를 bulk로 읽어들여 id 중복을 확인한다.
		PrimaryKeyIndexBulkReader pkBulkReader = new PrimaryKeyIndexBulkReader(pkFile);

		//제약조건: pk 크기는 1k를 넘지않는다.
		BytesBuffer buf = new BytesBuffer(1024);
		// 새로 추가된 pk가 이전 세그먼트에 존재하면 update된 것이다.
		while (pkBulkReader.next(buf) != -1) {
			// backward matching
			for (int i = prevPkReaderList.length - 1; i >= 0; i--) {
				int localDocNo = prevPkReaderList[i].get(buf);
				// logger.debug("check "+new String(buf.array, 0, buf.limit));
				if (localDocNo != -1) {
					if (!prevDeleteSetList[i].isSet(localDocNo)) {
						// add delete list
						prevDeleteSetList[i].set(localDocNo);
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

	/*
	 * 색인시 수집된 삭제문서리스트 deleteIdSet를 각 deleteSet에 적용한다.
	 * pk파일에 들어있다면 문서가 존재하는 것이므로 deleteSet에 업데이트해준다.
	 */
	private int applyDeleteIdSetToPrevSegments(String segmentId, File targetRevisionDir, DeleteIdSet deleteIdSet,
			PrimaryKeyIndexReader[] prevPkReaderList, BitSet[] prevDeleteSetList) throws IOException {

		int deleteDocumentSize = 0;

		if (deleteIdSet == null) {
			return deleteDocumentSize;
		}
		PrimaryKeysToBytesRef primaryKeysToBytesRef = new PrimaryKeysToBytesRef(collectionContext.schema());
		/*
		 * apply delete set. 이번 색인작업을 통해 삭제가 요청된 문서들을 삭제처리한다.
		 */
		PrimaryKeyIndexReader currentPkReader = new PrimaryKeyIndexReader(targetRevisionDir, IndexFileNames.primaryKeyMap);
		BitSet currentDeleteSet = new BitSet(targetRevisionDir, IndexFileNames.getSuffixFileName(IndexFileNames.docDeleteSet, segmentId));
		Iterator<PrimaryKeys> iterator = deleteIdSet.iterator();
		while (iterator.hasNext()) {

			PrimaryKeys ids = iterator.next();
			logger.debug("--- delete id = {}", ids);
			
			BytesRef bytesRef = primaryKeysToBytesRef.getBytesRef(ids);

			int localDocNo = currentPkReader.get(bytesRef);
			if (localDocNo != -1) {
				// add delete list
				logger.debug("--- delete localDocNo = {} at current segment", localDocNo);
				currentDeleteSet.set(localDocNo);
				deleteDocumentSize++;// deleteSize 증가.
				// 현재 세그먼트에서 삭제된 pk는, 이전 세그먼트에서 찾아볼필요가 없다.
				// 왜냐하면 현재세그먼트와 동일한 pk가 이전 세그먼트에 존재한다면 해당 pk는 이미 삭제리스트에 존재할것이기 때문이다.
				continue;
			}
			// backward matching
			for (int i = prevPkReaderList.length - 1; i >= 0; i--) {
				localDocNo = prevPkReaderList[i].get(bytesRef);
				if (localDocNo != -1) {
					if (!prevDeleteSetList[i].isSet(localDocNo)) {
						// add delete list
						logger.debug("--- delete doc[{}] at segment[{}]", localDocNo, i);
						prevDeleteSetList[i].set(localDocNo);
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

}
