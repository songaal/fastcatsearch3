package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.indexing.NodeIndexDocumentFileJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.TimeBaseRollingDocumentLogger.LogFileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private StringBuilder remnant; //쪼개진 json의 일부 

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
                    LogFileStatus logFileStatus = fileQueue.peek();

                    if (logFileStatus == null) {
                        //더이상 들어온 파일이 없다면 만들어둔 문서만 보낸다.
                        if (documentsBuilder != null && documentsBuilder.length() > 0) {
                            logger.debug("[{}] sendDocuments2 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
                            return documentsBuilder.toString();
                        } else {
                            return null;
                        }
                    }
                    File file = logFileStatus.getFile();
                    if (!file.exists()) {
                        //존재하지 않는다면 누군가 지운것임. 다음 파일을 확인한다.
                        fileQueue.poll(); //버린다
                        continue;
                    }
                    currentReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    logger.debug("Open indexlog {}", file.getName());
                    currentFileStatus = logFileStatus;
                }

                int tryCount = 0;
                while (!isCanceled) {
                    String docRequest = currentReader.readLine();
                    if (docRequest == null || docRequest.length() == 0) {
                        //쓰고 있는 중인지, 닫힌건지 판단필요.
                        if (currentFileStatus.isClosed()) {
                            //Writer가 파일을 닫았으므로, reader도 닫는다.
                            currentReader.close();
                            currentReader = null;
                            //다쓴 파일은 삭제한다.
                            toBeDeleted.add(currentFileStatus.getFile());
                            fileQueue.poll(); //뽑아서 버린다
                            currentFileStatus = null;
                            //다음 파일을 확인한다.
                            break;
                        } else {
                            //파일이 닫힐때 까지 무한 대기 할수 없으므로, 1초이내 안들어오면 보낸다.
                            logger.trace("[{}] reading retry..", collectionId);
                            try {
                                tryCount++;
                                Thread.sleep(250);
                            } catch (InterruptedException ignore) {
                            }

                            if (tryCount >= 4) {
                                if (documentsBuilder != null && documentsBuilder.length() > 0) {
                                    logger.debug("[{}] sendDocuments3 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
                                    return documentsBuilder.toString();
                                }
                            }
                        }
                    } else {

                        //Logger에서 bufferedWriter.flush를 해도 실제로 fileSystem에서 기록되는 것은 보장되지 않으므로 json이 끊어질 수 있다.
                        //그렇기 때문에 이어 붙여준다.
                        char lastChar = docRequest.charAt(docRequest.length() - 1);
                        if (lastChar != '}') {
                            if (remnant != null) {
                                remnant.append(docRequest);
                            } else {
                                remnant = new StringBuilder(docRequest);
                            }
                            //닫히지 않은 JSON은 다시 읽어서 붙여준다.
                            continue;
                        } else {
                            if (remnant != null) {
                                remnant.append(docRequest);
                                docRequest = remnant.toString();
                                remnant = null;
                            }
                        }

                        if (documentsBuilder == null) {
                            documentsBuilder = new StringBuilder();
                        }
                        documentsBuilder.append(docRequest).append('\n');
                        totalSize += (docRequest.length() + 1) * 2;
                        count++;
                        //0으로 만들어 주니 너무 많이 길어진다.
//                        tryCount = 0;
                        //보낼 사이즈가 찼다면..
                        if ((indexFileMaxSize > 0 && totalSize >= indexFileMaxSize) || (indexFileMaxCount > 0 && count >= indexFileMaxCount)) {
                            logger.debug("[{}] sendDocuments1 count[{}] size[{}]", collectionId, count, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize));
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
                for (File f : toBeDeletedFiles) {
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
