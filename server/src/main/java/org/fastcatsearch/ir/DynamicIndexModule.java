package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.CollectionContext;
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

/**
 * Created by swsong on 2016. 1. 13..
 */
public class DynamicIndexModule extends AbstractModule {
    private String collectionId;

    private LimitTimeSizeLogger dataLogger;
    private Timer indexTimer;
    private Timer mergeTimer;
    private int bulkSize;
    private File dir;
    private File stopIndexingFlagFile;
    private int flushPeriod = 5; //5초.

    public DynamicIndexModule(Environment environment, Settings settings, String collectionId, int bulkSize) {
        super(environment, settings);
        this.collectionId = collectionId;
        this.bulkSize = bulkSize;
        dir = environment.filePaths().collectionFilePaths(collectionId).file("indexlog");
        stopIndexingFlagFile = new File(environment.filePaths().collectionFilePaths(collectionId).file(), "indexlog.stop");
    }

    class IndexFireTask extends TimerTask {

        @Override
        public void run() {

            //TODO 10만건이 될때까지 추가하여 가져온다


            File file = dataLogger.pollFile();
            if(file != null && file.exists()) {
                //file 을 증분색인하도록 요청한다.
                logger.info("Found file to be indexed among {} files > {}", dataLogger.getQueueSize(), file.getAbsolutePath());

                String documentId = file.getName();
                try {
                    String documents = FileUtils.readFileToString(file, "utf-8");
                    NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
                    IRService irService = ServiceManager.getInstance().getService(IRService.class);
                    CollectionContext collectionContext = irService.collectionContext(collectionId);

                    Set<String> nodeSet = new HashSet<String>();
                    nodeSet.addAll(collectionContext.collectionConfig().getDataNodeList());
                    nodeSet.add(collectionContext.collectionConfig().getIndexNode());
//                    nodeSet.add(nodeService.getMasterNode().id());
                    List<String> nodeIdList = new ArrayList<String>(nodeSet);
                    List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(nodeIdList));

                    NodeIndexDocumentFileJob indexFileDocumentJob = new NodeIndexDocumentFileJob(collectionId, documentId, documents);
                    NodeJobResult[] nodeResultList = ClusterUtils.sendJobToNodeList(indexFileDocumentJob, nodeService, nodeList, true);
                    //여기서 색인이 끝날때 까지 블록킹해야 다음색인이 동시에 돌지 않게됨.
                    for(NodeJobResult result : nodeResultList) {
                        logger.debug("Index file {} : Node {} > {}", file.getName(), result.node().id(), result.result());
                    }
                    FileUtils.deleteQuietly(file);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
    }

    class IndexMergeTask extends TimerTask {

        private int name = hashCode();
        @Override
        public void run() {

            String documentId = String.valueOf(System.nanoTime());
//            logger.debug("MergeCheckTask-{} col[{}] at {}", name, collectionId, documentId);
            try {
                JobService jobService = ServiceManager.getInstance().getService(JobService.class);
                ResultFuture resultFuture = jobService.offer(new NodeIndexMergingJob(collectionId, documentId));
                Object result = resultFuture.take();
                if(result instanceof Boolean && ((Boolean) result).booleanValue()) {
//                    logger.debug("Merging id {} : Node {}", documentId, environment.myNodeId());
                } else {
                    //무시.
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }

    @Override
    protected boolean doLoad() throws ModuleException {
        mergeTimer = new Timer(true);
        //stop 파일이 없어야만 시작한다.
        if(!stopIndexingFlagFile.exists()) {
            startIndexingSchedule();
        }
        TimerTask indexMergeTask = new IndexMergeTask();
        mergeTimer.schedule(indexMergeTask, 5000, 5000);
        logger.info("[{}] Index Merger start scheduling! timer[{}] task[{}]", mergeTimer.hashCode(), indexMergeTask.hashCode());
        dataLogger = new LimitTimeSizeLogger(dir, bulkSize, flushPeriod);
        logger.info("[{}] To be indexed files = {}", collectionId, dataLogger.getQueueSize());
        return true;
    }

    @Override
    protected boolean doUnload() throws ModuleException {
        if(indexTimer != null) {
            indexTimer.cancel();
        }
        mergeTimer.cancel();
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
            indexTimer = new Timer(true);
            TimerTask indexFireTask = new IndexFireTask();
            indexTimer.schedule(indexFireTask, 5000, 1000);
            return stopIndexingFlagFile.delete();
        } else {
            logger.info("Dynamic Indexing is running. Stop a indexing first before starting.");
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
