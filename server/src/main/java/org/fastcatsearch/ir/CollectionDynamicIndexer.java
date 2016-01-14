package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.DefaultDataSourceReader;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.index.*;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.search.CollectionSearcher;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    protected DeleteIdSet deleteIdSet; //삭제문서리스트. 외부에서 source reader를 통해 셋팅된다.

    protected IndexWritable indexWriter;
    protected DataInfo.SegmentInfo workingSegmentInfo;
    protected int insertCount;
    protected int updateCount;
    protected int deleteCount;

    protected boolean stopRequested;

    private CollectionHandler collectionHandler;
    private DefaultDataSourceReader documentFactory;

    private Schema schema;
    private List<String> pkList;
    private CollectionSearcher collectionSearcher;

    public CollectionDynamicIndexer(CollectionHandler collectionHandler) throws IRException {
        this.collectionContext = collectionHandler.collectionContext();
        this.analyzerPoolManager = collectionHandler.analyzerPoolManager();
        this.schema = collectionContext.schema();
        init();
    }

    public DataInfo.SegmentInfo getSegmentInfo() {
        return workingSegmentInfo;
    }

    private void init() throws IRException {
        //PK를 확인한다.

        pkList =  new ArrayList<String>();
        for(RefSetting refSetting : schema.schemaSetting().getPrimaryKeySetting().getFieldList()) {
            pkList.add(refSetting.getRef().toUpperCase());
        }

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

        indexWriter = new SegmentWriter(schema, segmentDir, workingSegmentInfo, indexConfig, analyzerPoolManager, null);

        documentFactory = new DefaultDataSourceReader(collectionHandler.schema().schemaSetting());

        startTime = System.currentTimeMillis();
    }

    public void insertDocument(Map<String, Object> source) throws IRException, IOException {
        Document document = documentFactory.createDocument(source);
        indexWriter.addDocument(document);
        insertCount++;
        logger.debug("Insert doc > {}", source);
    }

    public void updateDocument(Map<String, Object> source) throws IRException, IOException {

        //1. pk를 뽑아내어 내부검색으로 이전 문서를 가져온다.
        StringBuffer pkSb = new StringBuffer();
        for(String pkId : pkList) {
            Object o = source.get(pkId);
            if(o != null) {
                if(pkSb.length() > 0) {
                    pkSb.append(";");
                }
                pkSb.append(o.toString());
            } else {
                throw new IRException("Cannot find primary key : " + pkId);
            }
        }
        String pkValue = pkSb.toString();
        if(collectionSearcher == null) {
            collectionSearcher = new CollectionSearcher(collectionHandler);
        }
        Document document = collectionSearcher.searchPk(pkValue);

        //2. 들어온 문서에서 각 필드를 업데이트 한다.
        for(Map.Entry<String, Object> entry : source.entrySet()) {
            String fieldId = entry.getKey().toLowerCase();
            Object data = entry.getValue();
            Integer idx = schema.fieldSequenceMap().get(fieldId);
            FieldSetting fs = schema.fieldSettingMap().get(fieldId);
            if (idx == null) {

                //존재하지 않음.
            } else {
                //null이면 공백문자로 치환.
                if (data == null) {
                    data = "";
                } else if (data instanceof String) {
                    data = ((String) data).trim();
                }

//				logger.debug("Get {} : {}", key, data);
                String multiValueDelimiter = fs.getMultiValueDelimiter();
                Field field = fs.createIndexableField(data, multiValueDelimiter);
                //교체.
                document.set(idx, field);
            }
        }

        indexWriter.addDocument(document);
        updateCount++;

        logger.debug("Update {} doc > {}", pkValue, source);

    }
    public void deleteDocument(Map<String, Object> source) throws IRException, IOException {

        //TODO 1. PK만 뽑아내어 현재 들어온 문서중에서 삭제후보가 있는지 찾아 현재 delete.set에 넣어준다.
        //1. pk를 뽑아내어 내부검색으로 이전 문서를 가져온다.
        StringBuffer pkSb = new StringBuffer();
        for(String pkId : pkList) {
            Object o = source.get(pkId);
            if(o != null) {
                if(pkSb.length() > 0) {
                    pkSb.append(";");
                }
                pkSb.append(o.toString());
            } else {
                throw new IRException("Cannot find primary key : " + pkId);
            }
        }
        String pkValue = pkSb.toString();
        indexWriter.deleteDocument(pkValue);


        //TODO 2. 삭제pk만 기록해 놓은 delete.req 파일을 만들어 놓는다.

        deleteCount++;

        logger.debug("Delete doc > {}", source);

    }

    public boolean close() throws IRException, SettingException, IndexingStopException {

//		RevisionInfo revisionInfo = workingSegmentInfo.getRevisionInfo();
        if (indexWriter != null) {
            try {
                indexWriter.close();
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
