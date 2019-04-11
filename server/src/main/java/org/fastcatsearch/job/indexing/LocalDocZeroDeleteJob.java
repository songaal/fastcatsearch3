package org.fastcatsearch.job.indexing;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionMergeIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 로컬 노드에서 색인머징을 수행하는 작업.
 */
public class LocalDocZeroDeleteJob extends Job {

    protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

    private String collectionId;
    private Set<String> zeroSegmentIdSet;

    public LocalDocZeroDeleteJob() {
    }

    public LocalDocZeroDeleteJob(String collectionId, Set<String>zeroSegmentIdSet) {
        this.collectionId = collectionId;
        this.zeroSegmentIdSet = zeroSegmentIdSet;
    }

    @Override
    public JobResult doRun() throws FastcatSearchException {
        try {
            IRService irService = ServiceManager.getInstance().getService(IRService.class);
            CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
//            synchronized (collectionHandler) {
                CollectionContext collectionContext = collectionHandler.collectionContext();
                collectionHandler.removeZeroSegment(zeroSegmentIdSet);
                CollectionContextUtil.saveCollectionAfterDynamicIndexing(collectionContext);
//            }
            return new JobResult(true);
        } catch (Throwable e) {
            logger.error("", e);
            throw new FastcatSearchException("ERR-00525", e);
        }
    }
}
