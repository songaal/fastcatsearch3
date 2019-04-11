package org.fastcatsearch.job.indexing;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.action.service.indexing.IndexDocumentsAction;
import org.fastcatsearch.http.action.service.indexing.JSONRequestReader;
import org.fastcatsearch.http.action.service.indexing.MapDocument;
import org.fastcatsearch.ir.CollectionDynamicIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.DataJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by swsong on 2016. 1. 14..
 */
public class NodeIndexDocumentFileJob extends DataJob implements Streamable {

    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

    private String collectionId;
    private String documentId;
    private String documents;

    public NodeIndexDocumentFileJob() {
    }

    public NodeIndexDocumentFileJob(String collectionId, String documentId, String documents) {
        this.collectionId = collectionId;
        this.documentId = documentId;
        this.documents = documents;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        try {
            IRService irService = ServiceManager.getInstance().getService(IRService.class);
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
            long startTime = System.currentTimeMillis();
            CollectionDynamicIndexer indexer = null;
            try {
                indexer = new CollectionDynamicIndexer(documentId, collectionHandler);
                JSONRequestReader jsonReader = new JSONRequestReader(documents);
//                List<MapDocument> docList = JSONRequestReader.readMapDocuments(documents);
                MapDocument doc = null;
                while((doc = jsonReader.readAsMapDocument()) != null) {
//                for(MapDocument doc : docList) {
                    String type = String.valueOf(doc.getType());
                    Map<String, Object> sourceMap = doc.getSourceMap();
                    if (type.equals(IndexDocumentsAction.INSERT_TYPE)) {
                        indexer.insertDocument(sourceMap);
                    } else if (type.equals(IndexDocumentsAction.UPDATE_TYPE)) {
                        indexer.updateDocument(sourceMap);
                    } else if (type.equals(IndexDocumentsAction.DELETE_TYPE)) {
                        indexer.deleteDocument(sourceMap);
                    } else {
                        logger.error("Unknown doctype[{}] {}", doc.getType(), sourceMap);
                    }
                }
            } finally {
                DataInfo.SegmentInfo segmentInfo = null;
                if (indexer != null) {
                    segmentInfo = indexer.close();

                    File segmentDir = indexer.getSegmentDir();
                    DeleteIdSet deleteIdSet = indexer.getDeleteIdSet();
                    if(segmentInfo.getDocumentCount() == 0 || segmentInfo.getLiveCount() <= 0) {
                        indexingLogger.info("[{}] Delete segment dir due to no documents = {}, deleteIdSet = {}", collectionHandler.collectionId(), segmentDir.getAbsolutePath(), deleteIdSet.size());
                        FileUtils.deleteDirectory(segmentDir);
                    }

                    //추가문서가 있거나, 또는 삭제문서가 있어야 적용을 한다.
                    int totalLiveDocs = 0;
                    if (segmentInfo.getLiveCount() > 0 || deleteIdSet.size() > 0) {
                        CollectionContext collectionContext = collectionHandler.applyNewSegment(segmentInfo, segmentDir, deleteIdSet);
                        CollectionContextUtil.saveCollectionAfterDynamicIndexing(collectionContext);
                        getJobExecutor().offer(new CacheServiceRestartJob(0));
                        totalLiveDocs = collectionContext.dataInfo().getDocuments() - collectionContext.dataInfo().getDeletes();
                    } else {
                        CollectionContext collectionContext = collectionHandler.collectionContext();
                        totalLiveDocs = collectionContext.dataInfo().getDocuments() - collectionContext.dataInfo().getDeletes();
                    }
                    long elapsed = System.currentTimeMillis() - startTime;

                    indexingLogger.info("[{}] Dynamic Indexing Done. Inserts[{}] Deletes[{}] Elapsed[{}] TotalLive[{}]", collectionId, segmentInfo.getDocumentCount(), segmentInfo.getDeleteCount(), Formatter.getFormatTime(elapsed), totalLiveDocs);
                }
            }
        } catch (Exception e) {
            logger.error(collectionId + " node dynamic index error!", e);
            logger.error("[{}] Dynamic index error documents >>\n{}", collectionId, documents);
        }

        return new JobResult();
    }

    @Override
    public void readFrom(DataInput input) throws IOException {
        collectionId = input.readString();
        documentId = input.readString();
        documents = input.readString();
    }

    @Override
    public void writeTo(DataOutput output) throws IOException {
        output.writeString(collectionId);
        output.writeString(documentId);
        output.writeString(documents);
    }
}
