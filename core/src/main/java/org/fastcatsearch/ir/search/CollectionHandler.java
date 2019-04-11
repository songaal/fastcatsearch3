package org.fastcatsearch.ir.search;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.analysis.AnalyzerFactoryManager;
import org.fastcatsearch.ir.analysis.AnalyzerPool;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.document.PrimaryKeyIndexBulkReader;
import org.fastcatsearch.ir.document.PrimaryKeyIndexReader;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.PrimaryKeys;
import org.fastcatsearch.ir.index.SegmentIdGenerator;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.BufferedFileOutput;
import org.fastcatsearch.ir.io.BytesBuffer;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Counter;
import org.fastcatsearch.ir.util.DummyCounter;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectionHandler {
	private static Logger logger = LoggerFactory.getLogger(CollectionHandler.class);
    private static Logger segmentLogger = LoggerFactory.getLogger("SEGMENT_LOG");
	private String collectionId;
	private CollectionContext collectionContext;
	private CollectionSearcher collectionSearcher;
	private Map<String, SegmentReader> segmentReaderMap;
    private Map<String, SegmentReader> tmpSegmentReaderMap;
	private Schema schema;
	private long startedTime;
	private boolean isLoaded;
	private FilePaths collectionFilePaths;

	private AnalyzerFactoryManager analyzerFactoryManager;
	private AnalyzerPoolManager analyzerPoolManager;

	private Counter queryCounter;

    private SegmentIdGenerator segmentIdGenerator;

    private DelayQueue<SegmentDelayedClose> segmentDelayedCloseQueue;

    private Map<String, String> mergingSegmentSet;
    private Map<String, List<String>> deletionForMergingMap;
    private Map<String, AtomicInteger> deletionIdRefCounter;

    public CollectionHandler(CollectionContext collectionContext, AnalyzerFactoryManager analyzerFactoryManager) throws IRException, SettingException {
		this.collectionContext = collectionContext;
		this.collectionId = collectionContext.collectionId();
		this.collectionFilePaths = collectionContext.collectionFilePaths();
		this.analyzerFactoryManager = analyzerFactoryManager;
        //현재 마지막 세그먼트 이름이후로 다시 시작한다.

        SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLatestSegmentInfo();
        if(lastSegmentInfo == null) {
            this.segmentIdGenerator = new SegmentIdGenerator();
        } else {
            this.segmentIdGenerator = new SegmentIdGenerator(lastSegmentInfo.getId());
            segmentIdGenerator.nextId(); //마지막에서 하나 증가시킨다.
        }
        logger.debug("## Segment Id start from {}", segmentIdGenerator.currentId());
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
		return collectionSearcher.clone();
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

        mergingSegmentSet = new ConcurrentHashMap<String, String>();
        deletionForMergingMap = new ConcurrentHashMap<String, List<String>>();
        deletionIdRefCounter = new ConcurrentHashMap<String, AtomicInteger>();
		// 색인기록이 있다면 세그먼트를 로딩한다.
		segmentReaderMap = new ConcurrentHashMap<String, SegmentReader>();
        tmpSegmentReaderMap = new ConcurrentHashMap<String, SegmentReader>();

        try {
            for (SegmentInfo segmentInfo : collectionContext.dataInfo().getSegmentInfoList()) {
                File segmentDir = dataPaths.segmentFile(dataSequence, segmentInfo.getId());
                if(segmentDir.exists()) {
                    segmentReaderMap.put(segmentInfo.getId(), new SegmentReader(segmentInfo, schema, segmentDir, analyzerPoolManager));
                } else {
                    logger.error("[{}] Cannot find segment dir = {}", collectionId, segmentDir.getName());
                }
            }
        } catch (IOException e) {
            throw new IRException(e);
        }

        //쓸데없는 세그먼트는 지운다.
        File[] segmentDirList = dataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        for(File dir : segmentDirList) {
            String name = dir.getName();
            //세그먼트에 포함된 디렉토리가 아니면 지운다.
            if(!segmentReaderMap.containsKey(name)) {
                FileUtils.deleteQuietly(dir);
            }
        }

        //쓸데없는 req파일은 지운다.
        File[] reqList = dataDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(IndexFileNames.docDeleteReq);
            }
        });
        for(File dir : reqList) {
            FileUtils.deleteQuietly(dir);
        }
	}

	public void close() throws IOException {
		logger.info("Close Collection handler {}", collectionId);
		if (segmentReaderMap != null) {
			for (SegmentReader segmentReader : segmentReaderMap.values()) {
				segmentReader.close();
			}
            segmentReaderMap.clear();
		}

        if (tmpSegmentReaderMap != null) {
            for (SegmentReader segmentReader : tmpSegmentReaderMap.values()) {
                segmentReader.close();
            }
            tmpSegmentReaderMap.clear();
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
			logger.debug("SEG#{} >> {}", i++, segmentReader.segmentInfo());
		}
	}

	public SegmentReader segmentReader(String segmentId) {
		return segmentReaderMap.get(segmentId);
	}

    //머징되서 삭제된 세그먼트를 잠시동안 유지한다.
    public SegmentReader getTmpSegmentReader(String segmentId) {
        return tmpSegmentReaderMap.get(segmentId);
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


    /////////////////
    // 머징 관련 메소드
    /////////////////
    public void putMerging(Set<String> segmentIdSet) {
        for(String segmentId : segmentIdSet) {
            mergingSegmentSet.put(segmentId, segmentId);
        }
    }

    public boolean isMerging(String segmentId) {
        return mergingSegmentSet.containsKey(segmentId);
    }

    public void finishMerging(String segmentId) {
        mergingSegmentSet.remove(segmentId);
    }

    ////////////////////
    // 머징시 삭제처리 관련
    ///////////////////

    public void prepareMergingDeletion(String documentId) {
        deletionForMergingMap.put(documentId, new ArrayList<String>());
        logger.debug("[{}] deletionForMergingMap Prepare {}", collectionId, documentId);
    }

    public boolean needMergingDeletion() {
        return deletionForMergingMap.size() > 0;
    }

    public List<String> takeMergingDeletion(String documentId) {
        logger.debug("[{}] deletionForMergingMap Remove {}", collectionId, documentId);
        return deletionForMergingMap.remove(documentId);
    }

    public void addMergingDeletion(String segmentId) {
        logger.debug("[{}] deletionForMergingMap Add {} to {}", collectionId, segmentId, deletionForMergingMap.keySet());
        ///갯수.
        deletionIdRefCounter.put(segmentId, new AtomicInteger(deletionForMergingMap.size()));
        for(Map.Entry<String, List<String>> entry : deletionForMergingMap.entrySet()) {
            entry.getValue().add(segmentId);
        }
    }

    /*
    * segmentInfo 가 null이면 새로 추가된 문서없이 deleteIdSet만 존재하는것임.
    * */
    public synchronized CollectionContext applyNewSegment(SegmentInfo segmentInfo, File segmentDir, DeleteIdSet deleteIdSet) throws IOException, IRException {

        String tempSegmentId = segmentInfo.getId();
        try {
            int liveDocumentSize = segmentInfo.getLiveCount();
            segmentLogger.info("[{}] -NewSegment-----", collectionId);
            segmentLogger.info("[{}] NewSegment start[{}] id[{}] doc[{}] del[{}] delReq[{}]", collectionId, segmentInfo.getStartTime(), tempSegmentId, segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), deleteIdSet.size());
            List<PrimaryKeyIndexReader> pkReaderList = new ArrayList<PrimaryKeyIndexReader>();
            List<BitSet> deleteSetList = new ArrayList<BitSet>();

            int segmentSize = segmentReaderMap.size();
            for (SegmentReader segmentReader : segmentReaderMap.values()) {
                File dir = segmentReader.segmentDir();
                pkReaderList.add(new PrimaryKeyIndexReader(dir, IndexFileNames.primaryKeyMap));
                deleteSetList.add(new BitSet(dir, IndexFileNames.docDeleteSet));
            }

            //세그먼트간의 삭제처리.
            if (segmentSize > 0) {
                /*
                * 1. PK Update 적용
                * */
                if (liveDocumentSize > 0) {
                    File pkFile = new File(segmentDir, IndexFileNames.primaryKeyMap);
                    applyPrimaryKeyToSegments(pkFile, pkReaderList, deleteSetList);
                }
                /*
                * 2. deleteIdSet 적용
                * */
                if(deleteIdSet.size() > 0) {
                    applyDeleteIdSetToSegments(deleteIdSet, pkReaderList, deleteSetList);
                }
            }

            for (PrimaryKeyIndexReader pkReader : pkReaderList) {
                pkReader.close();
            }

            // delete.req 파일은 머징중인 세그먼트의 데이터 일관성을 위함이다.
            // 머징중인 세그먼트가 있을때에만 delete.req 파일을 만든다.

            // 머징중인 각 세그먼트의 delete.set을 기반으로 삭제문서를 재확인할 것이므로, 따로 기록해놓을 필요가 없다.
            if (needMergingDeletion()) {
                //삭제ID만 기록해 놓은 delete.req 파일을 만들어 놓는다. (차후 세그먼트 병합시 사용됨)
                File deleteIdFile = new File(segmentDir.getParentFile(), tempSegmentId + "." + IndexFileNames.docDeleteReq);

                logger.debug("create deleteIdFile {}", deleteIdFile.getName());

                BufferedFileOutput deleteIdOutput = null;
                int size = 0;
                try {
                    deleteIdOutput = new BufferedFileOutput(deleteIdFile);
                    deleteIdOutput.writeInt(0);

                    ///1. pk를 모두 기록..
                    File newSegmentPkFile = new File(segmentDir, IndexFileNames.primaryKeyMap);
                    if (newSegmentPkFile.exists()) {
                        PrimaryKeyIndexBulkReader pkBulkReader = new PrimaryKeyIndexBulkReader(newSegmentPkFile);
                        try {
                            BytesBuffer buf = new BytesBuffer(1024);

                            while (pkBulkReader.next(buf) != -1) {
                                deleteIdOutput.writeVInt(buf.length());
                                deleteIdOutput.write(buf.array(), buf.offset, buf.length());
                                logger.debug("deleteId#1: {}", new String(buf.array(), buf.offset, buf.length()));
                                size++;
                            }
                        } finally {
                            if(pkBulkReader != null) {
                                pkBulkReader.close();
                            }
                        }
                    }
                    // 2. deleteset을 모두기록..
                    if(deleteIdSet.size() > 0) {
                        PrimaryKeysToBytesRef primaryKeysToBytesRef = new PrimaryKeysToBytesRef(schema);
                        Iterator<PrimaryKeys> iterator = deleteIdSet.iterator();
                        while (iterator.hasNext()) {
                            PrimaryKeys ids = iterator.next();
                            BytesRef ref = primaryKeysToBytesRef.getBytesRef(ids);
                            deleteIdOutput.writeVInt(ref.length());
                            deleteIdOutput.write(ref.array(), ref.offset, ref.limit - ref.offset);
                            logger.debug("deleteId#2: {}", new String(ref.array(), ref.offset, ref.limit - ref.offset));
                            size++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("error while write delete id file = " + deleteIdFile.getName(), e);
                    throw e;
                } finally {
                    if (deleteIdOutput != null) {
                        deleteIdOutput.seek(0);
                        deleteIdOutput.writeInt(size);
                        deleteIdOutput.close();
                    }
                }
                logger.debug("create {} size[{}]", deleteIdFile.getName(), size);
                addMergingDeletion(tempSegmentId);
                segmentLogger.info("[{}] NewSegment id[{}] created {}.{}", collectionId, tempSegmentId, tempSegmentId, IndexFileNames.docDeleteReq);
            }

            /*
             * 먼저 추가된 세그먼트를 붙이고 나서 삭제를 수행
              * 세그먼트 붙이기가 에러나면 삭제도 안한다.
             */
            if (liveDocumentSize > 0) {
                String segmentId = getNextSegmentId(segmentDir.getParentFile(), 100);
                File newSegmentDir = new File(segmentDir.getParentFile(), segmentId);
                FileUtils.moveDirectory(segmentDir, newSegmentDir);
                segmentLogger.info("[{}] NewSegment move id[{}] <- {}", collectionId, segmentId, tempSegmentId);
                segmentInfo.setId(segmentId);

                //신규 세그먼트 추가.
                SegmentReader segmentReader = new SegmentReader(segmentInfo, schema, newSegmentDir, analyzerPoolManager);
                segmentReaderMap.put(segmentId, segmentReader);
                collectionContext.addSegmentInfo(segmentInfo);
                long createTime = System.currentTimeMillis();
                segmentInfo.setCreateTime(createTime);
                segmentLogger.info("[{}] NewSegment id[{}] create[{}]", collectionId, segmentId, createTime);
            }

            // 여기에서 삭제파일을 업데이트 한다.
            for (BitSet deleteSet : deleteSetList) {
                deleteSet.save();
                logger.debug("[{}] New delete.set saved. set={}", collectionId, deleteSet);
            }
            //기존 세그먼트들 삭제리스트 재로딩
            for (SegmentReader r : segmentReaderMap.values()) {
                r.loadDeleteSet();
                r.syncDeleteCountToInfo();
            }

            /*
             * 업데이트되어 문서가 0인 세그먼트는 여기서 처리하지 않는다.
             * 머징중일수도 있기때문에, 머징 스케쥴러가 LocalDocZeroDeleteJob 를 태워서 처리하도록 한다.
             */

            collectionContext.dataInfo().updateAll();

            DataInfo dataInfo = collectionContext.dataInfo();
            int liveSize = dataInfo.getDocuments() - dataInfo.getDeletes();

            segmentLogger.info("[{}] NewSegment live[{}] doc[{}] del[{}] segSize[{}] tmpSegSize[{}]", collectionId, liveSize, dataInfo.getDocuments(), dataInfo.getDeletes(), segmentReaderMap.size(), tmpSegmentReaderMap.size());
            for(SegmentInfo info : dataInfo.getSegmentInfoList()) {
                segmentLogger.info("[{}] [{}] Segment live[{}] doc[{}] del[{}]", collectionId, info.getId(), info.getLiveCount(), info.getDocumentCount(), info.getDeleteCount());
            }
        }catch (Throwable t) {
            logger.error("에러발생", t);
            throw new IRException("applyNewSegment 에러발생.", t);
        }
        logger.debug("[{}] mergingSegmentSet > {}", collectionId, mergingSegmentSet);
        logger.debug("[{}]deletionForMergingMap > {}", collectionId, deletionForMergingMap);
        logger.debug("[{}]deletionIdRefCounter > {}", collectionId, deletionIdRefCounter);
        return collectionContext;
    }

    /*
    * 동적색인 pk update
    * */
    private int applyPrimaryKeyToSegments(File pkFile, List<PrimaryKeyIndexReader> prevPkReaderList, List<BitSet> prevDeleteSetList) throws IOException {

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
                    if (localDocNo != -1) {
                        BitSet deleteSet = prevDeleteSetList.get(i);
                        if (!deleteSet.isSet(localDocNo)) {
                            // add delete list
                            deleteSet.set(localDocNo);
                            updateDocumentSize++;// updateSize 증가
//                            segmentLogger.info("DEL_1 {} [{}] {}", pkReader.getDir().getName(), localDocNo, new String(buf.array(), 0, buf.limit));
                        } else {
//                            segmentLogger.info("DEL_0 {} [{}] {}", pkReader.getDir().getName(), localDocNo, new String(buf.array(), 0, buf.limit));
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

    /*
    * 동적색인 delete 적용.
    * */
    private int applyDeleteIdSetToSegments(DeleteIdSet deleteIdSet, List<PrimaryKeyIndexReader> prevPkReaderList,
                                           List<BitSet> prevDeleteSetList) throws IOException {

        int deleteDocumentSize = 0;

        if (deleteIdSet == null) {
            return deleteDocumentSize;
        }
        PrimaryKeysToBytesRef primaryKeysToBytesRef = new PrimaryKeysToBytesRef(schema);
		/*
		 * apply delete set. 이번 색인작업을 통해 삭제가 요청된 문서들을 삭제처리한다.
		 */
        Iterator<PrimaryKeys> iterator = deleteIdSet.iterator();
        while (iterator.hasNext()) {

            PrimaryKeys ids = iterator.next();
            logger.debug("--- delete id = {}", ids);

            BytesRef buf = primaryKeysToBytesRef.getBytesRef(ids);

            //기존 색인 세그먼트들에서 찾아서 지운다.
            int i = 0;
            for (PrimaryKeyIndexReader pkReader : prevPkReaderList) {
                int localDocNo = pkReader.get(buf);
                if (localDocNo != -1) {
                    BitSet deleteSet = prevDeleteSetList.get(i);
                    if (!deleteSet.isSet(localDocNo)) {
                        // add delete list
                        deleteSet.set(localDocNo);
                        deleteDocumentSize++;// deleteSize 증가
                        logger.debug("[{}] Mark deleted ids[{}] as docNo[{}]", collectionId, ids, localDocNo);
                    }
                }
                i++;
            }

        }

        return deleteDocumentSize;
    }

    //머징시 문서가 모두 0가 될때사용.
    public synchronized CollectionContext removeZeroSegment(Set<String> segmentIdRemoveList) throws IOException, IRException {
        segmentLogger.info("[{}] -RemoveSegment-----", collectionId);
        segmentLogger.info("[{}] RemoveSegment remove segments {}", collectionId, segmentIdRemoveList);
        removeSegments(segmentIdRemoveList);
        collectionContext.dataInfo().updateAll();

        DataInfo dataInfo = collectionContext.dataInfo();
        int documentSize = dataInfo.getDocuments();
        int deleteSize = dataInfo.getDeletes();
        int liveSize = documentSize - deleteSize;
        segmentLogger.info("[{}] RemoveSegment live[{}] doc[{}] del[{}] segSize[{}] tmpSegSize[{}]", collectionId, liveSize, documentSize, deleteSize, segmentReaderMap.size(), tmpSegmentReaderMap.size());
        for(SegmentInfo info : dataInfo.getSegmentInfoList()) {
            segmentLogger.info("[{}] [{}] Segment live[{}] doc[{}] del[{}]", collectionId, info.getId(), info.getLiveCount(), info.getDocumentCount(), info.getDeleteCount());
        }
        return collectionContext;
    }

    private void removeSegments(Set<String> segmentIdRemoveList) {
        for (String removeSegmentId : segmentIdRemoveList) {
            SegmentReader removeSegmentReader = segmentReaderMap.remove(removeSegmentId);
            if(removeSegmentReader != null) {
                //설정파일도 수정한다.
                collectionContext.removeSegmentInfo(removeSegmentId);
                /*
                * 중요! 삭제된 세그먼트 리더를 tmp 맵에 임시로 넣어둔다. 차후 10초후에 제거되고 close된다..
                * */
                SegmentReader oldSegmentReader = tmpSegmentReaderMap.put(removeSegmentId, removeSegmentReader);
                if(oldSegmentReader != null) {
                    try {
                        oldSegmentReader.close();
                    } catch(Exception ignore) { }
                }
                segmentDelayedCloseQueue.put(new SegmentDelayedClose(collectionId, removeSegmentId, tmpSegmentReaderMap, true));
            }
        }
    }

    public synchronized CollectionContext applyMergedSegment(SegmentInfo segmentInfo, File segmentDir, Set<String> segmentIdRemoveList) throws IOException, IRException {

        String tempSegmentId = segmentInfo.getId();
        List<String> createdSegmentIdList = takeMergingDeletion(tempSegmentId);
        segmentLogger.info("[{}] -MergedSegment-----", collectionId);
        segmentLogger.info("[{}] MergedSegment start[{}] id[{}] doc[{}] del[{}] merged[{}]", collectionId, segmentInfo.getStartTime(), tempSegmentId, segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), segmentIdRemoveList);

        /*
         * 기존 세그먼트의 delete.req와 pk를 통해 현 세그먼트를 삭제받는다.
         */

        PrimaryKeyIndexReader pkReader = new PrimaryKeyIndexReader(segmentDir, IndexFileNames.primaryKeyMap);
        BitSet deleteSet = new BitSet();

        // createdSegmentIdList 의 아이디를 가지고, 삭제를 확인한다. id.pk.lst 파일에 업데이트된 pk가 모두 들어있다.
        BytesBuffer buf = new BytesBuffer(1024);
        for(String deleteReqId : createdSegmentIdList) {
            File f = new File(segmentDir.getParentFile(), deleteReqId + "." + IndexFileNames.docDeleteReq);
            logger.debug("try delete file {}", f.getName());
            BufferedFileInput input = null;
            try {
                input = new BufferedFileInput(f);
                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    int limit = input.readVInt();
                    buf.limit(limit);
                    input.read(buf.array(), 0, limit);
                    int localDocNo = pkReader.get(buf);
                    if (localDocNo != -1) {
                        if (!deleteSet.isSet(localDocNo)) {
                            // add delete list
                            deleteSet.set(localDocNo);
//                            segmentLogger.info("DEL_1 [{}] {}", localDocNo, new String(buf.array(), 0, buf.limit));
                        } else {
//                            segmentLogger.info("DEL_0 [{}] {}", localDocNo, new String(buf.array(), 0, buf.limit));
                        }
                    }
                    buf.clear();
                }
            } finally {
                if(input != null) {
                    try {
                        input.close();
                    } catch(Exception e) {}
                }
            }
            AtomicInteger count = deletionIdRefCounter.get(deleteReqId);
            int referenceCount = count.decrementAndGet();
            logger.debug("check ref {} > {}", deleteReqId, referenceCount);
            if(referenceCount == 0) {
                //지운다.
                deletionIdRefCounter.remove(deleteReqId);
                logger.debug("delete file {}", f.getName());
                f.delete();
            }
        }

        pkReader.close();

        String segmentId = getNextSegmentId(segmentDir.getParentFile(), 100);

        File newSegmentDir = new File(segmentDir.getParentFile(), segmentId);
        FileUtils.moveDirectory(segmentDir, newSegmentDir);
        segmentLogger.info("[{}] MergedSegment move id[{}] <- {}", collectionId, segmentId, tempSegmentId);

        //2016-2-24 swsong 삭제파일은 최종적으로 여기에서 적용한다.
        deleteSet.setFile(new File(newSegmentDir, IndexFileNames.docDeleteSet));
        deleteSet.save();

        segmentInfo.setId(segmentId);
        SegmentReader segmentReader = new SegmentReader(segmentInfo, schema, newSegmentDir, analyzerPoolManager);
        segmentReader.syncDeleteCountToInfo();
        segmentReaderMap.put(segmentId, segmentReader);

        collectionContext.addSegmentInfo(segmentInfo);

        for(String removeSegmentId : segmentIdRemoveList) {
            finishMerging(removeSegmentId);
        }

        //2016.10.26 swsong 이부분에서 동기화 에러가 발생한듯.. 머징후에 머징된 서그먼트들을 지우는 작업이 동반되는데.
        // 지속적인 실시간 색인후 dataInfo 를 파일에 기록하다가, 머징작업후 removeSegment 부분과 dataInfo.segmentInfoList 수정코드와 겹쳐서 동기화 문제발생.
        // iteration 이전에 동기화블럭을 잡도록수정.
        removeSegments(segmentIdRemoveList);
        collectionContext.dataInfo().updateAll();

        long createTime = System.currentTimeMillis();
        segmentInfo.setCreateTime(createTime);
        segmentLogger.info("[{}] MergedSegment id[{}] create[{}]", collectionId, segmentId, createTime);

        DataInfo dataInfo = collectionContext.dataInfo();
        int documentSize = dataInfo.getDocuments();
        int deleteSize = dataInfo.getDeletes();
        int liveSize = documentSize - deleteSize;
        segmentLogger.info("[{}] MergedSegment live[{}] doc[{}] del[{}] segSize[{}] tmpSegSize[{}]", collectionId, liveSize, documentSize, deleteSize, segmentReaderMap.size(), tmpSegmentReaderMap.size());
        for(SegmentInfo info : dataInfo.getSegmentInfoList()) {
            segmentLogger.info("[{}] [{}] Segment live[{}] doc[{}] del[{}]", collectionId, info.getId(), info.getLiveCount(), info.getDocumentCount(), info.getDeleteCount());
        }

        logger.debug("[{}] mergingSegmentSet > {}", collectionId, mergingSegmentSet);
        logger.debug("[{}]deletionForMergingMap > {}", collectionId, deletionForMergingMap);
        logger.debug("[{}]deletionIdRefCounter > {}", collectionId, deletionIdRefCounter);
        return collectionContext;
    }

    private String getNextSegmentId(File dir, int maxTry) throws IRException {
        String segmentId = null;
        int idTry = 0;
        while (true) {
            segmentId = segmentIdGenerator.nextId();

            //세그먼트 디렉토리가 없을때 까지 찾는다.
            File tmpSegmentDir = new File(dir, segmentId);
            if (!tmpSegmentDir.exists()) {
                break;
            }
            segmentLogger.warn("[{}] MergedSegment [{}] is exists. find next id.", collectionId, segmentId);
            idTry++;
            if(idTry > maxTry) {
                //100번이상 시도했으면, 머징실패.
                segmentLogger.error("[{}] MergedSegment cannot proceed merging.", collectionId);
                throw new IRException("Too many segment error!");
            }
        }

        return segmentId;
    }

    /*
    * 머징색인 pk update
    * */
//    private int applyPrimaryKeyFromSegments(List<PrimaryKeyIndexBulkReader> pkBulkReaderList, PrimaryKeyIndexReader pkReader, BitSet deleteSet) throws IOException {
//
//        // 이전 모든 세그먼트를 통틀어 업데이트되고 삭제된 문서수.
//        int updateDocumentSize = 0; // 이번 pk와 이전 pk가 동일할 경우
//
//        // 제약조건: pk 크기는 1k를 넘지않는다.
//        BytesBuffer buf = new BytesBuffer(1024);
//        for(PrimaryKeyIndexBulkReader pkBulkReader : pkBulkReaderList) {
//            // 새로 추가된 pk가 이전 세그먼트에 존재하면 update된 것이다.
//            while (pkBulkReader.next(buf) != -1) {
//                int localDocNo = pkReader.get(buf);
//                // logger.debug("check "+new String(buf.array, 0, buf.limit));
//                if (localDocNo != -1) {
////                    segmentLogger.debug("merge delete {}", localDocNo);
//                    if (!deleteSet.isSet(localDocNo)) {
//                        // add delete list
//                        deleteSet.set(localDocNo);
//                        updateDocumentSize++;
//                        segmentLogger.info("DEL_1 {} [{}] {}", pkBulkReader.getFile().getParentFile().getName(), localDocNo, new String(buf.array(), 0, buf.limit));
//                    } else {
//                        segmentLogger.info("DEL_0 {} [{}] {}", pkBulkReader.getFile().getParentFile().getName(), localDocNo, new String(buf.array(), 0, buf.limit));
//                    }
//                }
//                buf.clear();
//            }
//        }
//
//        return updateDocumentSize;
//    }

    /*
    * 머징색인 delete 적용.
    * */
//    private int applyDeleteIdSetFromSegments(File[] deleteReqFileList, PrimaryKeyIndexReader pkReader, BitSet deleteSet) throws IOException {
//
//        int deleteDocumentSize = 0;
//
//        PrimaryKeysToBytesRef primaryKeysToBytesRef = new PrimaryKeysToBytesRef(schema);
//		/*
//		 * apply delete set. 이번 색인작업을 통해 삭제가 요청된 문서들을 삭제처리한다.
//		 */
//        for(File deleteReqFile : deleteReqFileList) {
//            if(!deleteReqFile.exists()) {
//                continue;
//            }
//
//            DeleteIdSet deleteReq = new DeleteIdSet();
//            BufferedFileInput deleteIdInput = null;
//            try {
//                deleteIdInput = new BufferedFileInput(deleteReqFile);
//                deleteReq.readFrom(deleteIdInput);
//            } catch (Exception e) {
//                logger.error("", e);
//            } finally {
//                if(deleteIdInput != null) {
//                    deleteIdInput.close();
//                }
//            }
//
//            Iterator<PrimaryKeys> iterator = deleteReq.iterator();
//            while (iterator.hasNext()) {
//
//                PrimaryKeys ids = iterator.next();
//                logger.debug("--- delete id = {}", ids);
//
//                BytesRef buf = primaryKeysToBytesRef.getBytesRef(ids);
//
//                //기존 색인 세그먼트들에서 찾아서 지운다.
//                int localDocNo = pkReader.get(buf);
//                if (localDocNo != -1) {
//                    if (!deleteSet.isSet(localDocNo)) {
//                        // add delete list
//                        deleteSet.set(localDocNo);
//                        deleteDocumentSize++;// deleteSize 증가
//                    }
//                }
//            }
//        }
//
//        return deleteDocumentSize;
//    }

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

    public void setSegmentDelayedCloseQueue(DelayQueue<SegmentDelayedClose> segmentDelayedCloseQueue) {
        this.segmentDelayedCloseQueue = segmentDelayedCloseQueue;
    }
}
