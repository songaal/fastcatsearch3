package org.fastcatsearch.job;

import java.io.File;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.common.SendFileResultFuture;

public class FileSendToNodeJob extends Job {
	private static final long serialVersionUID = 2700311768581340839L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		String[] args = getStringArrayArgs();
		if(args.length < 2){
			new FastcatSearchException("파라미터가 모자랍니다.");
			throw new FastcatSearchException("ERR-01100", 2, args.length);
		}
		
		String filepath = args[0];
		String nodeId = args[1];
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		File sourceFile = environment.filePaths().file(filepath);
		SendFileResultFuture resultFuture = null;
		try {
			resultFuture = nodeService.sendFile(node, sourceFile, new File(filepath));
		} catch (TransportException e) {
			logger.error("", e);
			throw new FastcatSearchException("ERR-00700", filepath);
		}
		
		if(resultFuture == null){
			throw new FastcatSearchException("ERR-00700", filepath);
		}
		logger.debug("파일전송 결과대기.");
		Object result = resultFuture.take();
		logger.debug("파일전송 결과받음 >> {}", result);
		return new JobResult(resultFuture.get());
	}

}
