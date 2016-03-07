package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.indexing.NodeIndexDocumentFileJob;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.TimeBaseRollingDocumentLogger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by swsong on 2016. 1. 13..
 */
public class DynamicIndexModule extends AbstractModule {
    private String collectionId;

    private TimeBaseRollingDocumentLogger dataLogger;
    private File dir;
    private File stopIndexingFlagFile;
    private int flushPeriodInSeconds;
    private int rollingPeriodInSeconds;
    private int indexFileMaxCount;
    private long indexFileMaxSize;
    private long mergePeriod;
    private long indexingPeriod;

    private IndexMergeScheduleWorker indexMergeScheduleWorker;
    private IndexFireScheduleWorker indexFireScheduleWorker;


    public DynamicIndexModule(Environment environment, Settings settings, String collectionId) {
        super(environment, settings);
        this.collectionId = collectionId;
        dir = environment.filePaths().collectionFilePaths(collectionId).file("indexlog");
        stopIndexingFlagFile = new File(environment.filePaths().collectionFilePaths(collectionId).file(), "indexlog.stop");
        flushPeriodInSeconds = settings.getInt("indexing.dynamic.log_flush_period_SEC", 1); //1초마다 flush.
        rollingPeriodInSeconds = settings.getInt("indexing.dynamic.log_rolling_period_SEC", 30); //30초마다 파일변경.
        indexFileMaxCount = settings.getInt("indexing.dynamic.max_log_count", 10000);//최소 1만개 를 모아서 보낸다
        indexFileMaxSize = settings.getLong("indexing.dynamic.max_log_size_MB", 20L) * 1000 * 1000; //최소 20MB를 모아서 보낸다.
        mergePeriod = settings.getInt("indexing.dynamic.merge_period_SEC", 5) * 1000; //5초마다.
        indexingPeriod = settings.getInt("indexing.dynamic.indexing_period_SEC", 1) * 1000; //1초마다.
        logger.debug("[{}] DynamicIndexModule flushPeriodInSeconds[{}] indexFileMaxCount[{}] indexFileMaxSize[{}] mergePeriod[{}] indexingPeriod[{}]",
                collectionId, flushPeriodInSeconds, indexFileMaxCount, indexFileMaxSize, mergePeriod, indexingPeriod);

        indexMergeScheduleWorker = new IndexMergeScheduleWorker(collectionId, mergePeriod);
        indexMergeScheduleWorker.setDaemon(true);
    }

    @Override
    protected boolean doLoad() throws ModuleException {
        dataLogger = new TimeBaseRollingDocumentLogger(dir, flushPeriodInSeconds, rollingPeriodInSeconds);
        //stop 파일이 없어야만 시작한다.
        if(!stopIndexingFlagFile.exists()) {
            startIndexingSchedule();
        }
        indexMergeScheduleWorker.start();
        logger.info("[{}] Index Merger start scheduling!", collectionId);
        logger.info("[{}] To be indexed files = {}", collectionId, dataLogger.getQueueSize());
        return true;
    }

    @Override
    protected boolean doUnload() throws ModuleException {
        if(dataLogger != null) {
            dataLogger.close();
        }

        if(indexFireScheduleWorker != null) {
            indexFireScheduleWorker.requestCancel();
        }
        indexMergeScheduleWorker.requestCancel();

        return true;
    }

    public boolean isIndexingScheduled() {
        return indexFireScheduleWorker != null && !stopIndexingFlagFile.exists();

    }

    public boolean stopIndexingSchedule() {
        if(indexFireScheduleWorker != null) {
            indexFireScheduleWorker.requestCancel();
            indexFireScheduleWorker = null;
        }

        try {
            stopIndexingFlagFile.createNewFile();
        } catch (IOException e) {
            logger.error("", e);
        }
        return true;
    }

    public boolean startIndexingSchedule() {
        if(indexFireScheduleWorker == null) {
            indexFireScheduleWorker = new IndexFireScheduleWorker(collectionId, dataLogger.getFileQueue(), indexingPeriod, indexFileMaxCount, indexFileMaxSize);
            indexFireScheduleWorker.setDaemon(true);
            indexFireScheduleWorker.start();
            stopIndexingFlagFile.delete();
            return true;
        } else {
            logger.warn("[{}] Dynamic Indexing is running. Stop a indexing first before starting.", collectionId);
        }
        return false;
    }

    public boolean insertDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("I " + json);
        }
        return true;
    }
    public boolean updateDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("U " + json);
        }
        return true;
    }
    public boolean deleteDocument(List<String> jsonList) {
        for(String json : jsonList) {
            dataLogger.log("D " + json);
        }
        return true;
    }
}
