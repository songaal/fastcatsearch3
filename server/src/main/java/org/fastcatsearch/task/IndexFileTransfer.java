package org.fastcatsearch.task;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.settings.SettingFileNames;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFileTransfer {
	
	private static Logger logger = LoggerFactory.getLogger(IndexFileTransfer.class);
	
	private Environment environment;
	
	public IndexFileTransfer(Environment environment){
		this.environment = environment;
	}
	
	//세그먼트를 전송한다. info.xml도 함께 전송.
	public void tranferSegmentDir(File collectionDataDir, File segmentDir, NodeService nodeService, List<Node> nodeList) throws FastcatSearchException{
		Collection<File> files = FileUtils.listFiles(segmentDir, null, true);
		//info.xml 파일추가.
		File collectionInfoFile = new File(collectionDataDir, SettingFileNames.dataInfo);
		files.add(collectionInfoFile);
		int totalFileCount = files.size();

		//TODO 순차적전송이라서 여러노드전송시 속도가 느림.해결요망.  
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if(nodeService.isMyNode(node)){
				//자신에게는 전송하지 않는다.
				continue;
			}
			logger.debug("Send File Nodes [{} / {}] {}", new Object[]{i+1, nodeList.size(), node});
			Iterator<File> fileIterator = files.iterator();
			int fileCount = 1;
			while (fileIterator.hasNext()) {
				File sourceFile = fileIterator.next();
				File relativeFile = environment.filePaths().relativise(sourceFile);
//				logger.debug("sourceFile >> {}", sourceFile.getAbsolutePath());
//				logger.debug("targetFile >> {}", relativeFile.getPath());
				logger.info("[{} / {}]파일 {} 전송시작! ", new Object[] { fileCount, totalFileCount, sourceFile.getPath() });
				
				SendFileResultFuture sendFileResultFuture = nodeService.sendFile(node, sourceFile, relativeFile);
				if(sendFileResultFuture != null){
					Object result = sendFileResultFuture.take();
					if (sendFileResultFuture.isSuccess()) {
						logger.info("[{} / {}]파일 {} 전송완료!", new Object[] { fileCount, totalFileCount, relativeFile.getPath() });
					} else {
						throw new FastcatSearchException("파일전송에 실패했습니다.");
					}
				}else{
					//null이라면 전송에러.
					logger.warn("디렉토리는 전송할수 없습니다.");
					break;
				}
				
				fileCount++;
			}

		}
	}
}
