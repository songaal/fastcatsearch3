package org.fastcatsearch.task;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFileTransfer {

	private static Logger logger = LoggerFactory.getLogger(IndexFileTransfer.class);

	private Environment environment;

	public IndexFileTransfer(Environment environment) {
		this.environment = environment;
	}

	// 세그먼트를 전송한다.
	public boolean[] transferDirectory(File directory, NodeService nodeService, List<Node> nodeList) throws FastcatSearchException {
		boolean[] resultList = new boolean[nodeList.size()];
		logger.debug("tranferDirectory >> {}", directory.getAbsolutePath());
		Collection<File> files = FileUtils.listFiles(directory, null, true);
		int totalFileCount = files.size();

		// TODO 순차적전송을 개선하여 더 빠른 방법을 찾아보자.
		OUTTER:
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if(!node.isActive()){
				continue;
			}
			if (nodeService.isMyNode(node)) {
				// 자신에게는 전송하지 않는다.
				continue;
			}
			logger.debug("Send File Nodes [{} / {}] {}", new Object[] { i + 1, nodeList.size(), node });
			Iterator<File> fileIterator = files.iterator();
			int fileCount = 1;
			while (fileIterator.hasNext()) {
				File sourceFile = fileIterator.next();
				File relativeFile = environment.filePaths().relativise(sourceFile);
				logger.info("[{} / {}]파일 {} 전송시작! ", new Object[] { fileCount, totalFileCount, sourceFile.getPath() });

				SendFileResultFuture sendFileResultFuture;
				try {
					sendFileResultFuture = nodeService.sendFile(node, sourceFile, relativeFile);
				} catch (TransportException e) {
					logger.error("Transport exception sending {} to {} : {}", sourceFile.getName(), node, e);
					continue OUTTER; //다음노드로.
				}
				if (sendFileResultFuture != null) {
					Object result = sendFileResultFuture.take();
					if (sendFileResultFuture.isSuccess()) {
						logger.info("[{} / {}]파일 {} 전송완료!", new Object[] { fileCount, totalFileCount, relativeFile.getPath() });
					} else {
						logger.error("Fail to send {} to {} : {}", sourceFile.getName(), node);
						continue OUTTER; //다음노드로.
					}
				} else {
					// null이라면 디렉토리 또는 동일노드..
					logger.warn("skip file {} to {}", sourceFile.getName(), node);
					continue; //다음파일로...
				}

				fileCount++;
			}

			resultList[i] = true;
		}
		
		return resultList;
	}

	
	public void transferFile(File file, NodeService nodeService, Node node) throws TransportException {
		Collection<File> files = null;
		if(!file.exists()){
			//not exists.
			throw new TransportException("File is not exist. "+file.getAbsolutePath());
		}else if(file.isDirectory()){
			files = FileUtils.listFiles(file, null, true);
		}else{
			files = new HashSet<File>();
			files.add(file);
		}
		int totalFileCount = files.size();
		Iterator<File> fileIterator = files.iterator();
		int fileCount = 1;
		
		while (fileIterator.hasNext()) {
			File sourceFile = fileIterator.next();
			File relativeFile = environment.filePaths().relativise(sourceFile);
			logger.info("[{} / {}]파일 {} 전송시작! ", new Object[] { fileCount, totalFileCount, sourceFile.getPath() });

			SendFileResultFuture sendFileResultFuture = null;
			try {
				sendFileResultFuture = nodeService.sendFile(node, sourceFile, relativeFile);
			} catch (TransportException e) {
				logger.error("Transport exception sending {} to {} : {}", sourceFile.getName(), node, e);
				throw e;
			}
			if (sendFileResultFuture != null) {
				logger.debug("파일전송 결과대기.");
				Object result = sendFileResultFuture.take();
				logger.debug("파일전송 결과받음 >> {}", result);
				if (sendFileResultFuture.isSuccess()) {
					logger.info("[{} / {}]파일 {} 전송완료!", new Object[] { fileCount, totalFileCount, relativeFile.getPath() });
				} else {
					logger.error("Fail to send {} to {} : {}", sourceFile.getName(), node);
					throw new TransportException("Fail to send file "+sourceFile.getName());
				}
			} else {
				// null이라면 디렉토리 또는 동일노드..
				logger.warn("skip file {} to {}", sourceFile.getName(), node);
				continue; //다음파일로...
			}

			fileCount++;
		}
	}
}
