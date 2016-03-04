package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.indexing.NodeIndexDocumentFileJob;
import org.fastcatsearch.job.indexing.NodeIndexMergingJob;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.LimitTimeSizeLogger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by swsong on 2016. 1. 13..
 */
public class DynamicIndexModule extends AbstractModule {
    private String collectionId;

    private LimitTimeSizeLogger dataLogger;
    private Timer indexTimer;
    private File dir;
    private File stopIndexingFlagFile;
    private int flushPeriodInSeconds;
    private long indexFileMinSize;
    private int indexFileMinCount;
    private long mergePeriod;
    private long indexingPeriod;

    private Semaphore indexingMutex = new Semaphore(1);

    private IndexMergeScheduleWorker indexMergeScheduleWorker;


    public DynamicIndexModule(Environment environment, Settings settings, String collectionId, ScheduledExecutorService scheduleService) {
        super(environment, settings);
        this.collectionId = collectionId;
        dir = environment.filePaths().collectionFilePaths(collectionId).file("indexlog");
        stopIndexingFlagFile = new File(environment.filePaths().collectionFilePaths(collectionId).file(), "indexlog.stop");
        flushPeriodInSeconds = settings.getInt("indexing.dynamic.log_flush_period_SEC", 1); //1초마다.
        indexFileMinSize = settings.getLong("indexing.dynamic.min_log_size_MB", 10L) * 1000 * 1000; //최소 10MB를 모아서 보낸다.
        indexFileMinCount = settings.getInt("indexing.dynamic.min_log_count", 20000);//최소 2만개 를 모아서 보낸다
        mergePeriod = settings.getInt("indexing.dynamic.merge_period_SEC", 5) * 1000; //5초마다.
        indexingPeriod = settings.getInt("indexing.dynamic.indexing_period_SEC", 1) * 1000; //1초마다.
        logger.debug("DynamicIndexModule flushPeriodInSeconds[{}] indexFileMinSize[{}] mergePeriod[{}] indexingPeriod[{}]",
                flushPeriodInSeconds, indexFileMinSize, mergePeriod, indexingPeriod);

        indexMergeScheduleWorker = new IndexMergeScheduleWorker(collectionId, mergePeriod);
    }

    class IndexFireTask extends TimerTask {

        @Override
        public void run() {
            if(indexingMutex.tryAcquire()) {
                try {
                    List<File> fileList = new ArrayList<File>();
                    long totalSize = 0;
                    while (true) {
                        File file = dataLogger.pollFile();
                        if (file != null && file.exists()) {
                            //존재하면 추가.
                            fileList.add(file);
                            totalSize += file.length();
                            if (totalSize >= indexFileMinSize) {
                                break;
                            }
                        } else {
                            if (fileList.size() > 0) {
                                //몇개라도 존재하면 계속진행.
                                break;
                            } else {
                                //없으면 리턴.
                                return;
                            }
                        }
                    }

                    if (fileList.size() > 0) {
                        List<String> fileNames = new ArrayList<String>();
                        for (File f : fileList) {
                            fileNames.add(f.getName());
                        }
                        //file 을 증분색인하도록 요청한다.
                        logger.debug("[{}] Indexing[{}] Remnants[{}] >> {}", fileList.size(), collectionId, dataLogger.getQueueSize(), fileNames);

                        String documentId = null;
                        try {
                            String documents = null;

                            //TODO 증분색인시 flush 주기가 길면, 다수의 문서가 들어와서 파일이 몇백 MB가 될수 있으므로, OOM우려됨.

                            // TODO 파일을 일정크기로 잘라 읽어서 여러번 전달하도록 처리필요.
                            // TODO 필요하면 gzip으로 압축해도 될듯..

                            if (fileList.size() == 1) {
                                File f = fileList.get(0);
                                documentId = f.getName();
                                documents = FileUtils.readFileToString(f, "utf-8");
                            } else {
                                StringBuffer documentsBuffer = new StringBuffer();
                                documentId = fileList.get(0).getName() + "_" + fileList.get(fileList.size() - 1).getName();
                                for (File f : fileList) {
                                    documentsBuffer.append(FileUtils.readFileToString(f, "utf-8"));
                                }
                                documents = documentsBuffer.toString();
                            }

                            NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
                            IRService irService = ServiceManager.getInstance().getService(IRService.class);
                            CollectionContext collectionContext = irService.collectionContext(collectionId);

                            Set<String> nodeSet = new HashSet<String>();
                            nodeSet.addAll(collectionContext.collectionConfig().getDataNodeList());
                            nodeSet.add(collectionContext.collectionConfig().getIndexNode());
                            List<String> nodeIdList = new ArrayList<String>(nodeSet);
                            List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(nodeIdList));

                            NodeIndexDocumentFileJob indexFileDocumentJob = new NodeIndexDocumentFileJob(collectionId, documentId, documents);
                            long st = System.nanoTime();
                            NodeJobResult[] nodeResultList = ClusterUtils.sendJobToNodeList(indexFileDocumentJob, nodeService, nodeList, true);
                            logger.debug("[{}] Index files send time : {}ms", collectionId, (System.nanoTime() - st) / 1000000);
                            //여기서 색인이 끝날때 까지 블록킹해야 다음색인이 동시에 돌지 않게됨.
                            for (NodeJobResult result : nodeResultList) {
                                logger.debug("[{}] Index files {} : Node {} > {}", collectionId, fileNames, result.node().id(), result.result());
                            }
                            for (File f : fileList) {
                                FileUtils.deleteQuietly(f);
                            }
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                } finally {
                    indexingMutex.release();
                }
            }
        }
    }

    @Override
    protected boolean doLoad() throws ModuleException {
        dataLogger = new LimitTimeSizeLogger(dir, flushPeriodInSeconds);
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
        if(indexTimer != null) {
            indexTimer.cancel();
        }
        indexMergeScheduleWorker.requestCancel();
        if(dataLogger != null) {
            dataLogger.close();
        }
        return true;
    }

    public boolean isIndexingScheduled() {
        return indexTimer != null && !stopIndexingFlagFile.exists();

    }

    public boolean stopIndexingSchedule() {
        if(indexTimer != null) {
            indexTimer.cancel();
            indexTimer = null;
        }

        try {
            stopIndexingFlagFile.createNewFile();
        } catch (IOException e) {
            logger.error("", e);
        }
        return true;
    }

    public boolean startIndexingSchedule() {
        if(indexTimer == null) {
            indexTimer = new Timer("DynamicIndexTimer", true);
            TimerTask indexFireTask = new IndexFireTask();
            indexTimer.schedule(indexFireTask, 5000, indexingPeriod);
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
