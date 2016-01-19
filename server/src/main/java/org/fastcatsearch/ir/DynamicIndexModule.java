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
import org.fastcatsearch.job.indexing.NodeIndexFileDocumentJob;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.LimitTimeSizeLogger;

import java.io.File;
import java.util.*;

/**
 * Created by swsong on 2016. 1. 13..
 */
public class DynamicIndexModule extends AbstractModule {
    private String collectionId;

    private LimitTimeSizeLogger dataLogger;
    private Timer timer;
    private int bulkSize;
    private File dir;
    private int flushPeriod = 2;
    public DynamicIndexModule(Environment environment, Settings settings, String collectionId, int bulkSize) {
        super(environment, settings);
        this.collectionId = collectionId;
        this.bulkSize = bulkSize;
        dir = environment.filePaths().collectionFilePaths(collectionId).file("indexlog");
    }

    class IndexFireTask extends TimerTask {

        @Override
        public void run() {
            File file = dataLogger.pollFile();
            if(file != null) {
                //file 을 증분색인하도록 요청한다.
                logger.debug("Found file to be indexed > {}", file.getAbsolutePath());

                String documentId = file.getName();
                String documents = null;
                try {
                    documents = FileUtils.readFileToString(file, "utf-8");
                    NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
                    IRService irService = ServiceManager.getInstance().getService(IRService.class);
                    CollectionContext collectionContext = irService.collectionContext(collectionId);

                    Set<String> nodeSet = new HashSet<String>();
                    nodeSet.addAll(collectionContext.collectionConfig().getDataNodeList());
                    nodeSet.add(collectionContext.collectionConfig().getIndexNode());
                    nodeSet.add(nodeService.getMasterNode().id());
                    List<String> nodeIdList = new ArrayList<String>(nodeSet);
                    List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(nodeIdList));

                    NodeIndexFileDocumentJob indexFileDocumentJob = new NodeIndexFileDocumentJob(collectionId, documentId, documents);
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

    @Override
    protected boolean doLoad() throws ModuleException {
        timer = new Timer();
        timer.schedule(new IndexFireTask(), 1000, 1000);
        dataLogger = new LimitTimeSizeLogger(dir, bulkSize, flushPeriod);
        return true;
    }

    @Override
    protected boolean doUnload() throws ModuleException {
        timer.cancel();
        return true;
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
