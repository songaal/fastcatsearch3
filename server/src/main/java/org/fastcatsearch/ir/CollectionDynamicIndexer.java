package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.*;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CoreFileUtils;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by swsong on 2016. 1. 14..
 *
 * API를 통해 단발적으로 들어는 문서들을 색인하는 동적인덱서
 */
public class CollectionDynamicIndexer {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractCollectionIndexer.class);
    protected CollectionContext collectionContext;
    protected AnalyzerPoolManager analyzerPoolManager;

    protected long startTime;
    protected IndexingTaskState indexingTaskState;

    protected DeleteIdSet deleteIdSet; //삭제문서리스트. 외부에서 source reader를 통해 셋팅된다.

    protected IndexWriteInfoList indexWriteInfoList;

    protected IndexWritable indexWriter;
    protected DataInfo.SegmentInfo workingSegmentInfo;
    protected int count;
    protected long lapTime;

    protected boolean stopRequested;

    private CollectionHandler collectionHandler;


    public CollectionDynamicIndexer(CollectionHandler collectionHandler) {
        this.collectionContext = collectionHandler.collectionContext();
        this.analyzerPoolManager = collectionHandler.analyzerPoolManager();
    }

    protected IndexWritable createIndexWriter(Schema schema, File segmentDir, DataInfo.SegmentInfo segmentInfo, IndexConfig indexConfig) throws IRException {
        return new SegmentWriter(schema, segmentDir, segmentInfo, indexConfig, analyzerPoolManager, null);
    }

    public DataInfo.SegmentInfo getSegmentInfo() {
        return workingSegmentInfo;
    }

    public void init(Schema schema) throws IRException {

        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        // 증분색인이면 기존스키마그대로 사용.
        String newSegmentId = collectionHandler.nextSegmentId();
        workingSegmentInfo = new DataInfo.SegmentInfo(newSegmentId);
        File segmentDir = indexFilePaths.file(workingSegmentInfo.getId());
        logger.debug("#색인시 세그먼트를 생성합니다. {}", workingSegmentInfo);
        try {
            CoreFileUtils.removeDirectoryCascade(segmentDir);
        } catch (IOException e) {
            throw new IRException(e);
        }

        FilePaths dataFilePaths = collectionContext.collectionFilePaths().dataPaths();
        int dataSequence = collectionContext.getIndexSequence();

        IndexConfig indexConfig = collectionContext.indexConfig();

        logger.debug("WorkingSegmentInfo = {}", workingSegmentInfo);
        String segmentId = workingSegmentInfo.getId();

        segmentDir = dataFilePaths.segmentFile(dataSequence, segmentId);
        logger.info("Segment Dir = {}", segmentDir.getAbsolutePath());

        File filePath = collectionContext.collectionFilePaths().file();

        indexWriter = createIndexWriter(schema, segmentDir, workingSegmentInfo, indexConfig);

        indexWriteInfoList = new IndexWriteInfoList();

        startTime = System.currentTimeMillis();
    }

    public void addDocument(Document document) throws IRException, IOException {
        indexWriter.addDocument(document);
        count++;
        if (count % 10000 == 0) {
            logger.info(
                    "{} documents indexed, lap = {} ms, elapsed = {}, mem = {}",
                    count, System.currentTimeMillis() - lapTime,
                    Formatter.getFormatTime(System.currentTimeMillis() - startTime),
                    Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
            lapTime = System.currentTimeMillis();
        }
        if(indexingTaskState != null){
            indexingTaskState.incrementDocumentCount();
        }
    }

    public void updateDocument(Document document) throws IRException, IOException {

        //TODO

        logger.debug("Update doc > {}", document);

    }
    public void deleteDocument(Document document) throws IRException, IOException {


        //TODO

        logger.debug("Delete doc > {}", document);

    }

    public boolean close() throws IRException, SettingException, IndexingStopException {

//		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
        if (indexWriter != null) {
            try {
                indexWriter.close();
                if(indexWriter instanceof WriteInfoLoggable)
                    ((WriteInfoLoggable) indexWriter).getIndexWriteInfo(indexWriteInfoList);
            } catch (IOException e) {
                throw new IRException(e);
            }
        }


        logger.debug("##Indexer close {}", workingSegmentInfo);
//        deleteIdSet = dataSourceReader.getDeleteList();
        int deleteCount = 0;
        if(deleteIdSet != null) {
            deleteCount = deleteIdSet.size();
        }

        workingSegmentInfo.setDeleteCount(deleteCount);

        long endTime = System.currentTimeMillis();

        CollectionIndexStatus.IndexStatus indexStatus = new CollectionIndexStatus.IndexStatus(workingSegmentInfo.getDocumentCount(), workingSegmentInfo.getInsertCount(), workingSegmentInfo.getUpdateCount(), deleteCount,
                Formatter.formatDate(new Date(startTime)), Formatter.formatDate(new Date(endTime)), Formatter.getFormatTime(endTime - startTime));

        if(done(workingSegmentInfo, indexStatus)){
            CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
        }else{
            //저장하지 않음.
        }
        return true;
    }

    public IndexWriteInfoList indexWriteInfoList() {
        return indexWriteInfoList;
    }

    protected boolean done(DataInfo.SegmentInfo segmentInfo, CollectionIndexStatus.IndexStatus indexStatus) throws IRException, IndexingStopException {

        int insertCount = segmentInfo.getInsertCount();
        int deleteCount = segmentInfo.getDeleteCount();
        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        File segmentDir = indexFilePaths.file(segmentInfo.getId());

        try {
            if (!stopRequested) {
                if (insertCount <= 0) {
                    // 세그먼트 증가시 segment디렉토리 삭제.
                    logger.debug("# 추가문서가 없으므로, segment를 삭제합니다. {}", segmentDir.getAbsolutePath());
                    FileUtils.deleteDirectory(segmentDir);
                    logger.info("delete segment dir ={}", segmentDir.getAbsolutePath());
                    segmentInfo.resetCountInfo();
                }
                if (deleteCount > 0) {
                    segmentInfo.setDeleteCount(deleteCount);
                }

                if (insertCount <= 0 && deleteCount <= 0) {
                    logger.info("[{}] Indexing Canceled due to no documents.", collectionContext.collectionId());
                    throw new IndexingStopException(collectionContext.collectionId() + " Indexing Canceled due to no documents.");
                }

                collectionHandler.updateCollection(collectionContext, segmentInfo, segmentDir, deleteIdSet);

                //status.xml 업데이트
                collectionContext.updateCollectionStatus(IndexingType.ADD, segmentInfo, startTime, System.currentTimeMillis());
                collectionContext.indexStatus().setAddIndexStatus(indexStatus);

            } else {
                FileUtils.deleteDirectory(segmentDir);
                logger.info("delete segment dir ={}", segmentDir.getAbsolutePath());
                logger.info("[{}] Indexing Canceled due to Stop Requested!", collectionContext.collectionId());
                throw new IndexingStopException(collectionContext.collectionId() + " Indexing Canceled due to Stop Requested");
            }
        } catch (IOException e) {
            throw new IRException(e);
        }

        return true;
    }

}
