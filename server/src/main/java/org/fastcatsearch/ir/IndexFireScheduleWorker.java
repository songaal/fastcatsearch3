package org.fastcatsearch.ir;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.indexing.NodeIndexDocumentFileJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.TimeBaseRollingDocumentLogger;
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

    private BlockingQueue<TimeBaseRollingDocumentLogger.LogFileStatus> fileQueue;

    private int indexFileMinSize;

    public IndexFireScheduleWorker(String collectionId, BlockingQueue<TimeBaseRollingDocumentLogger.LogFileStatus> fileQueue) {
        super("IndexFireScheduler-" + collectionId);
        this.collectionId = collectionId;
        this.fileQueue = fileQueue;
    }

    @Override
    public void run() {
        List<File> fileList = new ArrayList<File>();
        long totalSize = 0;

        int count = 0;
        StringBuilder documentsBuilder = new StringBuilder();

        while (true) {
            TimeBaseRollingDocumentLogger.LogFileStatus logFileStatus = fileQueue.peek();
            if (logFileStatus != null && logFileStatus.getFile().exists()) {

                BufferedReader reader = null;
                File file = logFileStatus.getFile();
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    while (true) {
                        String doc = reader.readLine();
                        if (doc == null) {
                            //쓰고 있는 중인지, 닫힌건지 판단필요.
                            if (logFileStatus.isClosed()) {
                                break;
                            }
                            System.out.println("reading retry..");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ignore) {
                            }
                        } else {
                            documentsBuilder.append(doc);
                            totalSize += doc.length() * 2;
                            count++;
                            System.out.println("read " + doc);

                            if (totalSize >= indexFileMinSize) {
                                break;
                            }

                            if (count > 20000) {
                                break;
                            }
                        }
                    }


                } catch (IOException e) {
                    logger.error("", e);
                }

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
            logger.debug("[{}] Indexing size[{}] count[{}] remnant[{}] >> {}", collectionId, org.fastcatsearch.ir.util.Formatter.getFormatSize(totalSize), fileList.size(), dataLogger.getQueueSize(), fileNames);

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
    }
}
}
