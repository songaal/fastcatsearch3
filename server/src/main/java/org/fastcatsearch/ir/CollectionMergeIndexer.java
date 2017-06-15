package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.reader.*;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.indexing.IndexingStopException;
import org.fastcatsearch.util.FilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 컬렉션의 세그먼트 머징을 수행하는 indexer.
 * */
public class CollectionMergeIndexer {
    protected static final Logger logger = LoggerFactory.getLogger(CollectionMergeIndexer.class);
    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
    protected CollectionContext collectionContext;
    protected AnalyzerPoolManager analyzerPoolManager;

    private File segmentDir;

    protected DeleteIdSet deleteIdSet; //삭제문서리스트.

    protected SegmentWriter indexWriter;
    protected DataInfo.SegmentInfo segmentInfo;

    private AbstractDataSourceReader dataSourceReader;

    private Schema schema;
    private long startTime;
    private long lapTime;

    private int count;

	public CollectionMergeIndexer(String documentId, CollectionHandler collectionHandler, File[] segmentDirs) throws IRException, IOException {
        this.collectionContext = collectionHandler.collectionContext();
        this.analyzerPoolManager = collectionHandler.analyzerPoolManager();
		//머징색인시는 현재 스키마를 그대로 사용한다.
        this.schema = collectionContext.schema();
        this.segmentInfo = new DataInfo.SegmentInfo(documentId);
        /*
        * 세그먼트 디렉토리가 미리존재한다면 삭제.
        * */
        FilePaths indexFilePaths = collectionContext.indexFilePaths();
        segmentDir = indexFilePaths.file(segmentInfo.getId());
        logger.debug("New Segment Dir = {}", segmentDir.getAbsolutePath());
        try {
            FileUtils.deleteDirectory(segmentDir);
        } catch (IOException e) {
            throw new IRException(e);
        }
        IndexConfig indexConfig = collectionContext.indexConfig();
        indexWriter = new SegmentWriter(schema, segmentDir, segmentInfo, indexConfig, analyzerPoolManager, null);
        deleteIdSet = new DeleteIdSet();

        dataSourceReader = new SegmentSourceReader(segmentDirs, schema.schemaSetting());
        dataSourceReader.init();
	}

	public File getSegmentDir() {
        return segmentDir;
    }

    public void doIndexing() throws IRException, IOException {
        startTime = System.currentTimeMillis();
        lapTime = startTime;
        while (dataSourceReader.hasNext()) {
            Document document = dataSourceReader.nextDocument();
//			logger.debug("doc >> {}", document);
            addDocument(document);
        }
    }

    public void addDocument(Document document) throws IRException, IOException{
        indexWriter.addDocument(document);
        count++;
        if (count % 10000 == 0) {
            indexingLogger.info(
                    "[{}] Merge Indexing {} ... lap = {} ms, elapsed = {}, mem = {}",
                    collectionContext.collectionId(), count, System.currentTimeMillis() - lapTime,
                    Formatter.getFormatTime(System.currentTimeMillis() - startTime),
                    Formatter.getFormatSize(Runtime.getRuntime().totalMemory()));
            lapTime = System.currentTimeMillis();
        }
    }

    public DataInfo.SegmentInfo close() throws IRException, SettingException, IndexingStopException {

        if (indexWriter != null) {
            try {
                segmentInfo = indexWriter.close();
                //머징된 세그먼트 임을 표시한다.
                segmentInfo.setMerged(true);
                logger.debug("##Indexer close {}", segmentInfo);
            } catch (IOException e) {
                throw new IRException(e);
            }
        }

        return segmentInfo;
    }
	
}

