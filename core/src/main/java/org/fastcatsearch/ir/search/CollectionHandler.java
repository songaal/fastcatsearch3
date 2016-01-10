package org.fastcatsearch.ir.search;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.analysis.AnalyzerFactoryManager;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.DynamicIndexer;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.index.SegmentIdGenerator;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Counter;
import org.fastcatsearch.ir.util.DummyCounter;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionHandler {
	private static Logger logger = LoggerFactory.getLogger(CollectionHandler.class);
	private String collectionId;
	private CollectionContext collectionContext;
	private CollectionSearcher collectionSearcher;
	private Map<String, SegmentReader> segmentReaderMap;
	private Schema schema;
	private long startedTime;
	private boolean isLoaded;
	private FilePaths collectionFilePaths;

	private AnalyzerFactoryManager analyzerFactoryManager;
	private AnalyzerPoolManager analyzerPoolManager;

	private Counter queryCounter;

    private SegmentIdGenerator segmentIdGenerator = new SegmentIdGenerator();

    private DynamicIndexer dynamicIndexer;

	public CollectionHandler(CollectionContext collectionContext, AnalyzerFactoryManager analyzerFactoryManager) throws IRException, SettingException {
		this.collectionContext = collectionContext;
		this.collectionId = collectionContext.collectionId();
		this.collectionFilePaths = collectionContext.collectionFilePaths();
		this.analyzerFactoryManager = analyzerFactoryManager;

		queryCounter = new DummyCounter();
	}

	public CollectionHandler load() throws IRException {
		loadSearcherAndReader();
		this.collectionSearcher = new CollectionSearcher(this);
		startedTime = System.currentTimeMillis();
		isLoaded = true;
		logger.info("Collection[{}] Loaded! {}", collectionId, collectionFilePaths.file().getAbsolutePath());
		return this;
	}

    public DynamicIndexer dynamicIndexer() {
        return dynamicIndexer;
    }

	@Deprecated
	public void setAnalyzerPoolManager(AnalyzerPoolManager analyzerPoolManager) {
		this.analyzerPoolManager = analyzerPoolManager;
	}

	public AnalyzerPoolManager analyzerPoolManager() {
		return analyzerPoolManager;
	}

	public Schema schema() {
		return schema;
	}

	public long getStartedTime() {
		return startedTime;
	}

	public FilePaths indexFilePaths() {
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

		analyzerPoolManager = new AnalyzerPoolManager();
		List<AnalyzerSetting> analyzerSettingList = collectionContext.schema().schemaSetting().getAnalyzerSettingList();
		analyzerPoolManager.register(analyzerSettingList, analyzerFactoryManager);

		this.schema = collectionContext.schema();
		int dataSequence = collectionContext.indexStatus().getSequence();
		FilePaths dataPaths = collectionFilePaths.dataPaths();
		File dataDir = dataPaths.indexFilePaths(dataSequence).file();
		if (!dataDir.exists()) {
			logger.info("create collection data directory [{}]", dataDir.getAbsolutePath());
			dataDir.mkdir();
		}

		logger.debug("Load CollectionHandler [{}] data >> {}", collectionId, dataDir.getAbsolutePath());

		// 색인기록이 있다면 세그먼트를 로딩한다.
		segmentReaderMap = new ConcurrentHashMap<String, SegmentReader>();

        try {
            for (SegmentInfo segmentInfo : collectionContext.dataInfo().getSegmentInfoList()) {
                File segmentDir = dataPaths.segmentFile(dataSequence, segmentInfo.getId());
                segmentReaderMap.put(segmentInfo.getId(), new SegmentReader(segmentInfo, schema, segmentDir, analyzerPoolManager));
            }
        } catch (IOException e) {
            throw new IRException(e);
        }
	}

	public void close() throws IOException {
		logger.info("Close Collection handler {}", collectionId);
		if (segmentReaderMap != null) {
			for (SegmentReader segmentReader : segmentReaderMap.values()) {
				segmentReader.close();
			}
		}
		collectionSearcher = null;
		isLoaded = false;
	}

	public String collectionId() {
		return collectionId;
	}

	public int getDataSequence() {
		return collectionContext.indexStatus().getSequence();

	}

	public void printSegmentStatus() {
		int i = 0;
		for (SegmentReader segmentReader : segmentReaderMap.values()) {
			logger.info("SEG#{} >> {}", i++, segmentReader.segmentInfo());
		}
	}

	public SegmentReader segmentReader(String segmentId) {
		return segmentReaderMap.get(segmentId);
	}

    public String nextSegmentId() {
        Set segmentIdSet = segmentReaderMap.keySet();
        String id = null;
        do {
            id = segmentIdGenerator.nextId();
        } while (segmentIdSet.contains(id));
        return id;
    }

	public SegmentSearcher segmentSearcher(String segmentId) {
        return segmentReaderMap.get(segmentId).segmentSearcher();
    }

    public Collection<SegmentReader> segmentReaders() {
        return segmentReaderMap.values();
    }

    public SegmentSearcher getFirstSegmentSearcher() {
        String segmentId = segmentReaderMap.keySet().iterator().next();
        return segmentReaderMap.get(segmentId).segmentSearcher();
    }

	/**
	 * 증분색인후 호출하는 메소드이다. 1. 이전 세그먼트에 삭제 문서를 적용한다. 2. 해당 segment reader 재로딩 및 대체하기.
	 * */
	public void updateCollection(CollectionContext collectionContext, SegmentInfo segmentInfo, File segmentDir, DeleteIdSet deleteSet) throws IOException, IRException {

		this.collectionContext = collectionContext;
//		String segmentId = segmentInfo.getId();
		logger.debug("Add segment {}", segmentInfo);
		int[] updateAndDeleteCount = updateDeleteSetWithSegments(segmentDir, deleteSet);
		/*
		 * 적용
		 */
		addSegmentApplyCollection(segmentInfo, segmentDir);
	}

	private int[] updateDeleteSetWithSegments(File segmentDir, DeleteIdSet deleteIdSet) throws IOException {
		int[] updateAndDelete = new int[] { 0, 0 };
		List<SegmentReader> segmentReaderList = new ArrayList<SegmentReader>();
		List<PrimaryKeyIndexReader> prevPkReaderList = new ArrayList<PrimaryKeyIndexReader>();
		List<BitSet> prevDeleteSetList = new ArrayList<BitSet>();

		for(SegmentReader segmentReader : segmentReaderMap.values()) {
			File dir = segmentReader.segmentDir();
			segmentReaderList.add(segmentReader);
			prevPkReaderList.add(new PrimaryKeyIndexReader(dir, IndexFileNames.primaryKeyMap));
			prevDeleteSetList.add(new BitSet(dir, IndexFileNames.docDeleteSet));
		}
//		// 1. applyPrimaryKeyToPrevSegments
//		// 신규 segment의 pk를 이전 segment의 pk와 비교하면서, 동일하면 삭제표시 한다.
//		// pk끼리 비교하면서 중복된 것은 deleteSet에 넣준다.
//		if (segmentReaderList.size() > 0) {
//			File pkFile = new File(segmentDir, IndexFileNames.primaryKeyMap);
//			int updateDocumentCount = applyPrimaryKeyToPrevSegment(pkFile, prevPkReaderList, prevDeleteSetList);
//			updateAndDelete[0] = updateDocumentCount;
//		}

		// 2. applyDeleteIdSetToPrevSegments
		// 색인시 수집된 deleteIdSet을 적용한다. 현재 세그먼트.revision과 이전 세그먼트에 모두적용.
		// 추가된 리비전이라면, 이전 리비전의 pk가 이미 머징되어있어야한다.
		int deleteDocumentCount = applyDeleteIdSetToAllSegment(segmentDir, deleteIdSet, prevPkReaderList, prevDeleteSetList);
		updateAndDelete[1] = deleteDocumentCount;

		for (PrimaryKeyIndexReader pkReader : prevPkReaderList) {
			pkReader.close();
		}
		for (BitSet deleteSet : prevDeleteSetList) {
			deleteSet.save();
			logger.debug("New delete.set saved. set={}", deleteSet);
		}
		return updateAndDelete;
	}

	/*
	 * 색인시 수집된 삭제문서리스트 deleteIdSet를 각 deleteSet에 적용한다. pk파일에 들어있다면 문서가 존재하는 것이므로 deleteSet에 업데이트해준다.
	 */
	private int applyDeleteIdSetToAllSegment(File segmentDir, DeleteIdSet deleteIdSet, List<PrimaryKeyIndexReader> prevPkReaderList,
											  List<BitSet> prevDeleteSetList) throws IOException {

		int deleteDocumentSize = 0;

		if (deleteIdSet == null) {
			return deleteDocumentSize;
		}
		PrimaryKeysToBytesRef primaryKeysToBytesRef = new PrimaryKeysToBytesRef(schema);
		/*
		 * apply delete set. 이번 색인작업을 통해 삭제가 요청된 문서들을 삭제처리한다.
		 */
		PrimaryKeyIndexReader currentPkReader = new PrimaryKeyIndexReader(segmentDir, IndexFileNames.primaryKeyMap);
		BitSet currentDeleteSet = new BitSet(segmentDir, IndexFileNames.docDeleteSet);
		Iterator<PrimaryKeys> iterator = deleteIdSet.iterator();
		while (iterator.hasNext()) {

			PrimaryKeys ids = iterator.next();
			logger.debug("--- delete id = {}", ids);

			BytesRef buf = primaryKeysToBytesRef.getBytesRef(ids);

			/*
			* 1. 신규 색인 세그먼트.
			* */
			int localDocNo = currentPkReader.get(buf);
			if (localDocNo != -1) {
				// add delete list
				logger.debug("--- delete localDocNo = {} at current segment", localDocNo);
				currentDeleteSet.set(localDocNo);
				deleteDocumentSize++;// deleteSize 증가.
				// 현재 세그먼트에서 삭제된 pk는, 이전 세그먼트에서 찾아볼필요가 없다.
				// 왜냐하면 현재세그먼트와 동일한 pk가 이전 세그먼트에 존재한다면 해당 pk는 이미 삭제리스트에 존재할것이기 때문이다.
				continue;
			}
			/*
			* 2. 기존 색인 세그먼트들.
			* */
			int i = 0;
			for (PrimaryKeyIndexReader pkReader : prevPkReaderList) {
				localDocNo = pkReader.get(buf);
				if (localDocNo != -1) {
					BitSet deleteSet = prevDeleteSetList.get(i);
					if (!deleteSet.isSet(localDocNo)) {
						// add delete list
						deleteSet.set(localDocNo);
						deleteDocumentSize++;// deleteSize 증가
					}
				}
				i++;
			}

		}

		currentDeleteSet.save();

		currentPkReader.close();

		return deleteDocumentSize;
	}

	private int applyPrimaryKeyToPrevSegment(File pkFile, List<PrimaryKeyIndexReader> prevPkReaderList, List<BitSet> prevDeleteSetList) throws IOException {

		// 이전 모든 세그먼트를 통틀어 업데이트되고 삭제된 문서수.
		int updateDocumentSize = 0; // 이번 pk와 이전 pk가 동일할 경우

		// 현 pk를 bulk로 읽어들여 id 중복을 확인한다.
		PrimaryKeyIndexBulkReader pkBulkReader = null;
		try {
			pkBulkReader = new PrimaryKeyIndexBulkReader(pkFile);
			// 제약조건: pk 크기는 1k를 넘지않는다.
			BytesBuffer buf = new BytesBuffer(1024);
			// 새로 추가된 pk가 이전 세그먼트에 존재하면 update된 것이다.
			while (pkBulkReader.next(buf) != -1) {
				// backward matching
				int i = 0;
				for (PrimaryKeyIndexReader pkReader : prevPkReaderList) {
					int localDocNo = pkReader.get(buf);
					// logger.debug("check "+new String(buf.array, 0, buf.limit));
					if (localDocNo != -1) {
						BitSet deleteSet = prevDeleteSetList.get(i);
						if (!deleteSet.isSet(localDocNo)) {
							// add delete list
							deleteSet.set(localDocNo);
							updateDocumentSize++;// updateSize 증가
						}
					}
					i++;
				}

				buf.clear();
			}
		} finally {
			if(pkBulkReader != null) {
				pkBulkReader.close();
			}
		}

		return updateDocumentSize;
	}

    // 색인되어있는 세그먼트를 단순히 추가만한다. delete.set파일은 이미 수정되어있다고 가정한다.
    public void addSegmentApplyCollection(SegmentInfo segmentInfo, File segmentDir) throws IOException, IRException {
        addSegmentApplyCollection(segmentInfo, segmentDir, null);
    }

    /*
    * mergeIdList 를 제거하고, 새로운 segment를 추가.
    * 삭제처리는 세그먼트를 apply할때 발생하므로,
    * 머징중 새로운세그먼트가 붙여질수 있으므로,
    * 머징이 끝나고 붙이기 직전 해당 세그먼트에 삭제문서가 추가되었는지 확인하여
    * 머징세그먼트에 삭제처리를 추가로 수행한다.
    * */
    private Object applyLock = new Object();
    public void addSegmentApplyCollection(SegmentInfo segmentInfo, File segmentDir, List<String> segmentIdRemoveList) throws IOException, IRException {
        /*
         * 머징과 증분색인이 동시에 추가될 수 있기 때문에 동기화 시킨다.
         */
		synchronized (applyLock) {
            /*
             * TODO 1. 새로 만들어진 segment의 pk를 기존세그먼트들의 pk와 비교(머징대상은 제외)하여 delete 파일을 업데이트 해준다.
             * TODO 머징된 세그먼트는 여러 세그먼트중 마지막 세그먼트의 붙인시간을 사용한다.
             * TODO 세그먼트 붙인 시간을 비교하여 최근 pk로 기존 pk의 id를 delete 표시한다.
             * TODO deleteIdSet은 차후 머징세그먼트로 적용할수 있으므로, 파일로 가지고 있기로 한다.
             * 2,3이 머징되어 4가 된다고 할때, 1-2-3-4-5 는 문제가 없지만, 1-2-3-5-4 이면, 머징하는중에 5가 붙은 경우이다. 이 경우는 5의 pk와 deleteid를 4에 적용해야한다.
             * applyPrimaryKeyToPrevSegment 참조.
             */









            /*
            * 2. 수정된 delete 파일을 재로딩한다.
            * */
            for (SegmentReader segmentReader : segmentReaderMap.values()) {
                segmentReader.loadDeleteSet();
            }

            /*
            * 3. 삭제할 세그먼트가 있다면 제거한다.
            * */
            if(segmentIdRemoveList != null) {
                for(String removeSegmentId : segmentIdRemoveList) {
                    SegmentReader segmentReader = segmentReaderMap.remove(removeSegmentId);
                    if(segmentReader != null) {
                        //설정파일도 수정한다.
                        collectionContext.removeSegmentInfo(removeSegmentId);
                    }
                    //TODO 레퍼런스가 없으면 닫도록 closeFuture를 구현한다.
                    segmentReader.close();
                    //segmentReader.closeFuture();
                }
            }

            /*
             * 4. 머징완료된 신규세그먼트를 추가해준다.
             * */
            SegmentReader segmentReader = new SegmentReader(segmentInfo, schema, segmentDir, analyzerPoolManager);
            // segment reader 추가.
            // collectionContext에는 segmentInfo를 추가하지 않는다.
            // 색인이 끝나면서 이미 context에 segmentinfo가 추가되어있는 상태이다.
            segmentReaderMap.put(segmentReader.segmentId(), segmentReader);
            // info.xml 파일업데이트용.
            collectionContext.updateSegmentInfo(segmentReader.segmentInfo());
        }
	}

	public int segmentSize() {
		return segmentReaderMap.size();
	}

	public AnalyzerPool getAnalyzerPool(String analyzerId) {
		return analyzerPoolManager.getPool(analyzerId);
	}

	public void setQueryCounter(Counter queryCounter) {
		if (queryCounter != null) {
			this.queryCounter = queryCounter;
			logger.debug("[{}] Collection set Query counter {}", collectionId, queryCounter);
		} else {
			logger.debug("[{}] Collection Query counter Not Found!", collectionId);
		}
	}

	public Counter queryCounter() {
		return queryCounter;
	}

}
