package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.util.*;
import org.fastcatsearch.job.indexing.NodeIndexDocumentFileJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.TimeBaseRollingDocumentLogger;
import org.fastcatsearch.util.TimeBaseRollingDocumentLogger.LogFileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by swsong on 2016. 3. 6..
 */
public class IndexFireScheduleWorker extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(IndexFireScheduleWorker.class);
    private String collectionId;
    private boolean isCanceled;
    private BlockingQueue<LogFileStatus> fileQueue;
    private int indexFileMaxCount; //동적색인 전달시 최대 문서집합 갯수.
    private long indexFileMaxSize; //동적색인 전달시 최대 문서집합 사이즈
    private long indexingPeriod;
    private BufferedReader currentReader;
    private LogFileStatus currentFileStatus;

    public IndexFireScheduleWorker(String collectionId, BlockingQueue<LogFileStatus> fileQueue, long indexingPeriod, int indexFileMaxCount, long indexFileMaxSize) {
        super("IndexFireScheduler-" + collectionId);
        this.collectionId = collectionId;
        this.fileQueue = fileQueue;
        this.indexingPeriod = indexingPeriod;
        this.indexFileMaxCount = indexFileMaxCount;
        this.indexFileMaxSize = indexFileMaxSize;
    }

    public void requestCancel() {
        this.interrupt();
        isCanceled = true;
        if (currentReader != null) {
            try {
                currentReader.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public String makeDocuments(List<File> toBeDeleted) {

        long totalSize = 0;
        int count = 0;
        StringBuilder documentsBuilder = null;

        try {
            while (!isCanceled) {

                if (currentReader == null) {
                    LogFileStatus logFileStatus = fileQueue.poll();

                    if (logFileStatus == null) {
                        //더이상 들어온 파일이 없다면 만들어둔 문서만 보낸다.
                        if (documentsBuilder != null && documentsBuilder.length() > 0) {
                            logger.info("[{}] sendDocuments2 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
                            return documentsBuilder.toString();
                        } else {
                            return null;
                        }
                    }
                    File file = logFileStatus.getFile();
                    if (!file.exists()) {
                        //존재하지 않는다면 누군가 지운것임. 다음 파일을 확인한다.
                        continue;
                    }
                    currentReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    logger.info("Open indexlog {}", file.getName());
                    currentFileStatus = logFileStatus;
                }

                int tryCount = 0;
                while (!isCanceled) {
                    String docRequest = currentReader.readLine();
                    if (docRequest == null) {
                        //쓰고 있는 중인지, 닫힌건지 판단필요.
                        if (currentFileStatus.isClosed()) {
                            //Writer가 파일을 닫았으므로, reader도 닫는다.
                            currentReader.close();
                            currentReader = null;
                            //다쓴 파일은 삭제한다.
                            toBeDeleted.add(currentFileStatus.getFile());
                            currentFileStatus = null;
                            //다음 파일을 확인한다.
                            break;
                        } else {
                            //파일이 닫힐때 까지 무한 대기 할수 없으므로, 2초이내 안들어오면 보낸다.
                            logger.info("[{}] reading retry..", collectionId);
                            try {
                                tryCount++;
                                Thread.sleep(500);
                            } catch (InterruptedException ignore) {
                            }

                            if(tryCount >= 4) {
                                if (documentsBuilder != null && documentsBuilder.length() > 0) {
                                    logger.info("[{}] sendDocuments3 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
                                    return documentsBuilder.toString();
                                }
                            }
                        }
                    } else {
                        if (documentsBuilder == null) {
                            documentsBuilder = new StringBuilder();
                        }
                        documentsBuilder.append(docRequest);
                        logger.info(">> {}", docRequest);
                        totalSize += docRequest.length() * 2;
                        count++;
                        tryCount = 0;
                        //보낼 사이즈가 찼다면..
                        if ((indexFileMaxSize > 0 && totalSize >= indexFileMaxSize) || (indexFileMaxCount > 0 && count >= indexFileMaxCount)) {
                            logger.info("[{}] sendDocuments1 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
                            return documentsBuilder.toString();
                        }
                    }
                }//while
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        //IO 에러시에는 null을 리턴한다.
        return null;
    }

    @Override
    public void run() {

        logger.info("[{}] {} is started!", collectionId, getClass().getName());
        //절대죽어서는 아니되오.
        List<File> toBeDeletedFiles = new ArrayList<File>();
        while (!isCanceled) {
            try {
                toBeDeletedFiles.clear();
                String documents = makeDocuments(toBeDeletedFiles);
                if (documents != null) {
                    sendDocuments(documents);
                }
                for(File f : toBeDeletedFiles) {
                    //전송이 잘 되었다면 실제로 지운다.
                    FileUtils.deleteQuietly(f);
                }
            } catch (Throwable t) {
                logger.error("", t);
            }
            try {
                Thread.sleep(indexingPeriod);
            } catch (InterruptedException e) {
                //종료..
            }
        }

        logger.info("[{}] {} is terminated!", collectionId, getClass().getName());
    }

    private void sendDocuments(String documents) {
        NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
        IRService irService = ServiceManager.getInstance().getService(IRService.class);
        CollectionContext collectionContext = irService.collectionContext(collectionId);

        Set<String> nodeSet = new HashSet<String>();
        nodeSet.addAll(collectionContext.collectionConfig().getDataNodeList());
        nodeSet.add(collectionContext.collectionConfig().getIndexNode());
        List<String> nodeIdList = new ArrayList<String>(nodeSet);
        List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(nodeIdList));

        String documentId = String.valueOf(System.nanoTime());
        NodeIndexDocumentFileJob indexFileDocumentJob = new NodeIndexDocumentFileJob(collectionId, documentId, documents);
        long st = System.nanoTime();
        NodeJobResult[] nodeResultList = ClusterUtils.sendJobToNodeList(indexFileDocumentJob, nodeService, nodeList, true);
        logger.debug("[{}] Send Indexing files. time : {}ms", collectionId, (System.nanoTime() - st) / 1000000);
        //여기서 문서전송이 모두 끝날때 까지 대기해야 문서순서가 바뀌지 않음.
        for (NodeJobResult result : nodeResultList) {
            logger.debug("[{}] Indexing request done > {} : Node {} > {}", collectionId, documentId, result.node().id(), result.result());
        }
    }

}
